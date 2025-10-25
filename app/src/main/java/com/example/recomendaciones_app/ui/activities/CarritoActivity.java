package com.example.recomendaciones_app.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recomendaciones_app.R;
import com.example.recomendaciones_app.data.manager.CarritoManager;
import com.example.recomendaciones_app.data.model.Producto;
import com.example.recomendaciones_app.ui.adapters.CarritoAdapter;
import com.example.recomendaciones_app.services.VentasServiceCompat;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CarritoActivity extends AppCompatActivity implements CarritoAdapter.OnCarritoActionListener {

    private RecyclerView recyclerViewCarrito;
    private TextView tvCarritoVacio, tvTotalAmount;
    private MaterialButton btnComprarAhora;
    private CarritoAdapter carritoAdapter;
    private List<Producto> listaDeCarrito;

    private VentasServiceCompat ventasService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        Toolbar toolbar = findViewById(R.id.toolbarCarrito);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerViewCarrito = findViewById(R.id.recyclerViewCarrito);
        tvCarritoVacio = findViewById(R.id.tvCarritoVacio);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnComprarAhora = findViewById(R.id.btnComprarAhora);
        MaterialButton btnTestWebhook = findViewById(R.id.btnTestWebhook);

        // Obtener items del carrito y asegurar inicialización
        listaDeCarrito = CarritoManager.getInstance().getCartItems();
        if (listaDeCarrito == null) {
            listaDeCarrito = new ArrayList<>();
        }

        // Debug: mostrar tamaño inicial del carrito
        android.util.Log.d("CarritoActivity", "onCreate - carrito size: " + listaDeCarrito.size());

        // Inicializar VentasServiceCompat (wrapper que delega a Kotlin VentasService)
        ventasService = new VentasServiceCompat();

        setupRecyclerView();
        actualizarEstadoCarrito();

        btnComprarAhora.setOnClickListener(v -> {
            // Log/Toast rápido para confirmar que se inició el flujo de ventas
            android.util.Log.d("CarritoActivity", "Botón Comprar ahora pulsado - iniciando notificación de ventas a n8n");
            Toast.makeText(this, "Procesando compra...", Toast.LENGTH_SHORT).show();
            if (listaDeCarrito == null || listaDeCarrito.isEmpty()) {
                Toast.makeText(this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                android.util.Log.w("CarritoActivity", "Intento de compra con carrito vacío");
                return;
            }

            try {
                List<Producto> productosACobrar = new ArrayList<>(listaDeCarrito);
                android.util.Log.d("CarritoActivity", "productosACobrar size: " + productosACobrar.size());

                for (Producto p : productosACobrar) {
                    VentasServiceCompat.VentaData ventaData = new VentasServiceCompat.VentaData(
                            p.getApiId(),
                            p.getNombre(),
                            1,
                            p.getPrice(),
                            p.getPrice() * 1,
                            getCurrentUserId(),
                            VentasServiceCompat.getCurrentFormattedDate(),
                            "Desconocido"
                    );

                    // Llamamos al wrapper Java
                    ventasService.notificarVenta(ventaData, (success, message) -> runOnUiThread(() -> {
                        if (success) {
                            Toast.makeText(CarritoActivity.this, "Venta enviada a n8n", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CarritoActivity.this, "Error enviando ventas a n8n: " + message, Toast.LENGTH_LONG).show();
                        }
                        // Limpiar carrito y actualizar UI (siempre en hilo UI)
                        int oldSize = listaDeCarrito != null ? listaDeCarrito.size() : 0;
                        CarritoManager.getInstance().clearCart();
                        listaDeCarrito = CarritoManager.getInstance().getCartItems();
                        if (listaDeCarrito == null) listaDeCarrito = new ArrayList<>();
                        if (carritoAdapter != null && oldSize > 0) {
                            carritoAdapter.notifyItemRangeRemoved(0, oldSize);
                        }
                        actualizarEstadoCarrito();
                    }));
                }
            } catch (Exception ex) {
                android.util.Log.e("CarritoActivity", "Excepción en onClick ComprarAhora", ex);
                Toast.makeText(this, "Error procesando la compra: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        btnTestWebhook.setOnClickListener(v -> {
            try {
                android.util.Log.d("CarritoActivity", "Test webhook pulsado - enviando payload de prueba");
                VentasServiceCompat.VentaData ventaTest = new VentasServiceCompat.VentaData(
                        12345,
                        "Producto de prueba",
                        1,
                        9.99,
                        9.99,
                        "test_user",
                        VentasServiceCompat.getCurrentFormattedDate(),
                        "Test"
                );

                ventasService.notificarVenta(ventaTest, (success, message) -> runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(CarritoActivity.this, "Test webhook OK", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(CarritoActivity.this, "Test webhook ERROR: " + message, Toast.LENGTH_LONG).show();
                    }
                }));
            } catch (Exception ex) {
                android.util.Log.e("CarritoActivity", "Error enviando test webhook", ex);
                Toast.makeText(this, "Error enviando test webhook: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerViewCarrito.setLayoutManager(new LinearLayoutManager(this));
        carritoAdapter = new CarritoAdapter(this, listaDeCarrito, this);
        recyclerViewCarrito.setAdapter(carritoAdapter);
    }

    private void actualizarEstadoCarrito() {
        // Debug: registrar estado del carrito antes de actualizar UI
        android.util.Log.d("CarritoActivity", "actualizarEstadoCarrito - size: " + (listaDeCarrito == null ? 0 : listaDeCarrito.size()));
        if (listaDeCarrito == null || listaDeCarrito.isEmpty()) {
            tvCarritoVacio.setVisibility(View.VISIBLE);
            recyclerViewCarrito.setVisibility(View.GONE);
            tvTotalAmount.setText(String.format(getString(R.string.precio_format), 0.0));
            btnComprarAhora.setEnabled(false);
        } else {
            tvCarritoVacio.setVisibility(View.GONE);
            recyclerViewCarrito.setVisibility(View.VISIBLE);
            btnComprarAhora.setEnabled(true);
            calcularTotal();
        }
        // Removed notifyDataSetChanged to use more specific notifications elsewhere
    }

    private void calcularTotal() {
        double total = 0;
        if (listaDeCarrito != null) {
            for (Producto item : listaDeCarrito) {
                total += item.getPrice();
            }
        }
        tvTotalAmount.setText(String.format(getString(R.string.precio_format), total));
    }

    @Override
    public void onRemoveItem(Producto item, int position) {
        if (listaDeCarrito != null && position >= 0 && position < listaDeCarrito.size()) {
            listaDeCarrito.remove(position);
            if (carritoAdapter != null) {
                carritoAdapter.notifyItemRemoved(position);
            }
        }
        actualizarEstadoCarrito();
        Toast.makeText(this, "Producto eliminado del carrito", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemClick(Producto producto) {
        Intent intent = new Intent(this, ProductoDetalleActivity.class);
        intent.putExtra(ProductoDetalleActivity.EXTRA_PRODUCTO, producto);
        startActivity(intent);
    }

    private String getCurrentUserId() {
        // Devuelve un id por defecto; reemplazar por implementación real si aplica
        return "guest_user";
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
