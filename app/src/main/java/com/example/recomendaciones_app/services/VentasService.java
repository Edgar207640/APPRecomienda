package com.example.recomendaciones_app.services;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * DEPRECATED: Implementación Java de notificador de ventas.
 * Se mantiene solo por compatibilidad pero la implementación recomendada
 * es la clase Kotlin `VentasService` en el mismo paquete.
 */
@Deprecated
class VentasServiceJava {

    // Cambia por tu webhook real (sin espacios)
    public static String WEBHOOK_URL = "https://primary-production-2873d.up.railway.app/webhook/nueva-venta";

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

    public VentasServiceJava() {
    }

    public static String getCurrentFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    /**
     * Envía la venta al webhook en background y reporta resultado por callback.
     */
    public void notificarVenta(final VentaData venta, final VentaCallback callback) {
        new Thread(() -> {
            HttpURLConnection conn = null;
            try {
                JSONObject payload = new JSONObject();
                payload.put("producto_id", venta.producto_id);
                payload.put("producto_nombre", venta.producto_nombre);
                payload.put("cantidad", venta.cantidad);
                payload.put("precio_unitario", venta.precio_unitario);
                payload.put("total", venta.total);
                payload.put("usuario_id", venta.usuario_id == null ? JSONObject.NULL : venta.usuario_id);
                payload.put("fecha", venta.fecha);
                payload.put("metodo_pago", venta.metodo_pago);

                URL url = new URL(WEBHOOK_URL);
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conn.setDoOutput(true);

                byte[] out = payload.toString().getBytes("UTF-8");
                try (OutputStream os = new BufferedOutputStream(conn.getOutputStream())) {
                    os.write(out);
                    os.flush();
                }

                int status = conn.getResponseCode();
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        status >= 200 && status < 300 ? conn.getInputStream() : conn.getErrorStream()
                ));
                StringBuilder resp = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    resp.append(line);
                }
                reader.close();

                if (status >= 200 && status < 300) {
                    Log.d("VentasServiceJava", "Webhook enviado correctamente: " + resp.toString());
                    if (callback != null) callback.onResult(true, resp.toString());
                } else {
                    Log.w("VentasServiceJava", "Error webhook HTTP " + status + ": " + resp.toString());
                    if (callback != null) callback.onResult(false, "HTTP " + status + " - " + resp.toString());
                }
            } catch (Exception e) {
                Log.e("VentasServiceJava", "Error enviando webhook", e);
                if (callback != null) callback.onResult(false, e.getMessage() == null ? "Exception" : e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        }).start();
    }
}
