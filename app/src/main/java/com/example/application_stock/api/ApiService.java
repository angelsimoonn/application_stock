package com.example.application_stock.api;

import com.example.application_stock.model.Categoria;
import com.example.application_stock.model.Producto;
import com.example.application_stock.model.RegisterRequest;
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

    @PUT("producto/{id}/stock")
    Call<Producto> actualizarStock(@Path("id") Long id, @Query("cantidad") int cantidad);

    @POST("auth/change-password")
    Call<Void> cambiarPassword(@Body com.example.application_stock.model.ChangePasswordRequest request);

    @POST("auth/forgot-password")
    Call<Void> recuperarPassword(@Body java.util.Map<String, String> body);

    @POST("auth/register")
    Call<Map<String, String>> registrar(@Body RegisterRequest request);
    // Nota: El backend devuelve AuthResponse (token), que es un JSON map, así que Map<String,String> nos vale.
}
