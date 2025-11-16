package com.example.application_stock.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application_stock.R;
import com.example.application_stock.model.CategoriaDTO;

import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ViewHolder> {

    private List<CategoriaDTO> categorias;

    public CategoriaAdapter(List<CategoriaDTO> categorias) {
        this.categorias = categorias;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CategoriaDTO categoria = categorias.get(position);
        holder.nombre.setText(categoria.getNombre());
        holder.descripcion.setText(categoria.getDescripcion());
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView nombre, descripcion;

        public ViewHolder(View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textNombre);
            descripcion = itemView.findViewById(R.id.textDescripcion);
        }
    }
}
