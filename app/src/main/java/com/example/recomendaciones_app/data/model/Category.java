package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("slug")
    private String slug;

    @SerializedName("name")
    private String name;

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }
}
