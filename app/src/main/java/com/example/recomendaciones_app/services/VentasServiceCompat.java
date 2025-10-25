package com.example.recomendaciones_app.services;

import androidx.annotation.NonNull;
import com.example.recomendaciones_app.data.network.VentasService;
/**
 * Wrapper Java para delegar en la implementación Kotlin `VentasService`.
 * Proporciona una API Java-friendly que las Activities Java pueden usar sin problemas
 * incluso si hay desincronización momentánea entre el compilador Java/Kotlin.
 */
public class VentasServiceCompat {

    public interface VentaCallback {
        void onResult(boolean success, String message);
    }

    public static class VentaData {
        public int producto_id;
        public String producto_nombre;
        public int cantidad;
        public double precio_unitario;
        public double total;
        public String usuario_id; // nullable
        public String fecha; // yyyy-MM-dd HH:mm:ss
        public String metodo_pago;

        public VentaData(int producto_id, String producto_nombre, int cantidad, double precio_unitario,
                         double total, String usuario_id, String fecha, String metodo_pago) {
            this.producto_id = producto_id;
            this.producto_nombre = producto_nombre;
            this.cantidad = cantidad;
            this.precio_unitario = precio_unitario;
            this.total = total;
            this.usuario_id = usuario_id;
            this.fecha = fecha;
            this.metodo_pago = metodo_pago;
        }
    }

    private final VentasService kotlinService;

    public VentasServiceCompat() {
        kotlinService = new VentasService();
    }

    public static String getCurrentFormattedDate() {
        return VentasService.getCurrentFormattedDate();
    }

    public void notificarVenta(final VentaData venta, final VentaCallback callback) {
        // Construir el objeto Kotlin VentasService.VentaData
        VentasService.VentaData kotlinData = new VentasService.VentaData(
                venta.producto_id,
                venta.producto_nombre,
                venta.cantidad,
                venta.precio_unitario,
                venta.total,
                venta.usuario_id,
                venta.fecha,
                venta.metodo_pago
        );

        // Llamada usando clase anónima para máxima compatibilidad
        kotlinService.notificarVentaJava(kotlinData, new VentasService.VentaCallbackJava() {
            @Override
            public void onResult(boolean success, @NonNull String message) {
                if (callback != null) callback.onResult(success, message);
            }
        });
    }
}
