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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;
import com.example.application_stock.utils.NotificationHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoCrearActivity extends AppCompatActivity {

    // Vistas
    EditText txtNombre, txtDescripcion, txtPrecio, txtStock;
    Spinner spinnerCategoria;
    Button btnCrear, btnCamara, btnGaleria;
    ImageView imgPreview;

    // Variables lógicas
    List<Categoria> categorias;
    Long categoriaSeleccionadaId;
    String imagenBase64 = null; // Aquí se guarda la foto para enviarla

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_crear);

        // 1. Vincular vistas
        txtNombre = findViewById(R.id.txtNombreCrear);
        txtDescripcion = findViewById(R.id.txtDescripcionCrear);
        txtPrecio = findViewById(R.id.txtPrecioCrear);
        txtStock = findViewById(R.id.txtStockCrear);
        spinnerCategoria = findViewById(R.id.spinnerCategoriaCrear);
        btnCrear = findViewById(R.id.btnCrearProducto);

        imgPreview = findViewById(R.id.imgPreview);
        btnCamara = findViewById(R.id.btnCamara);
        btnGaleria = findViewById(R.id.btnGaleria);

        // 2. Configurar Listeners de Botones
        btnCrear.setOnClickListener(v -> crearProducto());
        btnCamara.setOnClickListener(v -> abrirCamara());
        btnGaleria.setOnClickListener(v -> abrirGaleria());

        // 3. Cargar datos iniciales
        cargarCategorias();
    }

    private void cargarCategorias() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias = response.body();

                    // VALIDACIÓN: Si la lista está vacía, avisamos
                    if (categorias.isEmpty()) {
                        Toast.makeText(ProductoCrearActivity.this, "¡No hay categorías! Crea una primero.", Toast.LENGTH_LONG).show();
                        btnCrear.setEnabled(false); // Bloqueamos para que no falle
                        return;
                    }

                    // Llenamos el Spinner con los nombres
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProductoCrearActivity.this,
                            android.R.layout.simple_spinner_item,
                            categorias.stream().map(Categoria::getNombre).toArray(String[]::new)
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);

                    // AUTO-SELECCIONAR la primera categoría para que no sea null
                    categoriaSeleccionadaId = categorias.get(0).getId();

                    // Listener para cuando el usuario cambie la selección
                    spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            categoriaSeleccionadaId = categorias.get(position).getId();
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });

                } else {
                    Toast.makeText(ProductoCrearActivity.this, "Error al cargar categorías", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(ProductoCrearActivity.this, "Error de conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void crearProducto() {
        // VALIDACIONES
        String nombre = txtNombre.getText().toString();
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoriaSeleccionadaId == null) {
            Toast.makeText(this, "Espera a que carguen las categorías", Toast.LENGTH_SHORT).show();
            return;
        }

        // PREPARAR OBJETO
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setDescripcion(txtDescripcion.getText().toString());

        // Manejo de números (con valores por defecto si están vacíos)
        String precioStr = txtPrecio.getText().toString();
        p.setPrecio(precioStr.isEmpty() ? new BigDecimal("0") : new BigDecimal(precioStr));

        String stockStr = txtStock.getText().toString();
        p.setStock(stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr));

        // Asignar Categoría y Foto
        p.setCategoriaId(categoriaSeleccionadaId);
        p.setImagen(imagenBase64); // Puede ser null, no pasa nada

        // ENVIAR AL BACKEND
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.crearProducto(p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoCrearActivity.this, "Producto Creado!", Toast.LENGTH_SHORT).show();
                    
                    // Mandar notificación
                    NotificationHelper.sendNotification(ProductoCrearActivity.this, 
                            "Producto Añadido", 
                            "Se ha registrado " + nombre + " correctamente.");

                    finish(); // Cierra la pantalla y vuelve a la lista
                } else {
                    Toast.makeText(ProductoCrearActivity.this, "Error servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(ProductoCrearActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // =========================================================
    //        LÓGICA DE CÁMARA Y GALERÍA
    // =========================================================

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

    // PROCESAMIENTO: Escalar y convertir a Base64
    private void procesarImagen(Bitmap bitmap) {
        Bitmap redimensionado = escalarBitmap(bitmap, 800); // Max 800px
        imgPreview.setImageBitmap(redimensionado); // Mostrar en pantalla
        imagenBase64 = convertirBitmapABase64(redimensionado); // Guardar string
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}