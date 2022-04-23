package Fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.example.ultimatesketchbookproject.MainActivity;
import com.example.ultimatesketchbookproject.R;

import Interfaces.PassDataInterface;
import top.defaults.colorpicker.ColorPickerPopup;


@SuppressLint("ValidFragment")
public class DialogFragment extends android.app.DialogFragment {

    /**
     * Based color from other fragment is taking, but the initial color number / color is wrong
     */

    private static final String TAG = "DialogFragment";
    private PassDataInterface passDataInterface;
    private ImageButton base_1, base_2, base_3, base_4, base_5, color_pic;

    public DialogFragment(PassDataInterface passDataInterface) {
        this.passDataInterface = passDataInterface;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        base_1 = view.findViewById(R.id.base_1);
        base_2 = view.findViewById(R.id.base_2);
        base_3 = view.findViewById(R.id.base_3);
        base_4 = view.findViewById(R.id.base_4);
        base_5 = view.findViewById(R.id.base_5);
        color_pic = view.findViewById(R.id.color_pic);

        // Override onClick method
        onClick(base_1, "#17A589");
        onClick(base_2, "#922B21");
        onClick(base_3, "#581845");
        onClick(base_4, "#B7950B");
        onClick(base_5, "#717D7E");

        // custom for color_pic
        color_pic.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                new ColorPickerPopup.Builder(getContext())
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
                                passDataInterface.onDataReceived(color);
                            }
                        });
            }
        });
    }

    private void onClick(ImageButton button, String color) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                passDataInterface.onDataReceived(color);
                Log.d(TAG, "Received!");
            }
        });
    }
}