package com.example.ultimatesketchbookproject;


import android.graphics.Bitmap;

public class Gallery {
    private String name;
    private String date;
    private Bitmap bitmap_image;

    public Gallery(String name, String date, Bitmap bitmap) {
        this.name = name;
        this.date = date;
        this.bitmap_image = bitmap;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Bitmap getBitmap_image() {
        return bitmap_image;
    }

    public void setBitmap_image(Bitmap bitmap) {
        this.bitmap_image = bitmap;
    }
}
