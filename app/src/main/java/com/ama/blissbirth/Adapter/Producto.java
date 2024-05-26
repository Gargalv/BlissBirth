package com.ama.blissbirth.Adapter;
public class Producto {

    private String img, nom, prec;

    public Producto(String img, String nom, String prec) {
        this.img = img;
        this.nom = nom;
        this.prec = prec;
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

    public String getPrec() {
        return prec;
    }

    public void setPrec(String prec) {
        this.prec = prec;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "img='" + img + '\'' +
                ", nom='" + nom + '\'' +
                ", prec='" + prec + '\'' +
                '}';
    }
}
