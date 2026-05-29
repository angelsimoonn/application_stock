package com.example.application_stock.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;
import com.example.application_stock.adapter.CategoriaAdapter;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Categoria;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriasActivity extends AppCompatActivity {

    ListView listaCategorias;
    FloatingActionButton btnAdd;
    CategoriaAdapter adapter;
    List<Categoria> categorias = new ArrayList<>();
    ApiService api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);

        listaCategorias = findViewById(R.id.listaCategorias);
        btnAdd = findViewById(R.id.btnAddCategoria);

        api = ApiClient.getClient(this).create(ApiService.class);

        adapter = new CategoriaAdapter(this, categorias, new CategoriaAdapter.OnCategoriaListener() {
            @Override
            public void onEditar(Categoria categoria) {
                mostrarDialogoEditar(categoria);
            }
            @Override
            public void onEliminar(Categoria categoria) {
                confirmarEliminar(categoria);
            }
        });

        listaCategorias.setAdapter(adapter);
        btnAdd.setOnClickListener(v -> mostrarDialogoCrear());
        cargarCategorias();
    }

    private void cargarCategorias() {
        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categorias.clear();
                    categorias.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(CategoriasActivity.this, getString(R.string.error_msg, response.code()), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Toast.makeText(CategoriasActivity.this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoCrear() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_crear_categoria, null);
        TextInputEditText edtNombre = dialogView.findViewById(R.id.edtNombreCategoria);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarCategoria);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.new_category)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        btnConfirmar.setOnClickListener(v -> {
            String nombre = edtNombre.getText() != null ? edtNombre.getText().toString().trim() : "";
            if (!nombre.isEmpty()) {
                crearCategoria(nombre);
                dialog.dismiss();
            } else {
                edtNombre.setError(getString(R.string.name_empty_error));
            }
        });

        dialog.show();
    }

    private void mostrarDialogoEditar(Categoria categoria) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_crear_categoria, null);
        TextInputEditText edtNombre = dialogView.findViewById(R.id.edtNombreCategoria);
        Button btnConfirmar = dialogView.findViewById(R.id.btnConfirmarCategoria);

        edtNombre.setText(categoria.getNombre());
        btnConfirmar.setText(R.string.save_changes);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.edit_category)
                .setView(dialogView)
                .setNegativeButton(R.string.cancel, null)
                .create();

        btnConfirmar.setOnClickListener(v -> {
            String nombre = edtNombre.getText() != null ? edtNombre.getText().toString().trim() : "";
            if (!nombre.isEmpty()) {
                categoria.setNombre(nombre);
                editarCategoria(categoria);
                dialog.dismiss();
            } else {
                edtNombre.setError(getString(R.string.name_empty_error));
            }
        });

        dialog.show();
    }

    private void confirmarEliminar(Categoria categoria) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_category)
                .setMessage(getString(R.string.delete_confirm, categoria.getNombre()))
                .setPositiveButton(R.string.delete, (d, w) -> eliminarCategoria(categoria))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void crearCategoria(String nombre) {
        Categoria c = new Categoria();
        c.setNombre(nombre);
        c.setDescripcion("Created from App");

        api.crearCategoria(c).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriasActivity.this, R.string.category_created, Toast.LENGTH_SHORT).show();
                    cargarCategorias();
                } else {
                    Toast.makeText(CategoriasActivity.this, getString(R.string.error_msg, response.code()), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                Toast.makeText(CategoriasActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editarCategoria(Categoria categoria) {
        api.actualizarCategoria(categoria.getId(), categoria).enqueue(new Callback<Categoria>() {
            @Override
            public void onResponse(Call<Categoria> call, Response<Categoria> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriasActivity.this, R.string.category_updated, Toast.LENGTH_SHORT).show();
                    cargarCategorias();
                } else {
                    Toast.makeText(CategoriasActivity.this, getString(R.string.error_msg, response.code()), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Categoria> call, Throwable t) {
                Toast.makeText(CategoriasActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void eliminarCategoria(Categoria categoria) {
        api.eliminarCategoria(categoria.getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(CategoriasActivity.this, R.string.category_deleted, Toast.LENGTH_SHORT).show();
                    cargarCategorias();
                } else {
                    Toast.makeText(CategoriasActivity.this, getString(R.string.error_msg, response.code()), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(CategoriasActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
