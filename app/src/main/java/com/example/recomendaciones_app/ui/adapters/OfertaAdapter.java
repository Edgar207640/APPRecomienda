package com.example.recomendaciones_app.ui.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.model.Producto;

import java.util.List;

public class OfertaAdapter extends RecyclerView.Adapter<OfertaAdapter.OfertaViewHolder> {

    public interface OnOfertaClickListener {
        void onOfertaClick(Producto producto);
    }

    private List<Producto> ofertas;
    private Context context;
    private OnOfertaClickListener listener;

    public OfertaAdapter(Context context, List<Producto> ofertas, OnOfertaClickListener listener) {
        this.context = context;
        this.ofertas = ofertas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OfertaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_oferta, parent, false);
        return new OfertaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OfertaViewHolder holder, int position) {
        Producto oferta = ofertas.get(position);

        holder.tvNombre.setText(oferta.getNombre());

        // Anotación: FUNCIONALIDAD AÑADIDA. Lógica para precios con descuento.
        double originalPrice = oferta.getPrice();
        double discountPercentage = oferta.getDiscountPercentage();
        double discountedPrice = originalPrice * (1 - discountPercentage / 100.0);

        // Asignar y tachar el precio original
        holder.tvPrecioOriginal.setText(String.format(context.getString(R.string.precio_format), originalPrice));
        holder.tvPrecioOriginal.setPaintFlags(holder.tvPrecioOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Asignar el nuevo precio con descuento
        holder.tvPrecioDescuento.setText(String.format(context.getString(R.string.precio_format), discountedPrice));

        Glide.with(context)
                .load(oferta.getThumbnail())
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(holder.ivImagen);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOfertaClick(oferta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ofertas != null ? ofertas.size() : 0;
    }

    public static class OfertaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImagen;
        // Anotación: FUNCIONALIDAD AÑADIDA. Vinculación de los nuevos TextViews de precio.
        TextView tvNombre, tvPrecioOriginal, tvPrecioDescuento;

        public OfertaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImagen = itemView.findViewById(R.id.ivOfertaImagen);
            tvNombre = itemView.findViewById(R.id.tvOfertaNombre);
            tvPrecioOriginal = itemView.findViewById(R.id.tvOfertaPrecioOriginal);
            tvPrecioDescuento = itemView.findViewById(R.id.tvOfertaPrecioDescuento);
        }
    }
}
