package com.example.application_stock.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Base64; // IMPORTANTE
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView; // IMPORTANTE
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // IMPORTANTE
import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Producto;
import com.example.application_stock.ui.ProductoDetalleActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ViewHolder> {

    private List<Producto> listaOriginal;
    private List<Producto> listaFiltrada;

    public ProductosAdapter(List<Producto> lista) {
        this.listaOriginal = lista;
        this.listaFiltrada = new ArrayList<>(lista);
    }

    public void filtrar(String textoBusqueda) {
        if (textoBusqueda.length() == 0) {
            listaFiltrada.clear();
            listaFiltrada.addAll(listaOriginal);
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                List<Producto> coleccion = listaOriginal.stream()
                        .filter(i -> i.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase()))
                        .collect(Collectors.toList());
                listaFiltrada.clear();
                listaFiltrada.addAll(coleccion);
            } else {
                listaFiltrada.clear();
                for (Producto p : listaOriginal) {
                    if (p.getNombre().toLowerCase().contains(textoBusqueda.toLowerCase())) {
                        listaFiltrada.add(p);
                    }
                }
            }
        }
        notifyDataSetChanged();
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
        Producto p = listaFiltrada.get(position);

        holder.txtNombre.setText(p.getNombre());
        holder.txtPrecio.setText((p.getPrecio() != null ? p.getPrecio().toString() : "0") + " €");
        holder.txtStock.setText(p.getStock() != null ? p.getStock().toString() : "0");

        // --- LÓGICA PARA CARGAR IMAGEN ---
        if (p.getImagen() != null && !p.getImagen().isEmpty()) {
            try {
                // Decodificamos el Base64 a Bytes
                byte[] imageBytes = Base64.decode(p.getImagen(), Base64.DEFAULT);

                // Usamos Glide para cargar los bytes en el ImageView
                Glide.with(holder.itemView.getContext())
                        .load(imageBytes)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(holder.imgProducto);

            } catch (IllegalArgumentException e) {
                // Si el Base64 está corrupto, ponemos imagen por defecto
                holder.imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            // Si no tiene imagen, ponemos el icono por defecto
            holder.imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        // ---------------------------------

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, ProductoDetalleActivity.class);
            intent.putExtra("productoId", p.getId());
            context.startActivity(intent);
        });

        holder.btnMenos.setOnClickListener(v -> {
            int stockActual = p.getStock() != null ? p.getStock() : 0;
            if (stockActual > 0) {
                actualizarStock(v.getContext(), p, -1, holder);
            }
        });

        holder.btnMas.setOnClickListener(v -> {
            actualizarStock(v.getContext(), p, 1, holder);
        });
    }

    private void actualizarStock(Context context, Producto p, int cantidad, ViewHolder holder) {
        ApiService api = ApiClient.getClient(context).create(ApiService.class);

        api.actualizarStock(p.getId(), cantidad).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Producto actualizado = response.body();
                    p.setStock(actualizado.getStock());

                    // Actualizamos también en la lista original
                    int index = listaOriginal.indexOf(p);
                    if(index != -1) listaOriginal.get(index).setStock(actualizado.getStock());

                    holder.txtStock.setText(String.valueOf(actualizado.getStock()));
                } else {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtNombre, txtPrecio, txtStock;
        ImageView imgProducto; // <--- NUEVO
        ImageButton btnMas, btnMenos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreItem);
            txtPrecio = itemView.findViewById(R.id.txtPrecioItem);
            txtStock = itemView.findViewById(R.id.txtStockItem);
            imgProducto = itemView.findViewById(R.id.imgProductoItem); // <--- VINCULACIÓN
            btnMas = itemView.findViewById(R.id.btnMasStock);
            btnMenos = itemView.findViewById(R.id.btnMenosStock);
        }
    }
}