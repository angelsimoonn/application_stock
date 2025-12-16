package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Usuario;
import com.example.application_stock.storage.TokenManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUser, txtPass;
    private Button btnLogin;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 1. Inicializamos el TokenManager (que ahora usa encriptación internamente)
        tokenManager = new TokenManager(this);

        // 2. Comprobar si ya estamos logueados para ir directo al menú (Opcional)
        if (tokenManager.getToken() != null) {
            irAlMenuPrincipal();
        }

        // 3. Vincular vistas del XML
        txtUser = findViewById(R.id.edtUsuario);
        txtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // 4. Configurar el botón
        btnLogin.setOnClickListener(v -> {
            String usuario = txtUser.getText().toString().trim();
            String password = txtPass.getText().toString().trim();

            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show();
            } else {
                login(usuario, password);
            }
        });
    }

    private void login(String nombre, String password) {
        // Bloqueamos el botón para que no le den dos veces
        btnLogin.setEnabled(false);
        btnLogin.setText("Cargando...");

        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        // Creamos el objeto Usuario con los datos (coincide con LoginRequest del backend)
        Usuario u = new Usuario(nombre, password);

        api.login(u).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                // Reactivamos el botón pase lo que pase
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    // El backend devuelve un JSON tipo: {"token": "...", "rol": "..."}
                    String token = response.body().get("token");

                    if (token != null) {
                        // AQUÍ guardamos el token usando el método que te daba error antes
                        tokenManager.saveToken(token);

                        Toast.makeText(LoginActivity.this, "Login correcto", Toast.LENGTH_SHORT).show();
                        irAlMenuPrincipal();
                    } else {
                        Toast.makeText(LoginActivity.this, "El servidor no envió un token válido", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    // Si el código es 401 (Unauthorized) o 403 (Forbidden)
                    Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAlMenuPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
        // Estas flags evitan que el usuario pueda volver atrás al login pulsando "Atrás"
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}