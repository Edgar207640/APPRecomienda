package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.Producto;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class ProductoAdapter extends RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder> {

    public interface OnProductoActionListener {
        void onMeGustaClick(Producto producto, int position);
        void onVerMasClick(Producto producto);
    }

    private List<Producto> productos;
    private Context context;
    private OnProductoActionListener listener;

    public ProductoAdapter(Context context, List<Producto> productos, OnProductoActionListener listener) {
        this.context = context;
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);

        holder.tvNombre.setText(producto.getNombre());
        holder.tvPrecio.setText(String.format(holder.itemView.getContext().getString(R.string.precio_format), producto.getPrice()));
        holder.chipCategoria.setText(producto.getCategoria());
        holder.rbProductoRating.setRating((float) producto.getRating());

        // Anotación: CORRECCIÓN. Usar getThumbnail() en lugar de getImageUrl().
        Glide.with(context)
                .load(producto.getThumbnail())
                .placeholder(R.drawable.placeholder_image) 
                .error(R.drawable.error_image) 
                .into(holder.ivProductoImagen);

        updateMeGustaButton(holder.btnMeGusta, producto.getMeGusta() == 1);

        holder.btnMeGusta.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            Producto p = productos.get(adapterPos);
            if (listener != null) {
                listener.onMeGustaClick(p, adapterPos);
            }
        });

        holder.btnVerMas.setOnClickListener(v -> {
            int adapterPos = holder.getAdapterPosition();
            if (adapterPos == RecyclerView.NO_POSITION) return;
            Producto p = productos.get(adapterPos);
            if (listener != null) {
                listener.onVerMasClick(p);
            }
        });
    }

    private void updateMeGustaButton(MaterialButton button, boolean isLiked) {
        if (isLiked) {
            button.setIconResource(R.drawable.ic_favorite_filled);
        } else {
            button.setIconResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public int getItemCount() {
        return productos != null ? productos.size() : 0;
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductoImagen;
        TextView tvNombre, tvPrecio;
        Chip chipCategoria;
        RatingBar rbProductoRating;
        MaterialButton btnVerMas, btnMeGusta;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductoImagen = itemView.findViewById(R.id.ivProductoImagen);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvPrecio = itemView.findViewById(R.id.tvPrecio);
            chipCategoria = itemView.findViewById(R.id.chipCategoria);
            rbProductoRating = itemView.findViewById(R.id.rbProductoRating);
            btnVerMas = itemView.findViewById(R.id.btnVerMas);
            btnMeGusta = itemView.findViewById(R.id.btnMeGusta);
        }
    }
    
    // --- Métodos para Paginación ---

    public void setProductos(List<Producto> nuevosProductos) {
        this.productos.clear();
        this.productos.addAll(nuevosProductos);
        notifyDataSetChanged();
    }

    public void addProductos(List<Producto> productosAdicionales) {
        int startPosition = this.productos.size();
        this.productos.addAll(productosAdicionales);
        notifyItemRangeInserted(startPosition, productosAdicionales.size());
    }
}
