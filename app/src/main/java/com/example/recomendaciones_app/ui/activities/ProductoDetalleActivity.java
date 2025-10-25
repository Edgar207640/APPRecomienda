package com.example.recomendaciones_app.ui.activities;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.database.ProductoService;
import com.example.recomendaciones_app.data.manager.CarritoManager;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.ui.adapters.ImageSliderAdapter;
import com.example.recomendaciones_app.ui.adapters.ReviewAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductoDetalleActivity extends AppCompatActivity {

    public static final String EXTRA_PRODUCTO = "extra_producto";
    private Producto producto;
    private MaterialButton btnMeGusta;
    private ProductoService productoService;
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_detalle);

        productoService = new ProductoService(this);

        Toolbar toolbar = findViewById(R.id.toolbarDetalle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Vinculación de vistas
        ViewPager2 viewPagerImages = findViewById(R.id.viewPagerImages);
        TabLayout tabLayoutDots = findViewById(R.id.tabLayoutDots);
        TextView tvNombre = findViewById(R.id.tvDetalleNombre);
        TextView tvBrand = findViewById(R.id.tvDetalleBrand);
        RatingBar rbRating = findViewById(R.id.rbDetalleRating);
        TextView tvRatingValue = findViewById(R.id.tvDetalleRatingValue);
        TextView tvDescripcion = findViewById(R.id.tvDetalleDescripcion);
        btnMeGusta = findViewById(R.id.btnMeGustaDetalle);
        MaterialButton btnAnadirCarrito = findViewById(R.id.btnAnadirAlCarrito);
        TextView tvTituloOpiniones = findViewById(R.id.tvTituloOpiniones);
        RecyclerView recyclerViewReviews = findViewById(R.id.recyclerViewReviews);

        // Anotación: FUNCIONALIDAD AÑADIDA. Vinculación de los nuevos TextViews de precio.
        TextView tvPrecio = findViewById(R.id.tvDetallePrecio);
        TextView tvPrecioOriginal = findViewById(R.id.tvDetallePrecioOriginal);

        producto = (Producto) getIntent().getSerializableExtra(EXTRA_PRODUCTO);

        if (producto != null) {
            getSupportActionBar().setTitle(producto.getNombre());
            tvNombre.setText(producto.getNombre());
            tvBrand.setText(producto.getBrand());
            rbRating.setRating((float) producto.getRating());
            tvRatingValue.setText(String.valueOf(producto.getRating()));
            tvDescripcion.setText(producto.getDescripcion());

            // Anotación: FUNCIONALIDAD AÑADIDA. Lógica para mostrar precios de oferta.
            setupPrecio();

            if (producto.getImages() != null && !producto.getImages().isEmpty()) {
                ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(this, producto.getImages());
                viewPagerImages.setAdapter(sliderAdapter);
                new TabLayoutMediator(tabLayoutDots, viewPagerImages, (tab, position) -> {}).attach();
            }

            if (producto.getReviews() != null && !producto.getReviews().isEmpty()) {
                tvTituloOpiniones.setVisibility(View.VISIBLE);
                recyclerViewReviews.setVisibility(View.VISIBLE);
                recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
                ReviewAdapter reviewAdapter = new ReviewAdapter(this, producto.getReviews());
                recyclerViewReviews.setAdapter(reviewAdapter);
            } else {
                tvTituloOpiniones.setVisibility(View.GONE);
                recyclerViewReviews.setVisibility(View.GONE);
            }

            actualizarBotonMeGusta();
            btnMeGusta.setOnClickListener(v -> onMeGustaClick());
            btnAnadirCarrito.setOnClickListener(v -> {
                CarritoManager.getInstance().addProduct(producto);
                Toast.makeText(this, R.string.producto_anadido_al_carrito, Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Anotación: FUNCIONALIDAD AÑADIDA. Método para gestionar la lógica de precios.
    private void setupPrecio() {
        TextView tvPrecio = findViewById(R.id.tvDetallePrecio);
        TextView tvPrecioOriginal = findViewById(R.id.tvDetallePrecioOriginal);

        double originalPrice = producto.getPrice();
        double discountPercentage = producto.getDiscountPercentage();

        if (discountPercentage > 0) {
            // Si hay descuento
            double finalPrice = originalPrice * (1 - discountPercentage / 100.0);

            // Mostrar precio original tachado
            tvPrecioOriginal.setText(String.format(getString(R.string.precio_format), originalPrice));
            tvPrecioOriginal.setPaintFlags(tvPrecioOriginal.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tvPrecioOriginal.setVisibility(View.VISIBLE);

            // Mostrar precio final con descuento
            tvPrecio.setText(String.format(getString(R.string.precio_format), finalPrice));
        } else {
            // Si no hay descuento
            tvPrecioOriginal.setVisibility(View.GONE);
            tvPrecio.setText(String.format(getString(R.string.precio_format), originalPrice));
        }
    }

    private void onMeGustaClick() {
        if (producto == null) return;
        boolean nuevoEstado = producto.getMeGusta() == 0;
        producto.setMeGusta(nuevoEstado ? 1 : 0);
        actualizarBotonMeGusta();
        databaseExecutor.execute(() -> {
            productoService.actualizarProductoEstado(producto.getApiId(), producto.getVisto() == 1, nuevoEstado);
        });
    }

    private void actualizarBotonMeGusta() {
        if (producto == null) return;
        if (producto.getMeGusta() == 1) {
            btnMeGusta.setIconResource(R.drawable.ic_favorite_filled);
        } else {
            btnMeGusta.setIconResource(R.drawable.ic_favorite_border);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
