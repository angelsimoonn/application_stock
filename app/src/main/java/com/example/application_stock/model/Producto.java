package com.example.application_stock.model;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

public class Producto {
    @SerializedName("id")
    private Long id;
    @SerializedName("nombre")
    private String nombre;
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("precio")
    private BigDecimal precio;
    @SerializedName("stock")
    private Integer stock;
    @SerializedName("categoriaId")
    private Long categoriaId;

    public Producto() {
    }

    public Producto(String nombre, String descripcion, BigDecimal precio, Integer stock, Long categoriaId) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
    }

    public Producto(Long id, String nombre, String descripcion, BigDecimal precio, Integer stock, Long categoriaId) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoria) {
        this.categoriaId = categoriaId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
