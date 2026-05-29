package com.example.application_stock.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.ChangePasswordRequest;
import com.example.application_stock.storage.TokenManager;
import com.example.application_stock.utils.NotificationHelper;
import com.google.android.material.switchmaterial.SwitchMaterial;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AjustesActivity extends AppCompatActivity {

    SwitchMaterial switchAutoLogin;
    CheckBox cbNotificaciones;
    RadioGroup rgPrioridad;
    Button btnLogout, btnCambiarPass;
    LinearLayout btnTema, btnIdioma;
    TextView txtTemaActual, txtIdiomaActual;
    SharedPreferences prefs;

    // Manejador para solicitar permiso de notificaciones en Android 13+
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enviarNotificacionPrueba();
                } else {
                    Toast.makeText(this, "Permiso de notificaciones denegado", Toast.LENGTH_SHORT).show();
                    cbNotificaciones.setChecked(false);
                    prefs.edit().putBoolean("notifications_enabled", false).apply();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actvity_ajustes);

        // Vincular vistas
        switchAutoLogin = findViewById(R.id.switchAutoLogin);
        cbNotificaciones = findViewById(R.id.cbNotificaciones);
        rgPrioridad = findViewById(R.id.rgPrioridad);
        btnLogout = findViewById(R.id.btnCerrarSesion);
        btnCambiarPass = findViewById(R.id.btnCambiarPass);
        btnTema = findViewById(R.id.layoutTema);
        txtTemaActual = findViewById(R.id.txtTemaActual);
        btnIdioma = findViewById(R.id.layoutIdioma);
        txtIdiomaActual = findViewById(R.id.txtIdiomaActual);

        prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE);

        // 1. Cargar preferencias guardadas
        cargarPreferencias();

        // 2. Listeners para guardar cambios automáticamente
        switchAutoLogin.setOnCheckedChangeListener((v, isChecked) ->
                prefs.edit().putBoolean("auto_login", isChecked).apply()
        );

        cbNotificaciones.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("notifications_enabled", isChecked).apply();
            if (isChecked) {
                verificarPermisoYNotificar();
            }
        });

        rgPrioridad.setOnCheckedChangeListener((group, checkedId) -> {
            int priority = 0; // 0: Baja, 1: Media, 2: Alta
            if (checkedId == R.id.rbMedia) priority = 1;
            else if (checkedId == R.id.rbAlta) priority = 2;
            prefs.edit().putInt("notification_priority", priority).apply();
        });

        // 3. Otros listeners
        btnLogout.setOnClickListener(v -> cerrarSesion());
        btnTema.setOnClickListener(v -> mostrarDialogoTema());
        btnIdioma.setOnClickListener(v -> mostrarDialogoIdioma());
        btnCambiarPass.setOnClickListener(v -> mostrarDialogoCambioPassword());

        actualizarTextoTema();
        actualizarTextoIdioma();
    }

    private void verificarPermisoYNotificar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                enviarNotificacionPrueba();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            enviarNotificacionPrueba();
        }
    }

    private void enviarNotificacionPrueba() {
        NotificationHelper.sendNotification(this, 
                "Notificaciones activadas", 
                "Ahora recibirás alertas sobre el stock de tus productos.");
    }

    private void cargarPreferencias() {
        switchAutoLogin.setChecked(prefs.getBoolean("auto_login", true));
        cbNotificaciones.setChecked(prefs.getBoolean("notifications_enabled", true));
        
        int priority = prefs.getInt("notification_priority", 1); // Por defecto Media
        if (priority == 0) ((RadioButton)findViewById(R.id.rbBaja)).setChecked(true);
        else if (priority == 1) ((RadioButton)findViewById(R.id.rbMedia)).setChecked(true);
        else if (priority == 2) ((RadioButton)findViewById(R.id.rbAlta)).setChecked(true);
    }

    private void cerrarSesion() {
        new TokenManager(this).clear();
        prefs.edit().remove("username").apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void mostrarDialogoCambioPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.change_password);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(20 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad/2, pad, pad/2);

        final EditText inputOld = new EditText(this);
        inputOld.setHint("Contraseña Actual");
        inputOld.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputOld);

        final EditText inputNew = new EditText(this);
        inputNew.setHint("Nueva Contraseña");
        inputNew.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(inputNew);

        builder.setView(layout);
        builder.setPositiveButton(R.string.confirm, (dialog, which) -> {
            String oldPass = inputOld.getText().toString();
            String newPass = inputNew.getText().toString();
            if(!oldPass.isEmpty() && !newPass.isEmpty()) {
                hacerCambioEnBackend(oldPass, newPass);
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
    }

    private void hacerCambioEnBackend(String oldPass, String newPass) {
        String username = prefs.getString("username", "");
        if(username.isEmpty()) return;

        ChangePasswordRequest req = new ChangePasswordRequest(username, oldPass, newPass);
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.cambiarPassword(req).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if(response.isSuccessful()) Toast.makeText(AjustesActivity.this, "Éxito", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    private void mostrarDialogoTema() {
        String[] opciones = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        int checkedItem = prefs.getInt("theme_mode", 0);

        new AlertDialog.Builder(this)
                .setTitle(R.string.theme)
                .setSingleChoiceItems(opciones, checkedItem, (dialog, which) -> {
                    prefs.edit().putInt("theme_mode", which).apply();
                    aplicarTema(which);
                    actualizarTextoTema();
                    dialog.dismiss();
                })
                .show();
    }

    private void aplicarTema(int mode) {
        switch (mode) {
            case 0: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM); break;
            case 1: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO); break;
            case 2: AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); break;
        }
    }

    private void actualizarTextoTema() {
        int mode = prefs.getInt("theme_mode", 0);
        String[] textos = {getString(R.string.theme_system), getString(R.string.theme_light), getString(R.string.theme_dark)};
        txtTemaActual.setText(textos[mode]);
    }

    private void mostrarDialogoIdioma() {
        String[] opciones = {getString(R.string.language_es), getString(R.string.language_en)};
        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        int checkedItem = currentLang.startsWith("en") ? 1 : 0;

        new AlertDialog.Builder(this)
                .setTitle(R.string.language)
                .setSingleChoiceItems(opciones, checkedItem, (dialog, which) -> {
                    String langCode = (which == 0) ? "es" : "en";
                    cambiarIdioma(langCode);
                    dialog.dismiss();
                })
                .show();
    }

    private void cambiarIdioma(String langCode) {
        LocaleListCompat appLocales = LocaleListCompat.forLanguageTags(langCode);
        AppCompatDelegate.setApplicationLocales(appLocales);
    }

    private void actualizarTextoIdioma() {
        String currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags();
        if (currentLang.startsWith("en")) {
            txtIdiomaActual.setText(R.string.language_en);
        } else {
            txtIdiomaActual.setText(R.string.language_es);
        }
    }
}
