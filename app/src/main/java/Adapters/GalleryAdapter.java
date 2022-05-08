package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ultimatesketchbookproject.Gallery;
import com.example.ultimatesketchbookproject.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Gallery> gallery;

    public GalleryAdapter(Context context, List<Gallery> gallery) {
        this.gallery = gallery;
        this.inflater = LayoutInflater.from(context);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Gallery state = gallery.get(position);
        holder.imageView.setImageResource(state.getResourceId());
        holder.nameView.setText(state.getName());
        holder.dateView.setText(state.getDate());
    }

    @Override
    public int getItemCount() {
        return gallery.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView imageView;
        final TextView nameView, dateView;
        ViewHolder(View view){
            super(view);
            imageView = view.findViewById(R.id.image);
            nameView = view.findViewById(R.id.name);
            dateView = view.findViewById(R.id.date);
        }
    }
}