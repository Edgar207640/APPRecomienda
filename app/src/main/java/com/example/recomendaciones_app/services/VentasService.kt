package com.example.recomendaciones_app.services

import android.util.Log
import android.os.Handler
import android.os.Looper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Servicio responsable de notificar ventas al webhook de n8n.
 * Provee una API compatible con Java a través de `notificarVentaJava` y `getCurrentFormattedDate`.
 */
class VentasService {

    companion object {
        private const val TAG = "VentasService"
        // Actualizado al webhook Railway provisto por el usuario
        private const val WEBHOOK_URL = "https://primary-production-2873d.up.railway.app/webhook-test/products-sync"

        @JvmStatic
        fun getCurrentFormattedDate(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return sdf.format(Date())
        }
    }

    // Data class anidada: desde Java puede instanciarse como `new VentasService.VentaData(...)`.
    data class VentaData(
        val producto_id: Int,
        val producto_nombre: String,
        val cantidad: Int,
        val precio_unitario: Double,
        val total: Double,
        val usuario_id: String?,
        val fecha: String,
        val metodo_pago: String
    )

    // Interfaz para interop con Java
    interface VentaCallbackJava {
        fun onResult(success: Boolean, message: String)
    }

    private val client = OkHttpClient()

    /**
     * Notifica la venta al webhook. Ejecuta el callback en el hilo principal.
     */
    fun notificarVentaJava(venta: VentaData, callback: VentaCallbackJava?) {
        // Construir JSON
        val json = JSONObject().apply {
            put("producto_id", venta.producto_id)
            put("producto_nombre", venta.producto_nombre)
            put("cantidad", venta.cantidad)
            put("precio_unitario", venta.precio_unitario)
            put("total", venta.total)
            put("usuario_id", venta.usuario_id)
            put("fecha", venta.fecha)
            put("metodo_pago", venta.metodo_pago)
        }

        val payload = json.toString()
        Log.d(TAG, "Payload a enviar: $payload")

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(WEBHOOK_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error enviando webhook: ${e.message}", e)
                Handler(Looper.getMainLooper()).post {
                    callback?.onResult(false, "Network error: ${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                // Intentar leer el cuerpo de la respuesta para mayor detalle
                val responseBodyStr = try {
                    response.body?.string() ?: ""
                } catch (t: Throwable) {
                    Log.w(TAG, "No se pudo leer response body: ${t.message}")
                    ""
                }

                val success = response.isSuccessful
                val code = try { response.code } catch (_: Throwable) { -1 }
                val msgText = try { response.message } catch (_: Throwable) { "" }

                Log.d(TAG, "HTTP $code - message: $msgText - body: $responseBodyStr")

                val msg = if (success) {
                    "Webhook notified (HTTP $code) - body: $responseBodyStr"
                } else {
                    "Webhook error (HTTP $code): $msgText - body: $responseBodyStr"
                }
                // asegurar callback en hilo principal
                Handler(Looper.getMainLooper()).post {
                    callback?.onResult(success, msg)
                }
                response.close()
            }
        })
    }
}
