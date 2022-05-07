package com.example.ultimatesketchbookproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import Fragments.ColorsFragment;
import ViewModels.StrokeViewModel;
import Interfaces.PassDataColorInterface;


public class MainActivity extends AppCompatActivity implements PassDataColorInterface {

    //creating the object of type DrawView
    //in order to get the reference of the View
    private DrawView drawView;
    //creating objects of type button

    private ExtendedFloatingActionButton gallery, colorPicker, stroke, instruments; // 4th btn to open chat with other users
    private RelativeLayout layout;

    //creating a RangeSlider object, which will
    // help in selecting the width of the Stroke
    private RangeSlider rangeSlider;

    private static final int REQUEST_CODE = 123;
    private boolean isGranted = false;

    private static final String TAG = "MainActivity";

//    private static final int REQUEST_GET_PHOTO = 1;

    private ActivityResultLauncher<Intent> someActivityResultLauncher;

    private StrokeViewModel strokeViewModel;

    private Handler mUiHandler = new Handler();
    private SaveThread mWorkerThread;

    public static boolean ACTIVITY_STATE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActivityCompat.requestPermissions(this,
//                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//

        someActivityResultLauncher = registerForActivityResult( // todo: need to draw on selected image from gallery
                // todo: as a solution - make method in DrawView, which will clear all paths of strokes in ArrayList and clear canvas
                // todo: but how draw on it...
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
                                Uri imageUri = drawView.getImageUri(this, image);
                                drawView.setImageUri(imageUri); // can be selectedImage value in here
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        layout = findViewById(R.id.main_layout);
        drawView = (DrawView) findViewById(R.id.draw_view);
        rangeSlider = (RangeSlider) findViewById(R.id.rangebar);
        gallery = (ExtendedFloatingActionButton) findViewById(R.id.btn_gallery);
        colorPicker = (ExtendedFloatingActionButton) findViewById(R.id.btn_color);
        stroke = (ExtendedFloatingActionButton) findViewById(R.id.btn_stroke);
        instruments = (ExtendedFloatingActionButton) findViewById(R.id.btn_instruments);

        strokeViewModel = new ViewModelProvider((this)).get(StrokeViewModel.class);

        strokeViewModel.getLines().observe(this, new Observer<ArrayList<Stroke>>() {
            @Override
            public void onChanged(ArrayList<Stroke> strokes) {

            }
        });

        strokeViewModel.getColor().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {

            }
        });

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


        colorPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorsFragment fragment = new ColorsFragment(MainActivity.this);
                fragment.show(getSupportFragmentManager(), TAG);
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Snackbar snackbar = Snackbar.make(layout, "gallery opened. Wow!", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("dismiss", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
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

        Integer color = strokeViewModel.getColor().getValue();

        if (color == null && savedInstanceState != null) {
            ACTIVITY_STATE = true;
            strokeViewModel.setColor(drawView.getColor());
            drawView.setColor(savedInstanceState.getInt("color"));
        }

//        if (savedInstanceState != null) {
//            drawView.setStrokeWidth(savedInstanceState.getInt("stroke_width"));
//        }
//
//        if (state.strokeWidth != 0) {
//            drawView.setStrokeWidth(state.strokeWidth);
//        }

    }

    private void openSocialNetworks() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "Приложение name, скачивай от сюда - ссылка");
        sendIntent.setType("text/plain");
        startActivity(Intent.createChooser(sendIntent,"Поделиться"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
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
                saveImage();
                return true;
            case R.id.import_image:
                getImageGallery();
                return true;
            case R.id.export_image:
                openSocialNetworks();
                Toast.makeText(this, R.string.export_picture, Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getImageGallery() {
        if (isGranted) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            someActivityResultLauncher.launch(intent);
        } else {
            askPermission();
        }
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
            Log.d(TAG, "Permission for reading and writing granted");
        }
    }


    /**
     * private class, which will run in new Thread, using Runnable and which can send UI messages
     * to the user in the main Thread, using Loop and addressing to Message queue
     */
    private class SaveThread extends HandlerThread {
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


    /**
     * This function provides user to save image on external storage, so it will be private, but also
     * it saves image go a gallery, so every app can get it and it will be public
     * Image saving in another thread, in a way not to make main thread too heavy
     */
    private void saveImage() {
        mWorkerThread = new SaveThread("myWorkerThread");
        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (drawView.hasPaths() && isGranted) {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "DemoPicture.jpg");
                    Bitmap bitmap = drawView.save();
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
                        MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "", "");
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
                            Snackbar snackbar = Snackbar.make(layout, "Image saved!", Snackbar.LENGTH_SHORT);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.show();
                        }
                    });


                } else {
                    Toast.makeText(getApplicationContext(), "Your painting is empty or you didn't grant a permission!", Toast.LENGTH_SHORT).show();
                }
            }
        };
        mWorkerThread.start();
        mWorkerThread.prepareHandler();
        mWorkerThread.postTask(task);
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