package com.example.application_stock.ui;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;

import java.math.BigDecimal;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductoDetalleActivity extends AppCompatActivity {

    EditText txtNombre, txtDescripcion, txtPrecio, txtStock;
    Spinner spinnerCategoria;
    Button btnGuardar, btnEliminar;

    Long productoId;
    Long categoriaSeleccionadaId;

    List<Categoria> listaCategorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        productoId = getIntent().getLongExtra("productoId", -1);

        txtNombre = findViewById(R.id.txtNombreDetalle);
        txtDescripcion = findViewById(R.id.txtDescripcionDetalle);
        txtPrecio = findViewById(R.id.txtPrecioDetalle);
        txtStock = findViewById(R.id.txtStockDetalle);
        spinnerCategoria = findViewById(R.id.spinnerCategoriaDetalle);
        btnGuardar = findViewById(R.id.btnGuardarDetalle);
        btnEliminar = findViewById(R.id.btnEliminarDetalle);

        // CAMBIO 1: Solo llamamos a cargarCategorias.
        // cargarProducto() se llamará cuando las categorías estén listas.
        cargarCategorias();

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
                                Categoria c = listaCategorias.get(position);
                                categoriaSeleccionadaId = c.getId();

                                // LOG PARA VER QUÉ ESTÁS SELECCIONANDO REALMENTE
                                System.out.println("ANDROID DEBUG: Seleccionado: " + c.getNombre() + " -> ID REAL: " + c.getId());
                            }
                        }
                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });

                    // CAMBIO 2: AHORA que tenemos la lista, cargamos el producto
                    // para poder seleccionar la categoría correcta en el spinner
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

                    // CAMBIO 3: Asignamos y seleccionamos visualmente
                    if (p.getCategoriaId() != null) {
                        categoriaSeleccionadaId = p.getCategoriaId(); // Aseguramos la variable
                        seleccionarCategoria(p.getCategoriaId());     // Aseguramos la vista
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

        // CAMBIO 4: Validación extra
        if (categoriaSeleccionadaId != null) {
            p.setCategoriaId(categoriaSeleccionadaId);
        } else {
            Toast.makeText(this, "Espera a que carguen las categorías", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.gson.Gson gson = new com.google.gson.Gson();
        String jsonQueSeVaAEnviar = gson.toJson(p);
        System.out.println("ANDROID DEBUG JSON: " + jsonQueSeVaAEnviar);

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
}