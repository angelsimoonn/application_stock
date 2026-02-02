package com.example.application_stock.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // IMPORTANTE

import com.example.application_stock.R;
import com.example.application_stock.storage.TokenManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class AjustesActivity extends AppCompatActivity {

    SwitchMaterial switchAutoLogin;
    Button btnLogout;
    LinearLayout btnTema;
    TextView txtTemaActual;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_ajustes);

        switchAutoLogin = findViewById(R.id.switchAutoLogin);
        btnLogout = findViewById(R.id.btnCerrarSesion);
        btnTema = findViewById(R.id.layoutTema);
        txtTemaActual = findViewById(R.id.txtTemaActual);

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // --- AUTO LOGIN ---
        boolean isAutoLogin = prefs.getBoolean("auto_login", true);
        switchAutoLogin.setChecked(isAutoLogin);
        switchAutoLogin.setOnCheckedChangeListener((v, isChecked) ->
                prefs.edit().putBoolean("auto_login", isChecked).apply()
        );

        // --- CERRAR SESIÓN ---
        btnLogout.setOnClickListener(v -> {
            TokenManager tokenManager = new TokenManager(this);
            tokenManager.clear();
            Intent intent = new Intent(AjustesActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // --- GESTIÓN DE TEMAS ---
        actualizarTextoTema();

        btnTema.setOnClickListener(v -> mostrarDialogoTema());
    }

    private void mostrarDialogoTema() {
        String[] opciones = {"Sistema (Predeterminado)", "Claro", "Oscuro"};
        int checkedItem = prefs.getInt("theme_mode", 0); // 0=System, 1=Light, 2=Dark

        new AlertDialog.Builder(this)
                .setTitle("Elige un tema")
                .setSingleChoiceItems(opciones, checkedItem, (dialog, which) -> {
                    // Guardamos la selección
                    prefs.edit().putInt("theme_mode", which).apply();

                    // Aplicamos el cambio
                    aplicarTema(which);

                    actualizarTextoTema();
                    dialog.dismiss();
                })
                .show();
    }

    private void aplicarTema(int mode) {
        switch (mode) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }
    }

    private void actualizarTextoTema() {
        int mode = prefs.getInt("theme_mode", 0);
        String[] textos = {"Sistema", "Claro", "Oscuro"};
        txtTemaActual.setText(textos[mode]);
    }
}