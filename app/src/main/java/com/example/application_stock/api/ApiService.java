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

    @POST("producto")
    Call<Producto> crearProducto(@Body Producto p);

    @GET("producto/{id}")
    Call<Producto> getProducto(@Path("id") Long id);

    @PUT("producto/{id}")
    Call<Producto> actualizarProducto(@Path("id") Long id, @Body Producto p);

    @DELETE("producto/{id}")
    Call<Void> eliminarProducto(@Path("id") Long id);

    // CATEGORIAS
    @GET("categorias")
    Call<List<Categoria>> getCategorias();

    @POST("categoria")
    Call<Categoria> crearCategoria(@Body Categoria c); // Antes devolvía Call<Producto> y recibía Producto

    @GET("categoria/{id}")
    Call<Categoria> getCategoria(@Path("id") Long id); // Antes devolvía Call<Producto>

    @PUT("categoria/{id}")
    Call<Categoria> actualizarCategoria(@Path("id") Long id, @Body Categoria c); // Antes Call<Producto>

    @DELETE("categoria/{id}")
    Call<Void> eliminarCategoria(@Path("id") Long id);

    @GET("productos/categoria/{id}")
    Call<List<Producto>> getProductosPorCategoria(@Path("id") Long id);
}
