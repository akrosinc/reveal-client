package org.smartregister.reveal.widget;


import android.content.Context;

import android.text.TextUtils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.reveal.searchbox.SearchItem;
import org.smartregister.reveal.searchbox.SearchRequest;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.Getter;

public abstract class RevealSearchBoxFactory implements FormWidgetFactory {
    public static final String HEADER = "header";

    @Getter
    private String openMrsEntityParent;
    @Getter
    private String openMrsEntity;
    @Getter
    private String openMrsEntityId;


    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, popup, formFragment);

    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {

        return attachJson(stepName, context, jsonObject, listener, false, formFragment);
    }

    public abstract List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener
            listener, boolean popup, JsonFormFragment formFragment) throws JSONException ;

    protected void attachLogic(JSONObject jsonObject, Context context, View linearLayout) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);

        attachRefreshLogic(context, relevance, calculation, constraints, linearLayout);
    }

    protected void setTags(String stepName, JSONObject jsonObject, View view, boolean popup) throws JSONException {

        this.openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT, null);
        this.openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY, null);
        this.openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID, null);

        JSONArray canvasIds = new JSONArray();
        view.setId(ViewUtil.generateViewId());
        canvasIds.put(view.getId());

        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.extraPopup, popup);
        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        view.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        view.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        view.setTag(R.id.openmrs_entity, openMrsEntity);
        view.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        view.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        view.setId(ViewUtil.generateViewId());
    }



    public abstract void enqueueSearchWork(SearchRequest request, Context context,TextView resultTextView);

    public abstract SearchRequest getSearchRequest();



    public void attachRefreshLogic(Context context, String relevance, String calculation, String constraints, View constraintLayout) {
        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            constraintLayout.setTag(com.vijay.jsonwizard.R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(constraintLayout);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            constraintLayout.setTag(com.vijay.jsonwizard.R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(constraintLayout);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            constraintLayout.setTag(com.vijay.jsonwizard.R.id.constraints, constraints);
            ((JsonApi) context).addCalculationLogicView(constraintLayout);
        }
    }


    @NonNull
    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return Collections.emptySet();
    }


    public static class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.SearchItemViewHolder> {

        private final List<SearchItem> searchItems;

        private final LayoutInflater inflater;

        private final OnItemClickListener onItemClickListener;


        public interface OnItemClickListener {
            void onItemClick(SearchItem item);
        }

        public SearchItemAdapter(Context context, List<SearchItem> searchItems, OnItemClickListener listener) {
            this.inflater = LayoutInflater.from(context);
            this.searchItems = searchItems;
            this.onItemClickListener = listener;
        }


        @NonNull
        @Override
        public SearchItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.search_result, parent, false);
            return new SearchItemViewHolder(view);
        }

        @Override
        public int getItemCount() {
            return searchItems.size();
        }

        @Override
        public void onBindViewHolder(@NonNull SearchItemViewHolder holder, int position) {
            SearchItem item = searchItems.get(position);

            holder.label1.setText(item.getLabel1());
            holder.field1.setText(item.getField1());

            if (item.getField2() == null || item.getLabel2() == null) {
                holder.layout2.setVisibility(View.GONE);
            } else {
                holder.field2.setText(item.getField2());
                holder.label2.setText(item.getLabel2());
            }

            if (item.getField3() == null || item.getLabel3() == null) {
                holder.layout3.setVisibility(View.GONE);
            } else {
                holder.field3.setText(item.getField3());
                holder.label3.setText(item.getLabel3());
            }

            if (item.getField4() == null || item.getLabel4() == null) {
                holder.layout4.setVisibility(View.GONE);
            } else {
                holder.field4.setText(item.getField4());
                holder.label4.setText(item.getLabel4());
            }

            if (item.getField5() == null || item.getLabel5() == null) {
                holder.layout5.setVisibility(View.GONE);
            } else {
                holder.field5.setText(item.getField5());
                holder.label5.setText(item.getLabel5());
            }

            if (item.getField6() == null || item.getLabel6() == null) {
                holder.layout6.setVisibility(View.GONE);
            } else {
                holder.field6.setText(item.getField6());
                holder.label6.setText(item.getLabel6());
            }

            if (item.getField7() == null || item.getLabel7() == null) {
                holder.layout7.setVisibility(View.GONE);
            } else {
                holder.field7.setText(item.getField7());
                holder.label7.setText(item.getLabel7());
            }

            holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(item));
        }

        public static class SearchItemViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layout1;
            TextView label1;
            TextView field1;

            LinearLayout layout2;
            TextView label2;
            TextView field2;

            LinearLayout layout3;
            TextView label3;
            TextView field3;

            LinearLayout layout4;
            TextView label4;
            TextView field4;

            LinearLayout layout5;
            TextView label5;
            TextView field5;

            LinearLayout layout6;
            TextView label6;
            TextView field6;


            LinearLayout layout7;
            TextView label7;
            TextView field7;

            public SearchItemViewHolder(@NonNull View itemView) {
                super(itemView);
                label1 = itemView.findViewById(R.id.search_result_label1);
                field1 = itemView.findViewById(R.id.search_result_field1);
                layout1 = itemView.findViewById(R.id.search_result_layout1);

                label2 = itemView.findViewById(R.id.search_result_label2);
                field2 = itemView.findViewById(R.id.search_result_field2);
                layout2 = itemView.findViewById(R.id.search_result_layout2);

                label3 = itemView.findViewById(R.id.search_result_label3);
                field3 = itemView.findViewById(R.id.search_result_field3);
                layout3 = itemView.findViewById(R.id.search_result_layout3);

                label4 = itemView.findViewById(R.id.search_result_label4);
                field4 = itemView.findViewById(R.id.search_result_field4);
                layout4 = itemView.findViewById(R.id.search_result_layout4);

                label5 = itemView.findViewById(R.id.search_result_label5);
                field5 = itemView.findViewById(R.id.search_result_field5);
                layout5 = itemView.findViewById(R.id.search_result_layout5);

                label6 = itemView.findViewById(R.id.search_result_label6);
                field6 = itemView.findViewById(R.id.search_result_field6);
                layout6 = itemView.findViewById(R.id.search_result_layout6);

                label7 = itemView.findViewById(R.id.search_result_label7);
                field7 = itemView.findViewById(R.id.search_result_field7);
                layout7 = itemView.findViewById(R.id.search_result_layout7);

            }
        }
    }

}
