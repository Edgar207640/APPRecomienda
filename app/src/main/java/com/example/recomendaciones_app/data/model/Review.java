package com.example.recomendaciones_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

// Anotación: FUNCIONALIDAD AÑADIDA. Modelo para las opiniones de los usuarios.
public class Review implements Serializable {

    @SerializedName("rating")
    private int rating;

    @SerializedName("comment")
    private String comment;

    @SerializedName("date")
    private String date;

    @SerializedName("reviewerName")
    private String reviewerName;

    // No necesitamos el email, pero lo dejamos por si acaso en el futuro.
    @SerializedName("reviewerEmail")
    private String reviewerEmail;

    // --- Getters y Setters ---

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public String getReviewerEmail() {
        return reviewerEmail;
    }

    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }
}
