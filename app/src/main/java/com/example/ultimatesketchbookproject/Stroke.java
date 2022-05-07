package com.example.ultimatesketchbookproject;

import android.graphics.Path;


/**
 * This class needs for making paths and strokes objects, which will be displayed on the users UI
 * screen.
 */

public class Stroke {

    //color of the stroke
    public int color;
    //width of the stroke
    public int strokeWidth;
    //a Path object to represent the path drawn
    public Path path;

    //constructor to initialise the attributes
    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }
}