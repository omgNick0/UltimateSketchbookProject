package com.example.ultimatesketchbookproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import Adapters.GalleryAdapter;
import Interfaces.RecyclerViewClickListener;

public class GalleryActivity extends AppCompatActivity {

    // todo: recycler view on touch listener
    private RecyclerViewClickListener listener;

    private static final String TAG = "GalleryActivity";
    private String name = "";
    private ConstraintLayout layout;

    ArrayList<Gallery> images = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setOnClickListener();
        ArrayList<File> files = parseRootFolder(); // gets all files with full path and name
        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        GalleryAdapter adapter = new GalleryAdapter(this, images, listener);
        setInitialData(files);        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }

    private void setOnClickListener() {
        listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Gallery val = images.get(position);
                name = val.getName();
//                String name_new = dialogOpen();
//                val.setName(name_new);

                Log.d(TAG, val.getName()); // gets name item from recycler view
//                String name_new = dialogOpen();
//                val.setName(name_new);
//                dialogOpen(val.getName());
            }
        };
    }

    private void setInitialData(ArrayList<File> fileArrayList) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String date = format.format(new Date());
        for (File item : fileArrayList) {
//            String filename = item.toString().substring(0, item.toString().indexOf("Pictures/"));
            int index = item.toString().lastIndexOf("/");
            images.add(new Gallery(item.toString().substring(index + 1), date, loadFromFile(item)));
        }
    }

    private ArrayList<File> parseRootFolder() {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(path);
        ArrayList<File> files = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            try {
                for (File item : dir.listFiles()) {
                    Log.d(TAG, item + "");
                    if (item.isDirectory()) {
                        Log.d(TAG, item + " is a folder!");
                    } else if (item.toString().endsWith(".jpg")) {
                        files.add(item);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d("Files", "dir is empty!");
            }
        }
        return files;
    }

    private Bitmap loadFromFile(File file) {
        if (file.exists()) {

            Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            return myBitmap;

        } else {
            Log.d(TAG, "File doesn't exist!" + " " + file);
            return null;
        }
    }

//    private String dialogOpen() {
//        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(GalleryActivity.this);
//        builder.setTitle(R.string.change_painting_title);
//        final EditText input = new EditText(this);
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//        input.setText(name);
//        final String[] name_new = {""};
//
//        builder.setView(input);
//
//        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialogInterface, int i) {
//                // check empty String
//                if (!input.getText().toString().equals(" ")) {
//                    name_new[0] = input.getText().toString(); // get new name
//                } else { // if it's null
//                    Snackbar snackbar = Snackbar.make(layout, R.string.empty_name, Snackbar.LENGTH_SHORT);
//                    snackbar.setAction(R.string.ok, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            snackbar.dismiss();
//                        }
//                    });
//                    snackbar.show();
//                }
//
//            }
//        });
//        builder.setNegativeButton(R.string.cancel, null);
//        builder.show();
//        return name_new[0];
//    }
}