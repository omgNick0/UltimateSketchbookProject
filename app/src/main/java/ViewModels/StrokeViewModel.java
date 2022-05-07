package ViewModels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.ultimatesketchbookproject.Stroke;

import java.util.ArrayList;

public class StrokeViewModel extends ViewModel {
    MutableLiveData<ArrayList<Stroke>> lines = new MutableLiveData<>();

    MutableLiveData<Integer> sliderState = new MutableLiveData<>();

    MutableLiveData<Integer> color = new MutableLiveData<>();

    public MutableLiveData<Integer> getColor() {
        return color;
    }

    public void setColor(Integer color) {
        this.color.postValue(color);
    }

    public LiveData<ArrayList<Stroke>> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Stroke> lines) {
        this.lines.postValue(lines);
    }


    public LiveData<Integer> getSliderState() {
        return sliderState;
    }

    public void setSliderState(Integer state) {
        this.sliderState.postValue(state);
    }


    @Override
    protected void onCleared() {
        // clean up resources
        // non-realise
    }
// TODO: Implement the ViewModel
}