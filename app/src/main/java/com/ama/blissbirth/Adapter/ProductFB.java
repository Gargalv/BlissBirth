package com.ama.blissbirth.Adapter;

import android.widget.ImageView;
public class ProductFB {
    String name, desc, price, imgurl;

    public ProductFB(String name, String desc, String price, String imgurl) {
        this.name = name;
        this.desc = desc;
        this.price = price;
        this.imgurl = imgurl;
    }

    public ProductFB() {
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

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }

    @Override
    public String toString() {
        return "ProductFB{" +
                "name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", price='" + price + '\'' +
                '}';
    }
}
