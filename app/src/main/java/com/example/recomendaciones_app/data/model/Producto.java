package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Producto implements Serializable {

    private int localId;

    @SerializedName("id")
    private int apiId;

    @SerializedName("title")
    private String nombre;
    
    @SerializedName("description")
    private String descripcion;

    @SerializedName("price")
    private double price;

    @SerializedName("discountPercentage")
    private double discountPercentage;

    @SerializedName("rating")
    private double rating;
    
    @SerializedName("brand")
    private String brand;

    @SerializedName("category")
    private String categoria;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("images")
    private List<String> images;

    // Anotación: FUNCIONALIDAD AÑADIDA. Lista de opiniones del producto.
    @SerializedName("reviews")
    private List<Review> reviews;

    private int meGusta; 
    private int visto;   
    private String timestamp;

    // --- Getters y Setters ---

    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }

    public int getApiId() { return apiId; }
    public void setApiId(int apiId) { this.apiId = apiId; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }
    
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    // Anotación: FUNCIONALIDAD AÑADIDA. Getter y Setter para las opiniones.
    public List<Review> getReviews() { return reviews; }
    public void setReviews(List<Review> reviews) { this.reviews = reviews; }

    public int getMeGusta() { return meGusta; }
    public void setMeGusta(int meGusta) { this.meGusta = meGusta; }

    public int getVisto() { return visto; }
    public void setVisto(int visto) { this.visto = visto; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
