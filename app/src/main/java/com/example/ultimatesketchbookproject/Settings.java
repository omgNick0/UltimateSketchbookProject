package com.example.ultimatesketchbookproject;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class Settings extends AppCompatActivity { // todo change language today - 19.05.22

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        RadioGroup group = findViewById(R.id.radio_group);
        Button confirm = findViewById(R.id.set_language);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(group.getCheckedRadioButtonId()==-1) {
                    Toast.makeText(getApplicationContext(), "Please select Gender", Toast.LENGTH_SHORT).show();
                } else {
                    // get selected radio button from radioGroup
                    int selectedId = group.getCheckedRadioButtonId();
                    // find the radiobutton by returned id
                    RadioButton selectedRadioButton = (RadioButton)findViewById(selectedId);

                    switch (selectedId) {
                        case 2131231244: Locale locale = new Locale("en"); changeLocale(locale);break;
                        case 2131231246: Locale locale1 = new Locale("ru");changeLocale(locale1);break;
                    }


                    Log.d(TAG, "id: " + selectedId);
                    Toast.makeText(getApplicationContext(), selectedRadioButton.getText().toString()+" is selected", Toast.LENGTH_SHORT).show();
                }

            }
        });


//        Log.d(TAG, curr_id + " ");
    }

    private void changeLocale(Locale locale) {
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources()
                .updateConfiguration(configuration,
                        getBaseContext()
                                .getResources()
                                .getDisplayMetrics());
        setTitle(R.string.app_name);



        Button btn = findViewById(R.id.set_language);
        btn.setText(R.string.confirm_language);
    }
}