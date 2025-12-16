package com.example.application_stock.storage;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class TokenManager {
    // Nombre del archivo de preferencias
    private static final String PREF_NAME = "secret_appstock_prefs";
    private static final String KEY_TOKEN = "jwt_token";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public TokenManager(Context context) {
        try {
            // Creamos la llave maestra para encriptar
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Iniciamos las preferencias encriptadas
            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Si falla la encriptación (raro), usamos modo normal como fallback
            e.printStackTrace();
            prefs = context.getSharedPreferences("appstock_prefs_fallback", Context.MODE_PRIVATE);
        }

        // Inicializamos el editor
        editor = prefs.edit();
    }

    // ESTE ES EL MÉTODO QUE TE FALTABA O DABA ERROR
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clear() {
        editor.clear().apply();
    }
}