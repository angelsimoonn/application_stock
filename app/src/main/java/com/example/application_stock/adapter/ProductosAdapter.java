package com.example.application_stock.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.application_stock.R;
import com.example.application_stock.api.ApiClient;
import com.example.application_stock.api.ApiService;
import com.example.application_stock.model.Producto;
import com.example.application_stock.ui.ProductoDetalleActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ViewHolder> {

    // Constantes para el tipo de vista
    public static final int VIEW_TYPE_LIST = 0;
    public static final int VIEW_TYPE_GRID = 1;

    private List<Producto> listaOriginal;
    private List<Producto> listaFiltrada;
    private boolean isGridMode = false; // Controla si es cuadricula

    public ProductosAdapter(List<Producto> lista) {
        this.listaOriginal = lista;
        this.listaFiltrada = new ArrayList<>(lista);
    }

    // --- CAMBIAR MODO DE VISTA ---
    public void setGridMode(boolean isGrid) {
        this.isGridMode = isGrid;
        notifyDataSetChanged(); // Recargamos todo el recycler
    }

    // --- ORDENACIÓN ---
    public void ordenar(int tipoOrden) {
        // 0: Nombre A-Z, 1: Precio Asc, 2: Precio Desc, 3: Stock Asc, 4: Stock Desc
        switch (tipoOrden) {
            case 0: // Nombre A-Z
                Collections.sort(listaFiltrada, Comparator.comparing(Producto::getNombre, String.CASE_INSENSITIVE_ORDER));
                break;
            case 1: // Precio Asc
                Collections.sort(listaFiltrada, Comparator.comparing(Producto::getPrecio));
                break;
            case 2: // Precio Desc
                Collections.sort(listaFiltrada, (p1, p2) -> p2.getPrecio().compareTo(p1.getPrecio()));
                break;
            case 3: // Stock Asc
                Collections.sort(listaFiltrada, Comparator.comparingInt(Producto::getStock));
                break;
            case 4: // Stock Desc
                Collections.sort(listaFiltrada, (p1, p2) -> p2.getStock().compareTo(p1.getStock()));
                break;
        }
        notifyDataSetChanged();
    }

    // --- FILTRADO ---
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

    // Método clave para decidir qué XML cargar
    @Override
    public int getItemViewType(int position) {
        return isGridMode ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = (viewType == VIEW_TYPE_GRID) ? R.layout.item_producto_grid : R.layout.item_producto;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Producto p = listaFiltrada.get(position);

        holder.txtNombre.setText(p.getNombre());
        holder.txtPrecio.setText((p.getPrecio() != null ? p.getPrecio().toString() : "0") + " €");
        holder.txtStock.setText(p.getStock() != null ? p.getStock().toString() : "0");

        // --- EFECTO VISUAL STOCK 0 ---
        if (p.getStock() != null && p.getStock() == 0) {
            holder.itemView.setAlpha(0.6f); // Más transparente
            ColorMatrix matrix = new ColorMatrix();
            matrix.setSaturation(0); // Blanco y negro
            holder.imgProducto.setColorFilter(new ColorMatrixColorFilter(matrix));
        } else {
            holder.itemView.setAlpha(1.0f); // Opacidad normal
            holder.imgProducto.setColorFilter(null); // Sin filtro
        }

        // IMAGEN
        if (p.getImagen() != null && !p.getImagen().isEmpty()) {
            try {
                byte[] imageBytes = Base64.decode(p.getImagen(), Base64.DEFAULT);
                Glide.with(holder.itemView.getContext())
                        .load(imageBytes)
                        .centerCrop() // Importante para que se vea bien en cuadricula
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_delete)
                        .into(holder.imgProducto);
            } catch (Exception e) {
                holder.imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } else {
            holder.imgProducto.setImageResource(android.R.drawable.ic_menu_gallery);
        }

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

        holder.btnMas.setOnClickListener(v -> actualizarStock(v.getContext(), p, 1, holder));
    }

    private void actualizarStock(Context context, Producto p, int cantidad, ViewHolder holder) {
        ApiService api = ApiClient.getClient(context).create(ApiService.class);
        api.actualizarStock(p.getId(), cantidad).enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Producto actualizado = response.body();
                    p.setStock(actualizado.getStock());

                    int idx = listaOriginal.indexOf(p);
                    if (idx != -1) listaOriginal.get(idx).setStock(actualizado.getStock());

                    // Forzar re-bind de este elemento para que se aplique el efecto gris si baja a 0
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(context, "Error red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    // Como usamos los mismos IDs en ambos XML, solo necesitamos un ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtPrecio, txtStock;
        ImageView imgProducto;
        ImageButton btnMas, btnMenos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombreItem);
            txtPrecio = itemView.findViewById(R.id.txtPrecioItem);
            txtStock = itemView.findViewById(R.id.txtStockItem);
            imgProducto = itemView.findViewById(R.id.imgProductoItem);
            btnMas = itemView.findViewById(R.id.btnMasStock);
            btnMenos = itemView.findViewById(R.id.btnMenosStock);
        }
    }
}