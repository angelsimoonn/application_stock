package com.example.application_stock.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.application_stock.R;


public class MainMenuActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_principal);

        findViewById(R.id.cardProductos).setOnClickListener(v ->
                startActivity(new Intent(this, ProductosActivity.class))
        );


/* Falta por implementar
        findViewById(R.id.cardAjustes).setOnClickListener(v ->

        );*/


    }
}
