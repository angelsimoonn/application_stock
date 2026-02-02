package com.example.application_stock.ui;

import android.content.Context; // <--- IMPORTANTE
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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

        // 1. Inicializamos TokenManager
        tokenManager = new TokenManager(this);

        // 2. Cargamos las preferencias UNA SOLA VEZ
        // Usamos 'Context.MODE_PRIVATE' para arreglar el error del modo
        SharedPreferences prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // --- APLICAR TEMA GUARDADO ---
        int themeMode = prefs.getInt("theme_mode", 0);
        switch (themeMode) {
            case 0: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
            case 1: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case 2: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
        }

        // --- IMPORTANTE: setContentView VA DESPUÉS DE CONFIGURAR EL TEMA ---
        // Si lo pones antes, podría verse un parpadeo de colores incorrectos al abrir la app.
        setContentView(R.layout.activity_login);


        // --- LÓGICA DE AUTO LOGIN ---

        // Reutilizamos la variable 'prefs' que ya creamos arriba (no ponemos 'SharedPreferences' otra vez)
        boolean autoLoginEnabled = prefs.getBoolean("auto_login", true);

        // Comprobamos: ¿Tengo token? Y ADEMÁS ¿El auto login está activado?
        if (tokenManager.getToken() != null && autoLoginEnabled) {
            irAlMenuPrincipal();
            return; // Añadimos return para que no siga ejecutando código de vista innecesario
        }

        // ----------------------------------

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
        btnLogin.setEnabled(false);
        btnLogin.setText("Cargando...");

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        Usuario u = new Usuario(nombre, password);

        api.login(u).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");

                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().get("token");

                    if (token != null) {
                        tokenManager.saveToken(token);
                        Toast.makeText(LoginActivity.this, "Login correcto", Toast.LENGTH_SHORT).show();
                        irAlMenuPrincipal();
                    } else {
                        Toast.makeText(LoginActivity.this, "Error: Token vacío", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Usuario o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this, "Error de conexión", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void irAlMenuPrincipal() {
        Intent intent = new Intent(LoginActivity.this, MainMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}