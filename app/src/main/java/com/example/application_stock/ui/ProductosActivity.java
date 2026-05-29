package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton; // Importante
import android.widget.PopupMenu; // Importante
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager; // Importante
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
    EditText txtBuscador;
    FloatingActionButton btnCrearProducto;
    ImageButton btnOrdenar, btnCambiarVista; // Nuevos

    List<Categoria> listaCategorias = new ArrayList<>();
    ProductosAdapter productosAdapter;

    // Variable para controlar el modo actual (false = lista, true = grid)
    private boolean isGridMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos);

        recycler = findViewById(R.id.recyclerProductos);
        spinnerFiltro = findViewById(R.id.spinnerFiltroCategoria);
        txtBuscador = findViewById(R.id.txtBuscador);
        btnCrearProducto = findViewById(R.id.btnAddProducto);
        btnOrdenar = findViewById(R.id.btnOrdenar);
        btnCambiarVista = findViewById(R.id.btnCambiarVista);

        // Por defecto Lista lineal
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // --- BOTÓN CAMBIAR VISTA ---
        btnCambiarVista.setOnClickListener(v -> toggleVista());

        // --- BOTÓN ORDENAR ---
        btnOrdenar.setOnClickListener(v -> mostrarMenuOrden(v));

        // ... resto de listeners ...
        btnCrearProducto.setOnClickListener(v ->
                startActivity(new Intent(ProductosActivity.this, ProductoCrearActivity.class))
        );

        configurarBuscador();
        cargarCategoriasParaFiltro();
    }

    private void toggleVista() {
        isGridMode = !isGridMode; // Cambiamos el estado

        if (isGridMode) {
            // Ponemos Cuadrícula (2 columnas)
            recycler.setLayoutManager(new GridLayoutManager(this, 2));
            btnCambiarVista.setImageResource(android.R.drawable.ic_menu_agenda); // Icono para volver a lista
        } else {
            // Ponemos Lista Lineal
            recycler.setLayoutManager(new LinearLayoutManager(this));
            btnCambiarVista.setImageResource(android.R.drawable.ic_dialog_dialer); // Icono para ir a grid
        }

        // Avisamos al adapter para que use el XML correcto
        if (productosAdapter != null) {
            productosAdapter.setGridMode(isGridMode);
        }
    }

    private void mostrarMenuOrden(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        // Añadimos opciones manualmente
        popup.getMenu().add(0, 0, 0, "Nombre (A-Z)");
        popup.getMenu().add(0, 1, 1, "Precio: Menor a Mayor");
        popup.getMenu().add(0, 2, 2, "Precio: Mayor a Menor");
        popup.getMenu().add(0, 3, 3, "Stock: Menor a Mayor");
        popup.getMenu().add(0, 4, 4, "Stock: Mayor a Menor");

        popup.setOnMenuItemClickListener(item -> {
            if (productosAdapter != null) {
                productosAdapter.ordenar(item.getItemId());
            }
            return true;
        });
        popup.show();
    }

    // ... (Métodos existentes: configurarBuscador, onResume, etc) ...

    private void cargarTodosLosProductos() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getProductos().enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    productosAdapter = new ProductosAdapter(response.body());
                    // Aseguramos que el adapter respete el modo actual al cargar
                    productosAdapter.setGridMode(isGridMode);
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
                    productosAdapter = new ProductosAdapter(response.body());
                    // Aseguramos que el adapter respete el modo actual
                    productosAdapter.setGridMode(isGridMode);
                    recycler.setAdapter(productosAdapter);
                }
            }
            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {}
        });
    }

    // ... RESTO DE TU CÓDIGO (onResume, TextWatcher...) SE MANTIENE IGUAL
    private void configurarBuscador() {
        txtBuscador.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (productosAdapter != null) productosAdapter.filtrar(s.toString());
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void cargarCategoriasParaFiltro() {
        // ... (Copia tu código anterior aquí) ...
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

    @Override
    protected void onResume() {
        super.onResume();

        // Esto recargará las categorías y, al configurar el spinner,
        // disparará automáticamente la carga de productos ("TODAS").
        cargarCategoriasParaFiltro();

        if (txtBuscador != null) {
            txtBuscador.setText("");
        }
    }
}