package com.example.application_stock.model;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;

public class Movimiento {
    @SerializedName("descripcion")
    private String descripcion;
    @SerializedName("tipoMovimiento")
    private TipoMovimiento tipoMovimiento;
    @SerializedName("fecha")
    private LocalDateTime fecha;

    public Movimiento() {
    }

    public Movimiento(String descripcion, TipoMovimiento tipoMovimiento, LocalDateTime fecha) {
        this.descripcion = descripcion;
        this.tipoMovimiento = tipoMovimiento;
        this.fecha = fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
