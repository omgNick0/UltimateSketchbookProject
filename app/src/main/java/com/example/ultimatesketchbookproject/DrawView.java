package com.example.ultimatesketchbookproject;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;

import ViewModels.StrokeViewModel;


public class DrawView extends View {

    private static final float TOUCH_TOLERANCE = 4;
    private static final String TAG = "DrawView";

    private float mX, mY;
    private Paint mPaint;
    //ArrayList to store all the strokes drawn by the user on the Canvas
    private ArrayList<Stroke> paths = new ArrayList<>();
    private Stack<Stroke> removedPaths = new Stack<>(); // Stack data structure. LIFO Method used
    private int currentColor;
    private int strokeWidth;
    private boolean isInserted = false;


    //the Paint class encapsulates the color and style information about
    //how to draw the geometries,text and bitmaps
    private Path mPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;




    //Constructors to initialise all the attributes
    public DrawView(Context context) {
        this(context, null);
    }

    public DrawView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        //the below methods smoothens the drawings of the user
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        //0xff=255 in decimal
        mPaint.setAlpha(0xff);
        Log.d(TAG, "DrawView created");
    }

    //this method instantiate the bitmap and object
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //set an initial color of the brush
//        if (viewModel.getColor().getValue() != null) {
//            currentColor = viewModel.getColor().getValue();
//            Log.d(TAG, "here");
//        }
//        else {
//            currentColor = Color.RED;
//            Log.d(TAG, "Empty!");
//        }
        currentColor = Color.RED;

        Log.d(TAG, "Worked!");
        //set an initial brush size
        strokeWidth = 20;
    }

    //sets the current color of stroke
    public void setColor(int color) {
        currentColor = color;
    }
    public int getColor() {
        return currentColor;
    }

    //sets the stroke width
    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public ArrayList<Stroke> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Stroke> paths) {
        this.paths = paths;
    }

    public void clearScreen() {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        paths.clear();
        removedPaths.clear();
    }

    public void undo() {
        //check whether the List is empty or not
        //if empty, the remove method will return an error
        if (paths.size() != 0) {
            removedPaths.add(paths.get(paths.size() - 1));
            paths.remove(paths.size() - 1);
            invalidate();
        }
    }

    public void redo() {
        if (removedPaths.size() != 0) {
            paths.add(removedPaths.pop());
            invalidate();
        }
    }



    //this methods returns the current bitmap
    public Bitmap save() {
        return mBitmap;
    }

    public boolean hasPaths () {
        return paths.size() > 0;
    }

    //this is the main method where the actual drawing takes place

        // Need to switch current shapes and extc... github library
        //save the current state of the canvas before,
        //to draw the background of the canvas
    @Override
    protected void onDraw(Canvas canvas) { // todo как проверить что мы засеттили картинку сюда
        //todo 2 разных канваса ???
        canvas.save();
        //DEFAULT color of the canvas
        int backgroundColor = Color.WHITE;
        canvas.drawColor(backgroundColor);

        //now, we iterate over the list of paths and draw each path on the canvas
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);

            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, null);
        canvas.restore();
    }

    //the below methods manages the touch response of the user on the screen

    //firstly, we create a new Stroke and add it to the paths list
    private void touchStart(float x, float y) {
        mPath = new Path();
        Stroke fp = new Stroke(currentColor, strokeWidth, mPath);
        paths.add(fp);

        //finally remove any curve or line from the path
        mPath.reset();
        //this methods sets the starting point of the line being drawn
        mPath.moveTo(x, y);
        //we save the current coordinates of the finger
        mX = x;
        mY = y;
    }

    protected void setImageUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
            float aspecRatio = (float) bitmap.getHeight() / (float) bitmap.getWidth();
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            int mImageWidth = displayMetrics.widthPixels;
            int mImageHeight = Math.round(mImageWidth * aspecRatio);
            mBitmap = Bitmap.createScaledBitmap(bitmap, mImageWidth, mImageHeight, false);
            Bitmap mutableBitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            invalidate();
            requestLayout();
            mCanvas.drawBitmap(mutableBitmap, mImageWidth, mImageHeight, mPaint);
            mCanvas.setBitmap(mutableBitmap);
            Log.d(TAG, "inserted");
//            Canvas canvas = new Canvas(mutableBitmap);
//            canvas.setBitmap(mutableBitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Bitmap createBitmap(int img_id, int newHeight, int newWidth) {
        Bitmap myBitmap = BitmapFactory.decodeResource(getResources(), img_id);
        myBitmap = Bitmap.createScaledBitmap(myBitmap, newWidth, newHeight, false);
        return myBitmap;
    }

    protected void drawBitmap(Bitmap bitmap, float x, float y, Paint paint) { //todo: paint and more
        Log.d(TAG, "Bitmap: " + bitmap.toString());
        mCanvas.drawBitmap(bitmap, x, y, mPaint);
//        mCanvas.drawBitmap(bitmap, new Rect(200, 200, 200), new );
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "1", null);
        return Uri.parse(path);
    }

//    public void setBitmap(Bitmap bitmap) {
//        int centerX = (mCanvas.getWidth() - bitmap.getWidth()) / 2;
//        int centerY = (mCanvas.getHeight() - bitmap.getHeight()) / 2;
////
//        Paint paint = new Paint();
//        paint.setColor(Color.YELLOW);
//        paint.setAntiAlias(true);
//        paint.setDither(true);
//        paint.setFilterBitmap(true);
//
//        isInserted = true;
//
//        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Log.d(TAG, "Canvas: " + mCanvas);
//        mCanvas = new Canvas(mutableBitmap);



//        mCanvas.drawBitmap(bitmap, centerX, centerY, paint);
//        mCanvas.setBitmap(bitmap);
//
//        mCanvas.drawBitmap(bitmap, centerX, centerY, paint);
//        Log.d(TAG, bitmap + " "); /// D/DrawView: android.graphics.Bitmap@f8fa8b1

//        Bitmap srcBitmap = BitmapFactory.decodeResource(
//                getResources(),
//                R.drawable.colorpicker_img
//        );
//
//        // Initialize a new Bitmap
//        Bitmap bitmap = Bitmap.createBitmap(
//                700, // Width
//                500, // Height
//                Bitmap.Config.ARGB_8888 // Config
//        );
//
//        // Initialize a new Canvas instance
//        Canvas canvas = new Canvas(bitmap);
//
//        Matrix matrix = new Matrix();
//
//                /*
//                    public void setRotate (float degrees, float px, float py)
//                        Set the matrix to rotate by the specified number of degrees, with a pivot
//                        point at (px, py). The pivot point is the coordinate that should remain
//                        unchanged by the specified transformation.
//                */
//
//        // Set rotation on matrix
//        matrix.setRotate(
//                45, // degrees
//                srcBitmap.getWidth() / 2, // px
//                srcBitmap.getHeight() / 2 // py
//        );
//
//                /*
//                      postTranslate(float dx, float dy)
//                            Postconcats the matrix with the specified translation.
//                */
//
//        // Draw the bitmap at the center position of the canvas both vertically and horizontally
//        matrix.postTranslate(
//                mCanvas.getWidth() / 2 - srcBitmap.getWidth() / 2,
//                mCanvas.getHeight() / 2 - srcBitmap.getHeight() / 2
//        );
//
//                /*
//                    public void drawBitmap (Bitmap bitmap, Matrix matrix, Paint paint)
//                        Draw the bitmap using the specified matrix.
//
//                    Parameters
//                        bitmap : The bitmap to draw
//                        matrix : The matrix used to transform the bitmap when it is drawn
//                        paint : May be null. The paint used to draw the bitmap
//                */
//
//        // Finally, draw the bitmap on canvas as a rotated bitmap
//        mCanvas.drawBitmap(
//                bitmap, // Bitmap
//                matrix, // Matrix
//                paint // Paint
//        );
//
//        // Display the newly created bitmap on app interface
//        mCanvas.setBitmap(bitmap);
//    }



    //in this method we check if the move of finger on the
    // screen is greater than the Tolerance we have previously defined,
    //then we call the quadTo() method which actually smooths the turns we create,
    //by calculating the mean position between the previous position and current position
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
        }
    }

    //at the end, we call the lineTo method which simply draws the line until
    //the end position
    private void touchUp() {
        mPath.lineTo(mX, mY);
    }

    //the onTouchEvent() method provides us with the information about the type of motion
    //which has been taken place, and according to that we call our desired methods
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();



//        onTouchEventLine(event);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}