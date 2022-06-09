package org.smartregister.view.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.view.ListContract;

/**
 * @author rkodev
 */

public abstract class ListableViewHolder<T extends ListContract.Identifiable> extends RecyclerView.ViewHolder implements ListContract.AdapterViewHolder<T> {
    public ListableViewHolder(@NonNull View itemView) {
        super(itemView);
    }
}
