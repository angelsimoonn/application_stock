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

public class ProductoCrearActivity extends AppCompatActivity {

    EditText txtNombre, txtDescripcion, txtPrecio, txtStock;
    Spinner spinnerCategoria;
    Button btnCrear;

    List<Categoria> categorias;
    Long categoriaSeleccionadaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_crear);

        txtNombre = findViewById(R.id.txtNombreCrear);
        txtDescripcion = findViewById(R.id.txtDescripcionCrear);
        txtPrecio = findViewById(R.id.txtPrecioCrear);
        txtStock = findViewById(R.id.txtStockCrear);
        spinnerCategoria = findViewById(R.id.spinnerCategoriaCrear);
        btnCrear = findViewById(R.id.btnCrearProducto);

        cargarCategorias();

        btnCrear.setOnClickListener(v -> crearProducto());
    }

    private void cargarCategorias() {
        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        api.getCategorias().enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    categorias = response.body();

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            ProductoCrearActivity.this,
                            android.R.layout.simple_spinner_item,
                            categorias.stream().map(c -> c.getNombre()).toArray(String[]::new)
                    );

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCategoria.setAdapter(adapter);

                    spinnerCategoria.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                            categoriaSeleccionadaId = categorias.get(position).getId();
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

    private void crearProducto() {
        Producto p = new Producto();
        p.setNombre(txtNombre.getText().toString());
        p.setDescripcion(txtDescripcion.getText().toString());
        p.setPrecio(new BigDecimal(txtPrecio.getText().toString()));
        p.setStock(Integer.parseInt(txtStock.getText().toString()));

        p.setCategoriaId(categoriaSeleccionadaId);

        ApiService api = ApiClient.getClient(this).create(ApiService.class);

        api.crearProducto(p).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ProductoCrearActivity.this, "Producto creado", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {}
        });
    }
}
