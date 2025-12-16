package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
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
    ImageButton btnGestionarCategorias;
    FloatingActionButton btnCrearProducto;

    List<Categoria> listaCategorias = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        recycler = findViewById(R.id.recyclerProductos);
        spinnerFiltro = findViewById(R.id.spinnerFiltroCategoria);
        btnGestionarCategorias = findViewById(R.id.btnGestionarCategorias);
        btnCrearProducto = findViewById(R.id.btnAddProducto);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Botón para crear producto
        btnCrearProducto.setOnClickListener(v ->
                startActivity(new Intent(ProductosActivity.this, ProductoCrearActivity.class))
        );

        // Botón para ir a crear Categorías (Tipos)
        btnGestionarCategorias.setOnClickListener(v ->
                startActivity(new Intent(ProductosActivity.this, CategoriasActivity.class))
        );

        // Cargar datos
        cargarCategoriasParaFiltro();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargamos el filtro seleccionado actualmente o todas
        if (spinnerFiltro.getSelectedItemPosition() > 0) {
            // Si hay algo seleccionado que no sea "TODAS", filtramos
            // (La lógica de recarga está en el listener del spinner)
        } else {
            cargarTodosLosProductos();
        }
        // También refrescamos las categorías por si has creado una nueva
        cargarCategoriasParaFiltro();
    }

    private void cargarCategoriasParaFiltro() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    listaCategorias = response.body();

                    // Truco: Añadir opción "TODAS" al principio
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

                    // Listener para cuando cambias el filtro
                    spinnerFiltro.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (position == 0) {
                                cargarTodosLosProductos();
                            } else {
                                // -1 porque la posición 0 es "TODAS"
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
                    recycler.setAdapter(new ProductosAdapter(response.body()));
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
                    recycler.setAdapter(new ProductosAdapter(response.body()));
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Toast.makeText(ProductosActivity.this, "Error al filtrar", Toast.LENGTH_SHORT).show();
            }
        });
    }
}