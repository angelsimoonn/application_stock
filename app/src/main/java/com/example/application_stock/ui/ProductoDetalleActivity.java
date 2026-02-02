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

import com.bumptech.glide.Glide;
import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoDetalleActivity extends AppCompatActivity {

    EditText txtNombre, txtDescripcion, txtPrecio, txtStock;
    Spinner spinnerCategoria;
    Button btnGuardar, btnEliminar;

    // VARIABLES PARA IMAGEN
    ImageView imgPreview;
    Button btnCamara, btnGaleria;
    String imagenBase64 = null;

    Long productoId;
    Long categoriaSeleccionadaId;
    List<Categoria> listaCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        productoId = getIntent().getLongExtra("productoId", -1);

        // VINCULAR VISTAS
        txtNombre = findViewById(R.id.txtNombreDetalle);
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle);
        txtPrecio = findViewById(R.id.txtPrecioDetalle);
        txtStock = findViewById(R.id.txtStockDetalle);
        spinnerCategoria = findViewById(R.id.spinnerCategoriaDetalle);
        btnGuardar = findViewById(R.id.btnGuardarDetalle);
        btnEliminar = findViewById(R.id.btnEliminarDetalle);

        // Vistas de Imagen
        imgPreview = findViewById(R.id.imgPreviewDetalle);
        btnCamara = findViewById(R.id.btnCamaraDetalle);
        btnGaleria = findViewById(R.id.btnGaleriaDetalle);

        // CONFIGURAR LISTENERS IMAGEN
        btnCamara.setOnClickListener(v -> abrirCamara());
        btnGaleria.setOnClickListener(v -> abrirGaleria());

        // CARGAR DATOS
        cargarCategorias(); // Al terminar llama a cargarProducto()

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
    }

    private void cargarCategorias() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    listaCategorias = response.body();

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProductoDetalleActivity.this,
                            android.R.layout.simple_spinner_item,
                            listaCategorias.stream().map(Categoria::getNombre).toArray(String[]::new)
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);

                    spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            if (listaCategorias != null && !listaCategorias.isEmpty()) {
                                categoriaSeleccionadaId = listaCategorias.get(position).getId();
                            }
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });

                    // Una vez tenemos categorías, cargamos el producto
                    cargarProducto();
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(ProductoDetalleActivity.this, "Error cargando categorías", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cargarProducto() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        api.getProducto(productoId).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Producto p = response.body();

                    txtNombre.setText(p.getNombre());
                    txtDescripcion.setText(p.getDescripcion());
                    txtPrecio.setText(p.getPrecio() != null ? String.valueOf(p.getPrecio()) : "");
                    txtStock.setText(p.getStock() != null ? String.valueOf(p.getStock()) : "");

                    // MOSTRAR IMAGEN EXISTENTE
                    if (p.getImagen() != null && !p.getImagen().isEmpty()) {
                        byte[] imageBytes = Base64.decode(p.getImagen(), Base64.DEFAULT);
                        Glide.with(ProductoDetalleActivity.this)
                                .load(imageBytes)
                                .placeholder(android.R.drawable.ic_menu_camera)
                                .into(imgPreview);
                    }

                    if (p.getCategoriaId() != null) {
                        categoriaSeleccionadaId = p.getCategoriaId();
                        seleccionarCategoria(p.getCategoriaId());
                    }
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(ProductoDetalleActivity.this, "Error al cargar producto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void seleccionarCategoria(Long idBuscado) {
        if (listaCategorias == null) return;
        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getId().equals(idBuscado)) {
                spinnerCategoria.setSelection(i);
                return;
            }
        }
    }

    private void guardarCambios() {
        if (txtNombre.getText().toString().isEmpty()) {
            Toast.makeText(this, "El nombre es obligatorio", Toast.LENGTH_SHORT).show();
            return;
        }

        Producto p = new Producto();
        p.setNombre(txtNombre.getText().toString());
        p.setDescripcion(txtDescripcion.getText().toString());

        try {
            String precioStr = txtPrecio.getText().toString();
            if (precioStr.isEmpty()) precioStr = "0";
            p.setPrecio(new BigDecimal(precioStr));

            String stockStr = txtStock.getText().toString();
            if (stockStr.isEmpty()) stockStr = "0";
            p.setStock(Integer.parseInt(stockStr));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Números incorrectos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Si hemos elegido una nueva foto, la mandamos.
        // Si no (es null), el backend mantendrá la antigua gracias a nuestra lógica en Java del backend.
        if (imagenBase64 != null) {
            p.setImagen(imagenBase64);
        }

        if (categoriaSeleccionadaId != null) {
            p.setCategoriaId(categoriaSeleccionadaId);
        }

        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.actualizarProducto(productoId, p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(ProductoDetalleActivity.this, "Error servidor: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(ProductoDetalleActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarProducto() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.eliminarProducto(productoId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Toast.makeText(ProductoDetalleActivity.this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                finish();
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {}
        });
    }

    // =========================================================
    //        LÓGICA DE IMÁGENES (Idéntica a Crear)
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

    private void procesarImagen(Bitmap bitmap) {
        Bitmap redimensionado = escalarBitmap(bitmap, 800);
        imgPreview.setImageBitmap(redimensionado);
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}