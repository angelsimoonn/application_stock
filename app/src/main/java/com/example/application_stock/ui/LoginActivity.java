package com.example.application_stock.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Usuario;
import com.example.application_stock.storage.TokenManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUser, txtPass;
    private Button btnLogin, btnIrRegistro;
    private TextView txtOlvide;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tokenManager = new TokenManager(this);

        SharedPreferences prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // Tema
        int themeMode = prefs.getInt("theme_mode", 0);
        switch (themeMode) {
            case 0: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
            case 1: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case 2: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
        }

        setContentView(R.layout.activity_login);

        // Auto Login
        boolean autoLoginEnabled = prefs.getBoolean("auto_login", true);
        if (tokenManager.getToken() != null && autoLoginEnabled) {
            irAlMenuPrincipal();
            return;
        }

        txtUser = findViewById(R.id.edtUsuario);
        txtPass = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        txtOlvide = findViewById(R.id.txtOlvidePass); // VINCULAR NUEVO TEXTO

        // 2. VINCULAR LA VISTA
        btnIrRegistro = findViewById(R.id.btnIrRegistro);

        // 3. DARLE VIDA (Esto es lo que hace que cambie de pantalla)
        btnIrRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistroActivity.class);
            startActivity(intent);
        });

        // Listener Login
        btnLogin.setOnClickListener(v -> {
            String usuario = txtUser.getText().toString().trim();
            String password = txtPass.getText().toString().trim();
            if (usuario.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Rellena los campos", Toast.LENGTH_SHORT).show();
            } else {
                login(usuario, password);
            }
        });

        // Listener Olvidé Contraseña
        txtOlvide.setOnClickListener(v -> mostrarDialogoRecuperar());
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

                        // --- CAMBIO CLAVE: GUARDAR EL USERNAME ---
                        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                .edit()
                                .putString("username", nombre) // Guardamos quién se ha logueado
                                .apply();
                        // -----------------------------------------

                        Toast.makeText(LoginActivity.this, "Login correcto", Toast.LENGTH_SHORT).show();
                        irAlMenuPrincipal();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                btnLogin.setEnabled(true);
                btnLogin.setText("Entrar");
                Toast.makeText(LoginActivity.this, "Error conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoRecuperar() {
        EditText inputEmail = new EditText(this);
        inputEmail.setHint("Introduce tu email registrado");
        inputEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        // Un poco de padding estético
        int pad = (int)(16 * getResources().getDisplayMetrics().density);
        inputEmail.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(this)
                .setTitle("Recuperar Contraseña")
                .setMessage("Te enviaremos una contraseña temporal a tu correo.")
                .setView(inputEmail)
                .setPositiveButton("Enviar", (dialog, which) -> {
                    String email = inputEmail.getText().toString().trim();
                    if (!email.isEmpty()) enviarRecuperacion(email);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void enviarRecuperacion(String email) {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        Map<String, String> body = new HashMap<>();
        body.put("email", email);

        api.recuperarPassword(body).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this, "¡Correo enviado! Revisa tu bandeja.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Email no encontrado.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red.", Toast.LENGTH_SHORT).show();
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