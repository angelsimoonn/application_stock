package com.example.application_stock.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.RegisterRequest;
import com.example.application_stock.storage.TokenManager;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroActivity extends AppCompatActivity {

    EditText edtUser, edtPass, edtEmail;
    Button btnRegistrar;
    TextView txtVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        edtUser = findViewById(R.id.edtRegUser);
        edtPass = findViewById(R.id.edtRegPass);
        edtEmail = findViewById(R.id.edtRegEmail);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        txtVolver = findViewById(R.id.txtVolverLogin);

        btnRegistrar.setOnClickListener(v -> registrarse());
        txtVolver.setOnClickListener(v -> finish()); // Vuelve al login
    }

    private void registrarse() {
        String user = edtUser.getText().toString().trim();
        String pass = edtPass.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();

        if (user.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest req = new RegisterRequest(user, pass, email);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.registrar(req).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().get("token");
                    if (token != null) {
                        new TokenManager(RegistroActivity.this).saveToken(token);

                        getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                                .edit().putString("username", user).apply();

                        Toast.makeText(RegistroActivity.this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(RegistroActivity.this, MainMenuActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(RegistroActivity.this, "Error: Usuario o email ya existen", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Toast.makeText(RegistroActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}