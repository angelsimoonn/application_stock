package com.example.application_stock.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
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

        cargarCategorias();
        cargarProducto();

        btnGuardar.setOnClickListener(v -> guardarCambios());
        btnEliminar.setOnClickListener(v -> eliminarProducto());
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
                    txtPrecio.setText(String.valueOf(p.getPrecio()));
                    txtStock.setText(String.valueOf(p.getStock()));

                    if (p.getCategoria() != null) {
                        categoriaSeleccionadaId = p.getCategoria().getId();
                        seleccionarCategoria(categoriaSeleccionadaId);
                    }
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(ProductoDetalleActivity.this, "Error al cargar producto", Toast.LENGTH_SHORT).show();
            }
        });
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
                            listaCategorias.stream().map(c -> c.getNombre()).toArray(String[]::new)
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);

                    spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            categoriaSeleccionadaId = listaCategorias.get(position).getId();
                        }

                        @Override
                        public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {}
        });
    }

    private void seleccionarCategoria(Long id) {
        if (listaCategorias == null) return;

        for (int i = 0; i < listaCategorias.size(); i++) {
            if (listaCategorias.get(i).getId().equals(id)) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }
    }

    private void guardarCambios() {
        Producto p = new Producto();
        p.setNombre(txtNombre.getText().toString());
        p.setDescripcion(txtDescripcion.getText().toString());
        p.setPrecio(new BigDecimal(txtPrecio.getText().toString()));
        p.setStock(Integer.parseInt(txtStock.getText().toString()));

        Categoria c = new Categoria();
        c.setId(categoriaSeleccionadaId);
        p.setCategoria(c);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        api.actualizarProducto(productoId, p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoDetalleActivity.this, "Actualizado correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {}
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
