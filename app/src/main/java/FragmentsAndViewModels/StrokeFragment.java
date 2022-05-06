package FragmentsAndViewModels;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.ultimatesketchbookproject.R;

public class StrokeFragment extends Fragment {

    private StrokeViewModel mViewModel;

    public static StrokeFragment newInstance() {
        return new StrokeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(StrokeViewModel.class);
        return inflater.inflate(R.layout.fragment_stroke, container, false);
    }

}