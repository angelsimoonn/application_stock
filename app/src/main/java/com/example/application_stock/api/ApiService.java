package com.example.application_stock.api;

import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;
import com.example.application_stock.model.Usuario;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // LOGIN
    @POST("auth/login")
    Call<Map<String, String>> login(@Body Usuario usuario);

    // PRODUCTOS
    @GET("productos")
    Call<List<Producto>> getProductos();

    @POST("productos")
    Call<Producto> crearProducto(@Body Producto p);

    @GET("productos/{id}")
    Call<Producto> getProducto(@Path("id") Long id);

    @PUT("productos/{id}")
    Call<Producto> actualizarProducto(@Path("id") Long id, @Body Producto p);

    @DELETE("productos/{id}")
    Call<Void> eliminarProducto(@Path("id") Long id);

    // CATEGORIAS
    @GET("categorias")
    Call<List<Categoria>> getCategorias();
}
