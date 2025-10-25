package com.example.recomendaciones_app.data.network;

import com.example.recomendaciones_app.data.model.Category;
import com.example.recomendaciones_app.data.model.ProductoResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("products")
    Call<ProductoResponse> getProducts(@Query("limit") int limit, @Query("skip") int skip);

    @GET("products/search")
    Call<ProductoResponse> searchProducts(@Query("q") String query);

    // Anotación: CORRECCIÓN. La API ahora devuelve una lista de objetos Category, no Strings.
    @GET("products/categories")
    Call<List<Category>> getAllCategories();

    @GET("products/category/{categoryName}")
    Call<ProductoResponse> getProductsByCategory(@Path("categoryName") String categoryName);

}
