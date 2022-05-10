package com.example.ultimatesketchbookproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import Adapters.GalleryAdapter;
import Interfaces.RecyclerViewClickListener;

public class GalleryActivity extends AppCompatActivity {

    // todo: recycler view on touch listener
    private RecyclerViewClickListener listener;

    private static final String TAG = "GalleryActivity";

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
                Toast.makeText(GalleryActivity.this, "Touched!", Toast.LENGTH_SHORT).show();
            }
        };
    }
    private void setInitialData(ArrayList<File> fileArrayList ){
        for (File item: fileArrayList) {
//            String filename = item.toString().substring(0, item.toString().indexOf("Pictures/"));
            int index = item.toString().lastIndexOf("/");
            images.add(new Gallery (item.toString().substring(index + 1), "Буэнос-Айрес", loadFromFile(item)));
        }
    }

    private ArrayList<File> parseRootFolder() {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(path);
        ArrayList<File> files = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            try {
                for (File item: dir.listFiles()) {
                    Log.d(TAG, item + "");
                    if (item.isDirectory()) {
                        Log.d(TAG, item + " is a folder!" );
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

}