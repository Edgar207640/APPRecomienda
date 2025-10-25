package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.Producto;

import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {

    public interface OnCarritoActionListener {
        void onRemoveItem(Producto item, int position);
        // Anotación: FUNCIONALIDAD AÑADIDA. Método para notificar el clic en un item.
        void onItemClick(Producto producto);
    }

    private List<Producto> carritoItems;
    private Context context;
    private OnCarritoActionListener listener;

    public CarritoAdapter(Context context, List<Producto> carritoItems, OnCarritoActionListener listener) {
        this.context = context;
        this.carritoItems = carritoItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        Producto item = carritoItems.get(position);

        holder.tvNombre.setText(item.getNombre());
        holder.tvPrecio.setText(String.format(context.getString(R.string.precio_format), item.getPrice()));
        holder.tvCantidad.setVisibility(View.GONE);

        Glide.with(context)
                .load(item.getThumbnail())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.ivProducto);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveItem(item, position);
            }
        });

        // Anotación: FUNCIONALIDAD AÑADIDA. Listener para toda la fila.
        holder.itemView.setOnClickListener(v -> {
            if(listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return carritoItems != null ? carritoItems.size() : 0;
    }

    public static class CarritoViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProducto;
        TextView tvNombre, tvPrecio, tvCantidad;
        ImageButton btnRemove;

        public CarritoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProducto = itemView.findViewById(R.id.ivCarritoProducto);
            tvNombre = itemView.findViewById(R.id.tvCarritoNombre);
            tvPrecio = itemView.findViewById(R.id.tvCarritoPrecioUnitario);
            tvCantidad = itemView.findViewById(R.id.tvCarritoCantidad);
            btnRemove = itemView.findViewById(R.id.btnRemoveItem);
        }
    }
}
