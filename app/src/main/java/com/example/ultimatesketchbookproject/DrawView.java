package com.example.ultimatesketchbookproject;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Stack;


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


    //the Paint class encapsulates the color and style information about
    //how to draw the geometries,text and bitmaps
    private Path mPath;
    private Bitmap mBitmap;
    private Canvas mCanvas;
    private final Paint mBitmapPaint = new Paint(Paint.DITHER_FLAG);




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
//        init(getWidth(), getHeight()); // what is that
    }

    //this method instantiate the bitmap and object
    public void init(int height, int width) {

        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        //set an initial color of the brush
        currentColor = Color.GREEN;
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

    public int getStrokeWidth() {
        return strokeWidth;
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
    protected void onDraw(Canvas canvas) {
        canvas.save();
        //DEFAULT color of the canvas
        int backgroundColor = Color.WHITE;
        mCanvas.drawColor(backgroundColor);

        //now, we iterate over the list of paths and draw each path on the canvas
        for (Stroke fp : paths) {
            mPaint.setColor(fp.color);
            mPaint.setStrokeWidth(fp.strokeWidth);

            mCanvas.drawPath(fp.path, mPaint);
        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
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

    protected void drawBitmap(Bitmap bitmap, float x, float y, Paint paint) { //todo: paint and more
        Log.d(TAG, "Bitmap: " + bitmap.toString());
        mCanvas.drawBitmap(bitmap, x, y, mPaint);
//        mCanvas.drawBitmap(bitmap, new Rect(200, 200, 200), new );
    }

//    @Nullable
//    @Override
//    protected Parcelable onSaveInstanceState() {
//        Parcelable parentState = super.onSaveInstanceState();
//        SavedState savedState = new SavedState(parentState);
//        savedState.mBitmap = mBitmap;
//        savedState.mCanvas = mCanvas;
//        mCanvas.save();
//        savedState.mPath = mPath;
//        savedState.mPaint = mPaint;
//        return savedState;
//    }
//
//    @Override
//    protected void onRestoreInstanceState(Parcelable state) {
//        SavedState savedState = (SavedState) state;
//        super.onRestoreInstanceState(savedState.getSuperState());
//        setColor(savedState.currentColor);
//        setStrokeWidth(savedState.strokeWidth);
//
//        mBitmap = savedState.mBitmap;
//        mCanvas = savedState.mCanvas;
//        mCanvas.restore();
//        mPath = savedState.mPath;
//        mPaint = savedState.mPaint;
//    }

//    private static class SavedState extends BaseSavedState {
//        int currentColor;
//        int strokeWidth;
//        Path mPath;
//        Bitmap mBitmap;
//        Canvas mCanvas;
//        Paint mPaint;
//
//        SavedState(Parcelable superState) {
//            super(superState);
//        }
//
//        private SavedState(Parcel in) {
//            super(in);
//            strokeWidth = in.readInt();
//            currentColor = in.readInt();
//        }
//
//        public static final Parcelable.Creator<SavedState> CREATOR =
//                new Parcelable.Creator<SavedState>() {
//                    public SavedState createFromParcel(Parcel in) {
//                        return new SavedState(in);
//                    }
//
//                    public SavedState[] newArray (int size) {
//                        return new SavedState[size];
//                    }
//                };
//    }






//------------------------------------------------------------------
// Line//
// ------------------------------------------------------------------


//private void onDrawLine (Canvas canvas) {
//        float dx = Math.abs(mx - mStart)
//}


// code later  ....




//------------------------------------------------------------------
// Rectangle//
// ------------------------------------------------------------------
















}