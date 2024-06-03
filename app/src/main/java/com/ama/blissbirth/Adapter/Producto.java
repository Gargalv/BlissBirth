package com.ama.blissbirth.Adapter;

import com.google.firebase.firestore.GeoPoint;

public class Producto {

    private String img, nom, date;
    private GeoPoint location;

    public Producto(String img, String nom, String date, GeoPoint location) {
        this.img = img;
        this.nom = nom;
        this.date = date;
        this.location = location;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "img='" + img + '\'' +
                ", nom='" + nom + '\'' +
                ", date='" + date + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
