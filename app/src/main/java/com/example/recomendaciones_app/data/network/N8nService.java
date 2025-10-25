package com.example.recomendaciones_app.data.network;

import android.util.Log;

import androidx.annotation.NonNull;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Servicio para enviar datos al endpoint de n8n (webhook-test/products-sync) usando Retrofit.
 */
public class N8nService {
    private static final String TAG = "N8nService";
    // Use base host URL only; endpoints are defined in the interface
    private static final String BASE_URL = "https://primary-production-2873d.up.railway.app/";

    // Interfaz Retrofit interna
    public interface N8nApi {
        @Headers("Content-Type: application/json")
        @POST("webhook-test/products-sync")
        Call<ResponseBody> sendProductSync(@Body Object body);
    }

    private final N8nApi api;
    private final Gson gson = new Gson();

    public N8nService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(N8nApi.class);
    }

    /**
     * Envía el objeto recibido en el cuerpo de la petición POST al endpoint.
     * Esta implementación envuelve la lista en un objeto {"products": ...} y realiza la petición de forma asíncrona con logging.
     *
     * @param datos Objeto (normalmente lista) que será convertido a JSON y enviado en el cuerpo.
     */
    public void enviarDatos(Object datos) {
        // Build a JSON-friendly wrapper payload: { "products": datos }
        Map<String, Object> payload = new HashMap<>();
        payload.put("products", datos);

        // Log payload para diagnóstico
        try {
            String payloadJson = gson.toJson(payload);
            Log.d(TAG, "enviarDatos: payload=" + payloadJson);
        } catch (Exception e) {
            Log.w(TAG, "enviarDatos: fallo al serializar payload", e);
        }

        Call<ResponseBody> call = api.sendProductSync(payload);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "enviarDatos: Éxito - code=" + response.code());
                } else {
                    String errorBody = null;
                    try {
                        ResponseBody eb = response.errorBody();
                        if (eb != null) errorBody = eb.string();
                    } catch (IOException ioe) {
                        Log.w(TAG, "enviarDatos: fallo leyendo errorBody", ioe);
                    }
                    Log.w(TAG, "enviarDatos: Error en respuesta - code=" + response.code() + " body=" + errorBody);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "enviarDatos: Falló la petición", t);
            }
        });
    }

    /**
     * Alternativa que devuelve el Call para que el llamador pueda gestionar la ejecución o agregar un Callback.
     */
    public Call<ResponseBody> enviarDatos(Object datos, Callback<ResponseBody> callback) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("products", datos);
        Call<ResponseBody> call = api.sendProductSync(payload);
        if (callback != null) {
            call.enqueue(callback);
        }
        return call;
    }
}
