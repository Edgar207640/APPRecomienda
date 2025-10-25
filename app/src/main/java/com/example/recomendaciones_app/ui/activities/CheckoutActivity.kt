package com.example.recomendaciones_app.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.recomendaciones_app.data.network.VentasService

/**
 * Activity de ejemplo que simula el flujo de checkout y notifica una venta a n8n usando VentasService.
 * Esta clase sirve para integrar y probar `VentasService` desde la app.
 */
class CheckoutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No requerimos un layout para la prueba; en tu app real llama a simulateSale() tras el pago.
        simulateSale()
    }

    private fun simulateSale() {
        val ventasService = VentasService()

        val venta = VentasService.VentaData(
            producto_id = 1,
            producto_nombre = "Producto de prueba",
            cantidad = 2,
            precio_unitario = 49.99,
            total = 99.98,
            usuario_id = null, // No hay usuario por defecto
            fecha = VentasService.getCurrentFormattedDate(),
            metodo_pago = "Tarjeta"
        )

        // Hacer explícitos los tipos del lambda para evitar problemas de inferencia
        ventasService.notificarVenta(venta) { success: Boolean, message: String ->
            // El callback ya se ejecuta en el hilo principal según la implementación de VentasService
            if (success) {
                Toast.makeText(this, "Venta notificada: $message", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Error notificando venta: $message", Toast.LENGTH_LONG).show()
            }
        }
    }
}
