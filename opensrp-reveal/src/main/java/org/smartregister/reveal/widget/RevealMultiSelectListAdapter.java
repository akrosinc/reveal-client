package org.smartregister.reveal.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.reveal.R;

import java.util.ArrayList;
import java.util.List;

public class RevealMultiSelectListAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {
    private List<RevealMultiSelectItem> data;
    private List<RevealMultiSelectItem> origData;
    private static RevealMultiSelectListAdapter.ClickListener clickListener;

    public RevealMultiSelectListAdapter(List<RevealMultiSelectItem> data) {
        this.data = data;
        this.origData = data;
    }

    public List<RevealMultiSelectItem> getData() {
        return data;
    }

    public List<RevealMultiSelectItem> getOrigData() {
        return origData;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.revealmultiselectitem, parent, false);

            return new RevealMultiSelectListAdapter.SectionViewHolder(itemView);
        } else if (viewType == 1) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mutiselectlistitem_head, parent, false);
            return new RevealMultiSelectListAdapter.SectionTitleViewHolder(itemView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RevealMultiSelectItem multiSelectItem = data.get(position);
        if (multiSelectItem.getValue() != null) {
            RevealMultiSelectListAdapter.SectionViewHolder sectionViewHolder = (RevealMultiSelectListAdapter.SectionViewHolder) holder;

            if (multiSelectItem.getText()!=null){
                sectionViewHolder.txtMultiSelectItem.setText(multiSelectItem.getText());
            }else{
                sectionViewHolder.txtMultiSelectItem.setVisibility(View.GONE);
            }

            if (multiSelectItem.getText2()!=null){
                sectionViewHolder.txt2MultiSelectItem.setText(multiSelectItem.getText2());
            }else{
                sectionViewHolder.txt2MultiSelectItem.setVisibility(View.GONE);
            }


            if (multiSelectItem.getText3()!=null){
                sectionViewHolder.txt3MultiSelectItem.setText(multiSelectItem.getText3());
            }else{
                sectionViewHolder.txt3MultiSelectItem.setVisibility(View.GONE);
            }

            if (multiSelectItem.getLabel1()!=null){
                sectionViewHolder.label1.setText(multiSelectItem.getLabel1());
            }else{
                sectionViewHolder.label1.setVisibility(View.GONE);
            }
            if (multiSelectItem.getLabel2()!=null){
                sectionViewHolder.label2.setText(multiSelectItem.getLabel2());
            }else{
                sectionViewHolder.label2.setVisibility(View.GONE);
            }
            if (multiSelectItem.getLabel2()!=null){
                sectionViewHolder.label3.setText(multiSelectItem.getLabel3());
            }else{
                sectionViewHolder.label3.setVisibility(View.GONE);
            }

        } else {
            RevealMultiSelectListAdapter.SectionTitleViewHolder sectionViewHolder = (RevealMultiSelectListAdapter.SectionTitleViewHolder) holder;
            sectionViewHolder.txtMultiSelectHeader.setText(multiSelectItem.getText());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<RevealMultiSelectItem> filteredMultiSelectItems = null;
                if (constraint.length() == 0) {
                    filteredMultiSelectItems = origData;
                } else {
                    filteredMultiSelectItems = getFilteredResults(constraint.toString());
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredMultiSelectItems;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                data = (List<RevealMultiSelectItem>) results.values;
                notifyDataSetChanged();
            }

            protected List<RevealMultiSelectItem> getFilteredResults(String constraint) {
                List<RevealMultiSelectItem> results = new ArrayList<>();

                for (RevealMultiSelectItem item : origData) {
                    if (item.getText()!=null && item.getText().toLowerCase().contains(constraint.toLowerCase())) {
                        results.add(item);
                    }
                    if (item.getText2()!=null && item.getText2().toLowerCase().contains(constraint.toLowerCase())) {
                        results.add(item);
                    }
                    if (item.getText3()!=null && item.getText3().toLowerCase().contains(constraint.toLowerCase())) {
                        results.add(item);
                    }
                }
                return results;
            }
        };
    }

    public class SectionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtMultiSelectItem;
        private TextView txt2MultiSelectItem;
        private TextView txt3MultiSelectItem;

        private TextView label1;
        private TextView label2;
        private TextView label3;

        private SectionViewHolder(View view) {
            super(view);
            txtMultiSelectItem = view.findViewById(R.id.txtMultiSelectItem);
            txt2MultiSelectItem = view.findViewById(R.id.txt2MultiSelectItem);
            txt3MultiSelectItem = view.findViewById(R.id.txt3MultiSelectItem);

            label1 = view.findViewById(R.id.lbl1MultiSelectItem);
            label2 = view.findViewById(R.id.lbl2MultiSelectItem);
            label3 = view.findViewById(R.id.lbl3MultiSelectItem);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                clickListener.onItemClick(view);
            }
        }
    }

    public class SectionTitleViewHolder extends RecyclerView.ViewHolder {
        private TextView txtMultiSelectHeader;

        private SectionTitleViewHolder(View view) {
            super(view);
            txtMultiSelectHeader = view.findViewById(R.id.txtMultiSelectHeader);
        }
    }

    @Override
    public int getItemViewType(int position) {
        RevealMultiSelectItem multiSelectItem = data.get(position);
        if (multiSelectItem.getValue() != null) {
            return 0;
        } else {
            return 1;
        }
    }

    public RevealMultiSelectItem getItemAt(int position) {
        return data.get(position);
    }

    public interface ClickListener {
        void onItemClick(View view);
    }

    public void setOnClickListener(RevealMultiSelectListAdapter.ClickListener onClickListener) {
        RevealMultiSelectListAdapter.clickListener = onClickListener;
    }

}
