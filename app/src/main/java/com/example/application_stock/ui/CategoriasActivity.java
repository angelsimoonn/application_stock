package com.example.application_stock.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriasActivity extends AppCompatActivity {

    ListView listView;
    FloatingActionButton btnAdd;
    List<Categoria> categorias;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Usamos un layout simple por defecto o crea uno nuevo
        setContentView(R.layout.activity_categorias);

        listView = findViewById(R.id.listaCategorias);
        btnAdd = findViewById(R.id.btnAddCategoria);

        btnAdd.setOnClickListener(v -> mostrarDialogoCrear());

        cargarCategorias();
    }

    private void cargarCategorias() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias = response.body();
                    List<String> nombres = new ArrayList<>();
                    for (Categoria c : categorias) nombres.add(c.getNombre());

                    adapter = new ArrayAdapter<>(CategoriasActivity.this, android.R.layout.simple_list_item_1, nombres);
                    listView.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {}
        });
    }

    private void mostrarDialogoCrear() {
        EditText input = new EditText(this);
        input.setHint("Nombre del nuevo tipo");

        new AlertDialog.Builder(this)
                .setTitle("Nuevo Tipo de Componente")
                .setView(input)
                .setPositiveButton("Crear", (dialog, which) -> {
                    String nombre = input.getText().toString();
                    if (!nombre.isEmpty()) crearCategoria(nombre);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion("Creada desde App");

        ApiClient.getClient(this).create(ApiService.class).crearCategoria(c).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriasActivity.this, "Creada!", Toast.LENGTH_SHORT).show();
                    cargarCategorias();
                } else {
                    Toast.makeText(CategoriasActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {}
        });
    }
}