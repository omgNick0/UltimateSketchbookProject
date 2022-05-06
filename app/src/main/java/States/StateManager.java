package States;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.HashMap;

public class StateManager {

    public static HashMap<Class<? extends AppCompatActivity>, Object> map = new HashMap<>();

    public static  <T> T getState(AppCompatActivity activity, T defaultValue) {
        activity.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    activity.getLifecycle().removeObserver(this);
                    if (activity.isFinishing()) {
                        map.remove(activity.getClass());
                    }
                }
            }
        });
        if (map.containsKey(activity.getClass())) {
            return (T)map.get(activity.getClass());
        } else {
            map.put(activity.getClass(), defaultValue);
            return defaultValue;
        }
    }
}