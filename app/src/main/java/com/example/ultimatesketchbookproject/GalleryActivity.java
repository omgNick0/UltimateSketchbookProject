package com.example.ultimatesketchbookproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import Adapters.GalleryAdapter;

public class GalleryActivity extends AppCompatActivity {

    private static final String TAG = "GalleryActivity";

    ArrayList<Gallery> images = new ArrayList<Gallery>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setInitialData();
        RecyclerView recyclerView = findViewById(R.id.list);
        // создаем адаптер
        GalleryAdapter adapter = new GalleryAdapter(this, images);
        parseRootFolder();
        // устанавливаем для списка адаптер
        recyclerView.setAdapter(adapter);
    }
    private void setInitialData(){
        images.add(new Gallery ("Аргентина", "Буэнос-Айрес", R.drawable.argentina));
        images.add(new Gallery ("Колумбия", "Богота", R.drawable.columbia));
        images.add(new Gallery ("Уругвай", "Монтевидео", R.drawable.urugvai));
    }

    private void parseRootFolder() {
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File dir = new File(path);
        ArrayList<File> files = new ArrayList<>();
        if (dir.exists() && dir.isDirectory()) {
            try {
                for (File item: dir.listFiles()) {
                    if (item.isDirectory()) {
                        Log.d(TAG, item.toString() + " is a folder!" );
                    } else {
                        files.add(item);
                    }
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
                Log.d("Files", "dir is empty!");
            }
        }

        for (File item: files) {
            System.out.println("File - " + item);
        }
        // code here ...
    }

}