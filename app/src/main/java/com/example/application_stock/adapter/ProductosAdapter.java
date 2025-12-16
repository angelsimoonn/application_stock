package com.example.application_stock.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.application_stock.R;
import com.example.application_stock.model.Producto;
import com.example.application_stock.ui.ProductoDetalleActivity;

import java.util.List;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ViewHolder> {

    private List<Producto> lista;

    public ProductosAdapter(List<Producto> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto p = lista.get(position);

        // 1. Nombre
        holder.txtNombre.setText(p.getNombre());

        // 2. Precio (Añadimos símbolo €)
        holder.txtPrecio.setText((p.getPrecio() != null ? p.getPrecio().toString() : "0") + " €");

        // 3. STOCK (NUEVO)
        // Controlamos que no sea null para que no falle
        String stockValor = (p.getStock() != null) ? p.getStock().toString() : "0";
        holder.txtStock.setText("Stock: " + stockValor);

        // Click para ir al detalle
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ProductoDetalleActivity.class);
            intent.putExtra("productoId", p.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, txtPrecio;
        TextView txtStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreItem);
            txtPrecio = itemView.findViewById(R.id.txtPrecioItem);

            // Vincular el nuevo ID del XML
            txtStock = itemView.findViewById(R.id.txtStockItem);
        }
    }
}