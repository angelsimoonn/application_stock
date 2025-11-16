package com.example.application_stock.model;

import com.example.application_stock.TipoMovimiento;

import java.time.LocalDateTime;

public class MovimientoDTO {
    private String descripcion;
    private TipoMovimiento tipoMovimiento;
    private LocalDateTime fecha;

    public MovimientoDTO() {
    }

    public MovimientoDTO(String descripcion, TipoMovimiento tipoMovimiento, LocalDateTime fecha) {
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
