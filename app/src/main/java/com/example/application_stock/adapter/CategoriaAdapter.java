package com.example.application_stock.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.application_stock.R;
import com.example.application_stock.model.Categoria;

import java.util.List;

public class CategoriaAdapter extends ArrayAdapter<Categoria> {

    public interface OnCategoriaListener {
        void onEditar(Categoria categoria);
        void onEliminar(Categoria categoria);
    }

    private final OnCategoriaListener listener;

    public CategoriaAdapter(Context context, List<Categoria> categorias, OnCategoriaListener listener) {
        super(context, 0, categorias);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_categoria, parent, false);
        }

        Categoria categoria = getItem(position);

        TextView txtNombre = convertView.findViewById(R.id.txtNombreCategoria);
        ImageButton btnEditar = convertView.findViewById(R.id.btnEditarCategoria);
        ImageButton btnEliminar = convertView.findViewById(R.id.btnEliminarCategoria);

        txtNombre.setText(categoria.getNombre());
        btnEditar.setOnClickListener(v -> listener.onEditar(categoria));
        btnEliminar.setOnClickListener(v -> listener.onEliminar(categoria));

        return convertView;
    }
}