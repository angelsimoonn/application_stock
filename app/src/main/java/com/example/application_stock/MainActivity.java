package com.example.application_stock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.application_stock.adapter.CategoriaAdapter;
import com.example.application_stock.api.CategoriaApi;
import com.example.application_stock.model.CategoriaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CategoriaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerCategorias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.139:8081/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CategoriaApi api = retrofit.create(CategoriaApi.class);

        api.getCategorias().enqueue(new Callback<List<CategoriaDTO>>() {
            @Override
            public void onResponse(Call<List<CategoriaDTO>> call, Response<List<CategoriaDTO>> response) {
                if (response.isSuccessful()) {
                    List<CategoriaDTO> lista = response.body();
                    Log.d("API", "Recibido: " + lista);
                    adapter = new CategoriaAdapter(lista);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("API", "Respuesta fallida: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<CategoriaDTO>> call, Throwable t) {
                Log.e("API", "Error: " + t.getMessage());
            }
        });
    }
}
