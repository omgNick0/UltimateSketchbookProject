
package com.example.ultimatesketchbookproject;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Fragments.ColorsFragment;
import ViewModels.StrokeViewModel;
import Interfaces.PassDataColorInterface;

// todo рабочий - этот !!!!!!
// todo на завтра - восстановить ввод имени картинки, обработать нажатие на recycler view, ищменение имени

public class MainActivity extends AppCompatActivity implements PassDataColorInterface {
    // todo: import image on drawing + server + settings

    //creating the object of type DrawView
    //in order to get the reference of the View
    private DrawView drawView;
    //creating objects of type button

//    private ExtendedFloatingActionButton gallery, colorPicker, stroke, instruments; // 4th btn to open chat with other users
    private BottomNavigationView navigationView;
    private RelativeLayout layout;

    //creating a RangeSlider object, which will
    // help in selecting the width of the Stroke
    private RangeSlider rangeSlider;

    private static final int REQUEST_CODE = 123;
    private boolean isGranted = false;

    private static final String TAG = "MainActivity";
    private static final String INSERT_IMAGE = "InsertImage";

//    private static final int REQUEST_GET_PHOTO = 1;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private StrokeViewModel strokeViewModel;

    private final Handler mUiHandler = new Handler();

    private String image_name = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermission();

        navigationView = findViewById(R.id.bottom_navigation_menu);

        someActivityResultLauncher = registerForActivityResult( // todo: need to draw on selected image from gallery
                // todo: as a solution - make method in DrawView, which will clear all paths of strokes in ArrayList and clear canvas
                // todo: but how draw on it...
                // todo: возможно проблема в том что вызывается только в методе onCreate, соответственно DrawView не получается никакой информации
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
                                Log.d(TAG, image + " ");
                                Log.d(TAG, drawView + " ");
                                Uri imageUri = drawView.getImageUri(MainActivity.this, image);
                                Log.d(INSERT_IMAGE, "Image inserted");
                                drawView.setImageUri(imageUri); // can be selectedImage value in here
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        navigationView.setOnItemSelectedListener(item -> {

            switch (item.getItemId()){
                case R.id.btn_new_painting:
                    makeNewPainting();
                    break;
                case R.id.btn_stroke:
                    if (rangeSlider.getVisibility() == View.VISIBLE)
                        rangeSlider.setVisibility(View.GONE);
                    else
                        rangeSlider.setVisibility(View.VISIBLE);
                    break;
                case R.id.btn_color:
                    ColorsFragment fragment = new ColorsFragment(MainActivity.this);
                    fragment.show(getSupportFragmentManager(), TAG);
                    break;
                case R.id.btn_gallery:
                    startActivity(new Intent(MainActivity.this, GalleryActivity.class));
                    break;
            }
            return true;
        });

        layout = findViewById(R.id.main_layout);
        drawView = (DrawView) findViewById(R.id.draw_view);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);

        strokeViewModel = new ViewModelProvider((this)).get(StrokeViewModel.class);

//        Integer color = strokeViewModel.getColor().getValue();
//
//        if (color == null) {
//            strokeViewModel.setColor(drawView.getColor());
//        }
        //creating a OnClickListener for each button, to perform certain actions

        //the undo button will remove the most recent stroke from the canvas
//        undo.setOnClickListener(view -> paint.undo());
//
//        redo.setOnClickListener(view -> paint.redo());
        //the save button will save the current canvas which is actually a bitmap
        //in form of PNG, in the storage


        //set the range of the RangeSlider
        rangeSlider.setValueFrom(0.0f);
        rangeSlider.setValueTo(100.0f);

        //adding a OnChangeListener which will change the stroke width
        //as soon as the user slides the slider
        rangeSlider.addOnChangeListener((slider, value, fromUser) -> drawView.setStrokeWidth((int) value));

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

        Integer color = strokeViewModel.getColor().getValue();

        if (color == null && savedInstanceState != null) {
            strokeViewModel.setColor(drawView.getColor());
            drawView.setColor(savedInstanceState.getInt("color"));
        }

        if (savedInstanceState != null) {
            drawView.setStrokeWidth(savedInstanceState.getInt("stroke_width"));
        }

//        if (state.strokeWidth != 0) {
//            drawView.setStrokeWidth(state.strokeWidth);
//        }

    }

    private void openSocialNetworks(Uri uri) {
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/jpeg"); // mb need to change to png
        startActivity(intent);
        Log.d(TAG, "Reached!");
    }

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
            case R.id.save_image:
                askPermission();
                getImageName();
                return true;
            case R.id.import_image:
                getImageGallery();
                return true;
            case R.id.export_image:
                askPermission();
                openSocialNetworks(drawView.getImageUri(this, drawView.save()));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeNewPainting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.new_painting_title);
        builder.setMessage(R.string.new_painting_desc);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                drawView.makeNewPainting();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getImageGallery() {
        if (isGranted) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(intent);
        } else {
            askPermission();
        }
    }

    // Вроде бы всё исправил
    private void getImageName() { // todo everything new is here after crash
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this);
        builder.setTitle(R.string.save_title);
        builder.setMessage(R.string.save_message);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        builder.setView(input);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                image_name = input.getText().toString();
                if (!image_name.equals("") && drawView.hasPaths()) {
                    saveImage(drawView.save());
                    Log.d(TAG, "image_name: " + image_name);
                } else if (image_name.equals("") || !drawView.hasPaths() && (isGranted)) {
                    Snackbar snackbar = Snackbar.make(layout, R.string.error_save, Snackbar.LENGTH_SHORT);
                    snackbar.setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else if (!isGranted)
                    askPermission();
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.show();
        Log.d(TAG, "image_name: " + image_name);
    }

    private void askPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                + ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) { // Permissions are not granted
            isGranted = false;
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Create AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.grant_permission_title);
                builder.setMessage(R.string.grant_permission_message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
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
                builder.setNegativeButton(R.string.cancel, null);
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
            Log.d(TAG, "Permission for reading and writing granted");
        }
    }


    /**
     * private class, which will run in new Thread, using Runnable and which can send UI messages
     * to the user in the main Thread, using Loop and addressing to Message queue
     */
    private static class SaveThread extends HandlerThread {
        private Handler workerHandler;

        SaveThread(String name) {
            super(name);
        }

        public void postTask(Runnable task) {
            workerHandler.post(task);
        }

        public void prepareHandler() {
            workerHandler = new Handler(getLooper());
        }
    }

    private Uri saveImage(Bitmap bitmap) {
        SaveThread mWorkerThread = new SaveThread("Saver");
        final Uri[] uri = {null};
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (isGranted) {
                    String filename = image_name + ".jpg";
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
                    try {
                        // code, which turns View to a byte and writes it to an image
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
                        byte[] bitmapData = bos.toByteArray();
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(bitmapData);
                        fos.flush();
                        fos.close();
                        // insert our picture to gallery
                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, image_name, "desc"); // todo: what description here ???
                        uri[0] = Uri.fromFile(file);
                        Log.d(TAG, "Saved in gallery and External Storage");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "File was not found!", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
//                 Unable to create file, likely because external storage is
//                 not currently mounted.
                        Log.w("ExternalStorage", "Error writing " + file, e);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Nothing to save", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Another Error occurred!", Toast.LENGTH_SHORT).show();
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar snackbar = Snackbar.make(layout, R.string.image_saved, Snackbar.LENGTH_SHORT);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    });

                }
            }
        };
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        mWorkerThread.postTask(task);

        return uri[0];
    }


//    /**
//     * This function provides user to save image on external storage, so it will be private, but also
//     * it saves image go a gallery, so every app can get it and it will be public
//     * Image saving in another thread, in a way not to make main thread too heavy
//     */
//    private Uri saveImage(Bitmap bitmap) {
//        SaveThread mWorkerThread = new SaveThread("Saver");
//        final Uri[] uri = {null};
//        Runnable task = new Runnable() {
//            @Override
//            public void run() {
//                if (drawView.hasPaths() && isGranted) {
//                    Date currentDate = new Date();
//                    SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
//                    String date = format.format(new Date());
//                    DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
//                    String timeText = timeFormat.format(currentDate);
//                    String filename = date + ".jpg";
//                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename);
//                    try {
//                        // code, which turns View to a byte and writes it to an image
//                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                        bitmap.compress(Bitmap.CompressFormat.PNG, 0, bos);
//                        byte[] bitmapData = bos.toByteArray();
//                        FileOutputStream fos = new FileOutputStream(file);
//                        fos.write(bitmapData);
//                        fos.flush();
//                        fos.close();
//                        // insert our picture to gallery
//                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, timeText, "desc");
//                        uri[0] = Uri.fromFile(file);
//                        Log.d(TAG, "Saved in gallery and External Storage");
//                    } catch (FileNotFoundException e) {
//                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "File was not found!", Toast.LENGTH_SHORT).show();
//                    } catch (IOException e) {
////                 Unable to create file, likely because external storage is
////                 not currently mounted.
//                        Log.w("ExternalStorage", "Error writing " + file, e);
//                    } catch (NullPointerException e) {
//                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Nothing to save", Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        Toast.makeText(MainActivity.this, "Another Error occurred!", Toast.LENGTH_SHORT).show();
//                    }
//
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                    mUiHandler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            Snackbar snackbar = Snackbar.make(layout, R.string.image_saved, Snackbar.LENGTH_SHORT);
//                            snackbar.setAction("OK", new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    snackbar.dismiss();
//                                }
//                            });
//                            snackbar.show();
//                        }
//                    });
//
//                } else {
//                    Snackbar snackbar = Snackbar.make(layout, R.string.no_permission, Snackbar.LENGTH_SHORT);
//                    snackbar.setAction(R.string.ok, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            askPermission();
//                        }
//                    });
//                    snackbar.show();
//                }
//            }
//        };
//        mWorkerThread.start();
//        mWorkerThread.prepareHandler();
//        mWorkerThread.postTask(task);
//
//        return uri[0];
//    }

    /**
     * Checks if the external storage is writable.
     * @return true if storage is writable, false otherwise
     */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDataReceived(String color) {
        drawView.setColor(Color.parseColor(color));
    }

    @Override
    public void onDataReceived(int color) {
        drawView.setColor(color);
    }

    @Override // What is wrong with all this ?????
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt("color", drawView.getColor());

        // TODO put value from Fragment in here
//        outState.putString("key", );

//        state.strokeWidth = drawView.getStrokeWidth();
//        drawView.setStrokeWidth(state.strokeWidth);
//        outState.putParcelable("stroke_width", drawView.getStrokeWidth());
//        outState.putInt("color", drawView.getColor());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        savedInstanceState.getInt("color");
//        drawView.setStrokeWidth(state.strokeWidth);
//        int width = savedInstanceState.getInt("stroke_width", drawView.getStrokeWidth());
//        drawView.setStrokeWidth(width);
//        drawView.setColor(savedInstanceState.getInt("color"));
    }

    // set values
    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Stroke> data = strokeViewModel.getLines().getValue();
        Integer color = strokeViewModel.getColor().getValue();

        if (data != null) {
            drawView.setPaths(data);
        }
        Log.d(TAG, "Resumed");

//        if (color != null) {
//            drawView.setColor(color);
//        }

    }

    // Get values
    @Override
    protected void onPause() {
        super.onPause();
        strokeViewModel.setLines(drawView.getPaths());
        strokeViewModel.setColor(drawView.getColor());
    }

}