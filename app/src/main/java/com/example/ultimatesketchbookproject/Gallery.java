package com.example.ultimatesketchbookproject;


public class Gallery {
    private String name;
    private String date;
    private int resourceId;

    public Gallery(String name, String date, int file) {
        this.name = name;
        this.date = date;
        this.resourceId = resourceId;
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

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }
}
