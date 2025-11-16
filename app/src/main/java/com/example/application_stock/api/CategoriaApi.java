package com.example.application_stock.api;

import com.example.application_stock.model.CategoriaDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.*;

public interface CategoriaApi {
    @GET("categorias")
    Call<List<CategoriaDTO>> getCategorias();

    @POST("categoria")
    Call<CategoriaDTO> createCategoria(@Body CategoriaDTO categoria);
}
