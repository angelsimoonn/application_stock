package com.example.application_stock.model;

public class RegisterRequest {
    private String nombre;
    private String password;
    private String email;

    public RegisterRequest(String nombre, String password, String email) {
        this.nombre = nombre;
        this.password = password;
        this.email = email;
    }
}