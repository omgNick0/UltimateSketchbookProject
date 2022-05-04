package com.example.ultimatesketchbookproject;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.RangeSlider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Fragments.DialogFragment;
import Interfaces.PassDataInterface;
import States.StateManager;


public class MainActivity extends AppCompatActivity implements PassDataInterface {

    //creating the object of type DrawView
    //in order to get the reference of the View
    private DrawView drawView;
    //creating objects of type button

    private ExtendedFloatingActionButton save, colorPicker, stroke, instruments; // 4th btn to open chat with other users

    //creating a RangeSlider object, which will
    // help in selecting the width of the Stroke
    private RangeSlider rangeSlider;

    private static final int REQUEST_CODE = 123;
    private boolean isGranted = false;

    private static final String TAG = "MainActivity";

//    private static final int REQUEST_GET_PHOTO = 1;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private String dir;
    private File file;


//    private MainState state;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.undo:
                drawView.undo();
                return true;
            case R.id.redo:
                drawView.redo();
                return true;
//            case R.id.item3:
//                Toast.makeText(this, "Item 3 selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.import_image:
                getImageGallery();
//                Intent intent = new Intent();
//                intent.setType("image/*");
//                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
//                startActivity(Intent.createChooser(intent, "Tack Image"));
//                dispatchTakePictureIntent();
                return true;
            case R.id.export_image:
                Toast.makeText(this, R.string.export_picture, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);

//        state = StateManager.getState(this, new MainState());

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (data != null) {

                            InputStream inputStream;
                            Uri selectedImage = data.getData();

                            try {
                                inputStream = getContentResolver().openInputStream(selectedImage);
                                Bitmap image = BitmapFactory.decodeStream(inputStream);
                                DrawView view = (DrawView) findViewById(R.id.draw_view);
                                view.drawBitmap(image, 0, 0, new Paint());
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
//        String date = format.format(new Date());
////        filename = path + "/" + date + ".png";
//        try {
//            path.mkdirs();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        //getting the reference of the views from their ids

        drawView = (DrawView) findViewById(R.id.draw_view);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
        save = (ExtendedFloatingActionButton) findViewById(R.id.btn_save);
        colorPicker = (ExtendedFloatingActionButton) findViewById(R.id.btn_color);
        stroke = (ExtendedFloatingActionButton) findViewById(R.id.btn_stroke);
        instruments = (ExtendedFloatingActionButton) findViewById(R.id.btn_instruments);


        //creating a OnClickListener for each button, to perform certain actions

        //the undo button will remove the most recent stroke from the canvas
//        undo.setOnClickListener(view -> paint.undo());
//
//        redo.setOnClickListener(view -> paint.redo());
        //the save button will save the current canvas which is actually a bitmap
        //in form of PNG, in the storage


        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment fragment = new DialogFragment(MainActivity.this);
                fragment.show(getFragmentManager(), "TAG");
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    askPermission();
                    saveImage();
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            @SuppressLint("RestrictedApi")
            @Override
            public void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser) {
                drawView.setStrokeWidth((int) value);
            }
        });

        //pass the height and width of the custom view to the init method of the DrawView object
        ViewTreeObserver vto = drawView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                drawView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = drawView.getMeasuredWidth();
                int height = drawView.getMeasuredHeight();
                drawView.init(height, width);
            }
        });

        //          -> Then application starts not for the first time
//        if (savedInstanceState != null) {
//            drawView.setStrokeWidth(savedInstanceState.getInt("stroke_width"));
//        }

//        if (state.strokeWidth != 0) {
//            drawView.setStrokeWidth(state.strokeWidth);
//        }

    }
    private void getImageGallery() {
        if (isGranted) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(intent);
        } else {
            askPermission();
        }
    } // todo: github - functions - latest version of project

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) { // Permissions are not granted
            isGranted = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Create AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Grant permission");
                builder.setMessage("If you want to save your drawings, you need to grant permission");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                },
                                REQUEST_CODE
                        );
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        },
                        REQUEST_CODE
                );
            }
        } else { // When permissions are already granted
            isGranted = true;
            Log.d(TAG, "Permission for reading and writing granted"); // just a LOG
        }

    }

//    private void createDirectoryAndSaveFile() {
//
//        File direct = new File(Environment.getExternalStorageDirectory() + "/DirName");
//
//        if (!direct.exists()) {
//            File wallpaperDirectory = new File("/sdcard/DirName/");
//            wallpaperDirectory.mkdirs();
//        }
//
//        File file = new File(new File("/sdcard/DirName/"), filename);
//        if (file.exists()) {
//            file.delete();
//        }
//        // File file = new File(filename);
//        Bitmap bitmap = drawView.save();
//
//        if (drawView.hasPaths() && isGranted) {
//            try {
//                ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
//                byte[] bitmapData = bos.toByteArray();
//                FileOutputStream fos = new FileOutputStream(file);
//                fos.write(bitmapData);
//                fos.flush();
//                fos.close();
//                Toast.makeText(MainActivity.this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
//                System.out.println(filename);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "File was not found!", Toast.LENGTH_SHORT).show();
//            } catch (NullPointerException e) {
//                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "In code error occurred. Please contact developer", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "Another Error occurred!", Toast.LENGTH_SHORT).show();
//            }
//        } else
//            Toast.makeText(getApplicationContext(), "Your painting is empty or you didn't grant a permission!", Toast.LENGTH_SHORT).show();
//    }

    private void saveImage() throws IOException {
        dir = Environment.getExternalStorageDirectory().toString()+"/Pictures";
        File file = new File(dir + "/sketchbook_pictures");
        Bitmap bitmap = drawView.save();

        // Just some logs in here
        if (file.mkdirs())
            Log.d(TAG, "Folder was created!");
        else if (file.exists())
            Log.d(TAG, "Folder is exists already!");
        else
            Log.d(TAG, "Some error occurred...");

        if (drawView.hasPaths() && isGranted) {
            try {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                byte[] bitmapData = bos.toByteArray();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bitmapData);
                fos.flush();
                fos.close();
                Toast.makeText(MainActivity.this, "Image saved successfully!", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "File was not found!", Toast.LENGTH_SHORT).show();
            } catch (NullPointerException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "In code error occurred. Please contact developer", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Another Error occurred!", Toast.LENGTH_SHORT).show();
            }
        } else
            Toast.makeText(getApplicationContext(), "Your painting is empty or you didn't grant a permission!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataReceived(String color) {
        drawView.setColor(Color.parseColor(color));
        colorPicker.setBackgroundColor(Color.parseColor(color));
    }

    @Override
    public void onDataReceived(int color) {
        drawView.setColor(color);
        colorPicker.setBackgroundColor(color);
    }
    @Override // What is wrong with all this ?????
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

//        state.strokeWidth = drawView.getStrokeWidth();
//        drawView.setStrokeWidth(state.strokeWidth);
//        outState.putParcelable("stroke_width", drawView.getStrokeWidth());
//        outState.putInt("color", drawView.getColor());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
//        drawView.setStrokeWidth(state.strokeWidth);
//        int width = savedInstanceState.getInt("stroke_width", drawView.getStrokeWidth());
////        Log.d(TAG, "" + drawView.getStrokeWidth());
//        drawView.setStrokeWidth(width);
//        drawView.setColor(savedInstanceState.getInt("color"));
    }
}