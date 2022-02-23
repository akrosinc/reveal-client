package org.smartregister.family.adapter;

import android.database.Cursor;

import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.commonregistry.CommonRepository;
import org.smartregister.cursoradapter.RecyclerViewPaginatedAdapter;
import org.smartregister.cursoradapter.RecyclerViewProvider;

public class FamilyRecyclerViewCustomAdapter<V extends RecyclerView.ViewHolder> extends RecyclerViewPaginatedAdapter<V> {

    private boolean showPagination;

    public FamilyRecyclerViewCustomAdapter(Cursor cursor, RecyclerViewProvider<RecyclerView.ViewHolder> listItemProvider, CommonRepository commonRepository, boolean showPagination) {
        super(cursor, listItemProvider, commonRepository);
        this.showPagination = showPagination;
    }

    @Override
    public boolean isFooter(int position) {
        if (showPagination) {
            return super.isFooter(position);
        }
        return false;
    }


    @Override
    public int getItemCount() {
        int count = super.getItemCount();
        if (showPagination || count <= 0) {
            return count;
        }
        return count - 1;
    }
}
