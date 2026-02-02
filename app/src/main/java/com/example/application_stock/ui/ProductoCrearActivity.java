package com.example.application_stock.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.application_stock.R;
// ... tus otros imports ...
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ProductoCrearActivity extends AppCompatActivity {

    // ... variables existentes ...
    ImageView imgPreview;
    Button btnCamara, btnGaleria;
    String imagenBase64 = null; // Aquí guardaremos la foto final

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_crear);

        // Vincular vistas antiguas...
        // ...

        // Nuevas vistas
        imgPreview = findViewById(R.id.imgPreview);
        btnCamara = findViewById(R.id.btnCamara);
        btnGaleria = findViewById(R.id.btnGaleria);

        btnCamara.setOnClickListener(v -> abrirCamara());
        btnGaleria.setOnClickListener(v -> abrirGaleria());

        // Al dar a Crear, asegúrate de setear la imagen en el objeto P
        /*
        btnCrear.setOnClickListener(v -> {
             Producto p = new Producto();
             // ... set nombre, precio ...
             p.setImagen(imagenBase64); // <--- IMPORTANTE

             // llamar a API...
        });
        */
    }

    // --- GESTIÓN DE CÁMARA ---
    private void abrirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        }
    }

    ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    procesarImagen(imageBitmap);
                }
            });

    // --- GESTIÓN DE GALERÍA ---
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImage = result.getData().getData();
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(selectedImage);
                        Bitmap selectedBitmap = BitmapFactory.decodeStream(imageStream);
                        procesarImagen(selectedBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

    // --- PROCESAMIENTO Y COMPRESIÓN (CRUCIAL) ---
    private void procesarImagen(Bitmap bitmap) {
        // 1. Redimensionar si es muy grande (Max 800px) para no petar la BD
        Bitmap redimensionado = escalarBitmap(bitmap, 800);

        // 2. Mostrar en pantalla
        imgPreview.setImageBitmap(redimensionado);

        // 3. Convertir a Base64 String
        imagenBase64 = convertirBitmapABase64(redimensionado);
    }

    private Bitmap escalarBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        if (width > maxWidth) {
            float ratio = (float) width / maxWidth;
            width = maxWidth;
            height = (int) (height / ratio);
            return Bitmap.createScaledBitmap(bitmap, width, height, true);
        }
        return bitmap;
    }

    private String convertirBitmapABase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        // Calidad 70 para reducir tamaño sin perder mucha calidad
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}