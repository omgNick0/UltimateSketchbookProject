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

import Interfaces.RecyclerViewClickListener;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private final LayoutInflater inflater;
    private final List<Gallery> gallery;
    private RecyclerViewClickListener listener;

    public GalleryAdapter(Context context, List<Gallery> gallery, RecyclerViewClickListener listener) {
        this.gallery = gallery;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    /**
     * Inner class which provides working with a recycler View
     * It sets image, name, date and works with clicks
     * !!! Warning !!! Android studio can offer you to make this class static
     * Don't do this! Or you won't can get access to listener variable;
     */

    // todo: longOnClickListener in the interface and deleting an item
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final ImageView imageView;
        final TextView nameView, dateView;

        ViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            imageView = view.findViewById(R.id.image);
            nameView = view.findViewById(R.id.name);
            dateView = view.findViewById(R.id.date);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(view, getAdapterPosition());
        }
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
        holder.imageView.setImageBitmap(state.getBitmap_image());
        holder.nameView.setText(state.getName());
        holder.dateView.setText(state.getDate());
//        String text = holder.itemView.getContext().getString(holder.nameView.);
    }

    @Override
    public int getItemCount() {
        return gallery.size();
    }
}