package com.example.ultimatesketchbookproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.slider.RangeSlider;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import top.defaults.colorpicker.ColorPickerPopup;


public class MainActivity extends AppCompatActivity {
    //creating the object of type DrawView
    //in order to get the reference of the View
    private DrawView paint;
    //creating objects of type button
    private ImageButton save, color, stroke, undo;
    //creating a RangeSlider object, which will
    // help in selecting the width of the Stroke
    private RangeSlider rangeSlider;

    private Bitmap bitmap;


    private static String filename;
    private File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
    private File file = new File(path, "DemoPicture.jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        askPermission(); // Asks users permission for reading and writing external storage memory

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        filename = path + "/" + date + ".png";

        try {
            path.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //getting the reference of the views from their ids
        paint = (DrawView) findViewById(R.id.draw_view);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
        undo = (ImageButton) findViewById(R.id.btn_undo);
        save = (ImageButton) findViewById(R.id.btn_save);
        color = (ImageButton) findViewById(R.id.btn_color);
        stroke = (ImageButton) findViewById(R.id.btn_stroke);

        //creating a OnClickListener for each button, to perform certain actions

        //the undo button will remove the most recent stroke from the canvas
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paint.undo();
            }
        });
        //the save button will save the current canvas which is actually a bitmap
        //in form of PNG, in the storage

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    saveImage();
                    Toast.makeText(MainActivity.this, "Saved successfully!", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Error occurred!", Toast.LENGTH_SHORT).show();
                }
            }
        });
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //getting the bitmap from DrawView class
//                Bitmap bmp=paint.save();
//                //opening a OutputStream to write into the file
//                OutputStream imageOutStream = null;
//
//                ContentValues cv=new ContentValues();
//                //name of the file
//                cv.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing.png");
//                //type of the file
//                cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
//                //location of the file to be saved
//                cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//
//                //get the Uri of the file which is to be v=created in the storage
//                Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//                try {
//                    //open the output stream with the above uri
//                    imageOutStream = getContentResolver().openOutputStream(uri);
//                    //this method writes the files in storage
//                    bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
//                    //close the output stream after use
//                    imageOutStream.close();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        });
        //the color button will allow the user to select the color of his brush

        color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPickerPopup.Builder(MainActivity.this)
                        .initialColor(Color.RED) // default color
                        .enableAlpha(true)
                        .okTitle("choose")
                        .enableBrightness(true)
                        .showIndicator(true)
                        .showValue(true)
                        .build()
                        .show(view, new ColorPickerPopup.ColorPickerObserver() {
                            @Override
                            public void onColorPicked(int color) {
                                paint.setColor(color);
                            }
                        });
            }
        });
        // the button will toggle the visibility of the RangeBar/RangeSlider
        stroke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rangeSlider.getVisibility() == View.VISIBLE)
                    rangeSlider.setVisibility(View.GONE);
                else
                    rangeSlider.setVisibility(View.VISIBLE);
            }
        });

        //set the range of the RangeSlider
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);
        //adding a OnChangeListener which will change the stroke width
        //as soon as the user slides the slider
        rangeSlider.addOnChangeListener(new RangeSlider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                paint.setStrokeWidth((int) value);
            }
        });

        //pass the height and width of the custom view to the init method of the DrawView object
        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });
    }


    private void askPermission() {
        Dexter.withContext(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        Toast.makeText(MainActivity.this, "Granted!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();
    }

    private void saveImage() throws IOException {
        File file = new File(filename);
        Bitmap bitmap = paint.save();


        if (paint.hasPaths()) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapData = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
                Toast.makeText(MainActivity.this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
                System.out.println(filename);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "File was not found!", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "NullPointer Exception error!", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Another Error occurred!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Your painting is empty!", Toast.LENGTH_SHORT).show();
    }
}