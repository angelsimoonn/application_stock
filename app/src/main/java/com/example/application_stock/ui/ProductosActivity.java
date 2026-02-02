package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText; // IMPORTANTE
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application_stock.R;
import com.example.application_stock.adapter.ProductosAdapter;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosActivity extends AppCompatActivity {

    RecyclerView recycler;
    Spinner spinnerFiltro;
    EditText txtBuscador; // NUEVO
    FloatingActionButton btnCrearProducto;

    List<Categoria> listaCategorias = new ArrayList<>();
    ProductosAdapter productosAdapter; // Necesitamos referencia al adapter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        recycler = findViewById(R.id.recyclerProductos);
        spinnerFiltro = findViewById(R.id.spinnerFiltroCategoria);
        txtBuscador = findViewById(R.id.txtBuscador); // NUEVO
        btnCrearProducto = findViewById(R.id.btnAddProducto);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        btnCrearProducto.setOnClickListener(v ->
                startActivity(new Intent(ProductosActivity.this, ProductoCrearActivity.class))
        );

        // CONFIGURAR EL BUSCADOR
        configurarBuscador();

        // Cargar datos
        cargarCategoriasParaFiltro();
    }

    // MÉTODO NUEVO
    private void configurarBuscador() {
        txtBuscador.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cada vez que escribimos, llamamos al adapter
                if (productosAdapter != null) {
                    productosAdapter.filtrar(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Limpiamos el buscador al volver para evitar confusiones
        if(txtBuscador != null) txtBuscador.setText("");

        if (spinnerFiltro.getSelectedItemPosition() > 0) {
            // Lógica del spinner recarga sola
        } else {
            cargarTodosLosProductos();
        }
        cargarCategoriasParaFiltro();
    }

    // ... (El resto de cargarCategoriasParaFiltro sigue igual) ...
    // Solo cambia donde asignamos el adapter en las respuestas de la API:

    private void cargarCategoriasParaFiltro() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    listaCategorias = response.body();
                    List<String> nombres = new ArrayList<>();
                    nombres.add("TODAS");
                    for (Categoria c : listaCategorias) {
                        nombres.add(c.getNombre());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProductosActivity.this,
                            android.R.layout.simple_spinner_item,
                            nombres
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerFiltro.setAdapter(adapter);

                    spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            // Limpiamos buscador al cambiar categoría
                            txtBuscador.setText("");

                            if (position == 0) {
                                cargarTodosLosProductos();
                            } else {
                                Long idCat = listaCategorias.get(position - 1).getId();
                                cargarProductosFiltrados(idCat);
                            }
                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {}
        });
    }

    private void cargarTodosLosProductos() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    // Guardamos la referencia en la variable global
                    productosAdapter = new ProductosAdapter(response.body());
                    recycler.setAdapter(productosAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {}
        });
    }

    private void cargarProductosFiltrados(Long categoriaId) {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getProductosPorCategoria(categoriaId).enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    // Guardamos la referencia en la variable global
                    productosAdapter = new ProductosAdapter(response.body());
                    recycler.setAdapter(productosAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(ProductosActivity.this, "Error al filtrar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}