package Interfaces;

import android.view.View;

/**
 * Interface for working with recyclerView
 */
public interface RecyclerViewClickListener { //todo: onLongClickListener and deleting item
    void onClick(View v, int position);
}
