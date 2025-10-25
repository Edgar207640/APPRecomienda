package com.example.recomendaciones_app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.database.ProductoService;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.ui.activities.ProductoDetalleActivity;
import com.example.recomendaciones_app.ui.adapters.ProductoAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritosFragment extends Fragment implements ProductoAdapter.OnProductoActionListener {

    private RecyclerView recyclerViewFavoritos;
    private ProgressBar progressBarFavoritos;
    private TextView tvEmptyFavoritos;
    private ProductoAdapter productoAdapter;
    private ProductoService productoService;
    private List<Producto> listaDeFavoritos = new ArrayList<>();

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);

        recyclerViewFavoritos = view.findViewById(R.id.recyclerViewFavoritos);
        progressBarFavoritos = view.findViewById(R.id.progressBarFavoritos);
        tvEmptyFavoritos = view.findViewById(R.id.tvEmptyFavoritos);
        productoService = new ProductoService(getContext());

        setupRecyclerView();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void setupRecyclerView() {
        recyclerViewFavoritos.setLayoutManager(new LinearLayoutManager(getContext()));
        productoAdapter = new ProductoAdapter(getContext(), listaDeFavoritos, this);
        recyclerViewFavoritos.setAdapter(productoAdapter);
    }

    private void cargarFavoritos() {
        progressBarFavoritos.setVisibility(View.VISIBLE);
        tvEmptyFavoritos.setVisibility(View.GONE);
        recyclerViewFavoritos.setVisibility(View.GONE);

        databaseExecutor.execute(() -> {
            List<Producto> favoritos = productoService.obtenerPorFavoritos();
            uiHandler.post(() -> {
                progressBarFavoritos.setVisibility(View.GONE);
                listaDeFavoritos.clear();
                if (favoritos != null && !favoritos.isEmpty()) {
                    listaDeFavoritos.addAll(favoritos);
                    recyclerViewFavoritos.setVisibility(View.VISIBLE);
                } else {
                    tvEmptyFavoritos.setVisibility(View.VISIBLE);
                }
                productoAdapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public void onMeGustaClick(Producto producto, int position) {
        // Anotación: CORRECCIÓN. Al quitar un favorito, el estado de "Me Gusta" pasa a ser 0 (false).
        producto.setMeGusta(0);
        databaseExecutor.execute(() -> {
            // Anotación: CORRECCIÓN. Se usa apiId para la operación, y se pasa 'false' para el estado de "Me Gusta".
            productoService.actualizarProductoEstado(producto.getApiId(), producto.getVisto() == 1, false);
        });

        // Actualiza la UI inmediatamente.
        listaDeFavoritos.remove(position);
        productoAdapter.notifyItemRemoved(position);
        productoAdapter.notifyItemRangeChanged(position, listaDeFavoritos.size());

        Toast.makeText(getContext(), R.string.eliminado_de_favoritos, Toast.LENGTH_SHORT).show();

        if (listaDeFavoritos.isEmpty()) {
            tvEmptyFavoritos.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onVerMasClick(Producto producto) {
        Intent intent = new Intent(getActivity(), ProductoDetalleActivity.class);
        intent.putExtra(ProductoDetalleActivity.EXTRA_PRODUCTO, producto);
        startActivity(intent);
    }
}
