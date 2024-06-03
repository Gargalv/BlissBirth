package com.ama.blissbirth.Adapter;

import com.google.firebase.firestore.GeoPoint;

public class ProductFB {
    private String imgurl;
    private String name;
    private String desc;
    private String date;
    private GeoPoint location;

    public ProductFB() {
        // Constructor vacío necesario para la deserialización de Firestore
    }

    public ProductFB(String imgurl, String name, String desc, String date, GeoPoint location) {
        this.imgurl = imgurl;
        this.name = name;
        this.desc = desc;
        this.date = date;
        this.location = location;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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
}
