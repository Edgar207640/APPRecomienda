package com.example.recomendaciones_app.data.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Servicio responsable de notificar ventas al webhook de n8n.
 * Usa OkHttp para realizar la petición POST y org.json para construir el payload JSON.
 */
class VentasService {

    companion object {
        private const val TAG = "VentasService"
        // URL del webhook de n8n — usar endpoint público y sin espacios
        private const val WEBHOOK_URL = "https://primary-production-2873d.up.railway.app/webhook-test/products-sync"
        // Usar API compatible con okhttp3 en esta versión del proyecto
        private val JSON_MEDIA_TYPE: MediaType = "application/json; charset=utf-8".toMediaType()

        @JvmStatic
        fun getCurrentFormattedDate(): String {
            return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * Datos de la venta que se enviarán al webhook.
     */
    data class VentaData constructor(
        val producto_id: Int,
        val producto_nombre: String,
        val cantidad: Int,
        val precio_unitario: Double,
        val total: Double,
        val usuario_id: String?,
        val fecha: String, // formato "yyyy-MM-dd HH:mm:ss"
        val metodo_pago: String
    )

    /**
     * Interfaz de callback para Java
     */
    interface VentaCallbackJava {
        fun onResult(success: Boolean, message: String)
    }

    /**
     * Método específico para Java que usa la interfaz VentaCallbackJava
     */
    @JvmName("notificarVentaJava")
    fun notificarVenta(venta: VentaData, callback: VentaCallbackJava) {
        notificarVenta(venta) { success, message ->
            callback.onResult(success, message)
        }
    }

    /**
     * Notifica la venta enviando un POST JSON al webhook.
     * El callback siempre se ejecuta en el hilo principal (UI).
     *
     * @param venta Objeto con los datos de la venta.
     * @param callback Función que recibirá (exito: Boolean, mensaje: String).
     */
    fun notificarVenta(venta: VentaData, callback: (Boolean, String) -> Unit) {
        // Construir JSON con org.json
        val jsonObject = try {
            JSONObject().apply {
                put("producto_id", venta.producto_id)
                put("producto_nombre", venta.producto_nombre)
                put("cantidad", venta.cantidad)
                put("precio_unitario", venta.precio_unitario)
                put("total", venta.total)
                put("usuario_id", venta.usuario_id ?: JSONObject.NULL)
                put("fecha", venta.fecha)
                put("metodo_pago", venta.metodo_pago)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error construyendo JSON de la venta", e)
            // Ejecutar callback en UI thread con error
            mainHandler.post { callback(false, "Error serializando datos de la venta: ${e.message}") }
            return
        }

        val payload = jsonObject.toString()
        Log.d(TAG, "Payload venta: $payload")

        // Usar extensión toRequestBody
        val requestBody: RequestBody = payload.toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(WEBHOOK_URL)
            .post(requestBody)
            .addHeader("Accept", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Error enviando venta al webhook", e)
                mainHandler.post { callback(false, "Fallo de red: ${e.message}") }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    val code = try { resp.code } catch (_: Exception) { -1 }
                    val bodyStr = try { resp.body?.string() } catch (_: Exception) { null }

                    if (code in 200..299) {
                        Log.d(TAG, "Venta notificada correctamente. code=$code body=$bodyStr")
                        mainHandler.post { callback(true, "Notificación enviada (code=$code)") }
                    } else {
                        Log.e(TAG, "Error notificando venta. code=$code body=$bodyStr")
                        mainHandler.post { callback(false, "Error del servidor: code=$code") }
                    }
                }
            }
        })
    }
}