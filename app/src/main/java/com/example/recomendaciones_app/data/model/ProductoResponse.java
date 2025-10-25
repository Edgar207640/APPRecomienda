package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductoResponse {

    @SerializedName("products")
    private List<Producto> products;

    @SerializedName("total")
    private int total;

    @SerializedName("skip")
    private int skip;

    @SerializedName("limit")
    private int limit;

    // --- Getters ---

    public List<Producto> getProducts() {
        return products;
    }

    public int getTotal() {
        return total;
    }

    public int getSkip() {
        return skip;
    }

    public int getLimit() {
        return limit;
    }
}
