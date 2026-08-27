package org.smartregister.reveal.widget;

import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import timber.log.Timber;

public class RevealMultiSelectListLoadTask extends AsyncTask<Void, Void, List<RevealMultiSelectItem>> {

    private RevealMultiSelectListFactory multiSelectListFactory;
    private JSONObject jsonObject;
    private String currentAdapterKey;
    private ProgressDialog progressBar;

    @Override
    protected void onPreExecute() {
        try {
            progressBar.show();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public RevealMultiSelectListLoadTask(RevealMultiSelectListFactory multiSelectListFactory) {
        this.multiSelectListFactory = multiSelectListFactory;
        this.jsonObject = multiSelectListFactory.jsonObject;
        this.currentAdapterKey = multiSelectListFactory.currentAdapterKey;
        this.progressBar = new ProgressDialog(multiSelectListFactory.context);
        this.progressBar.setMessage(multiSelectListFactory.context.getString(R.string.loading_multi_select_list));
    }

    @Override
    protected List<RevealMultiSelectItem> doInBackground(Void... voids) {
        String source = jsonObject.optString(JsonFormConstants.MultiSelectUtils.SOURCE);
        List<MultiSelectItem> multiSelectItems_ = multiSelectListFactory.loadListItems(source);
        if (multiSelectItems_ == null) {
            return null;
        }

        List<RevealMultiSelectItem> multiSelectItems = multiSelectItems_.stream().map(item -> (RevealMultiSelectItem) item).collect(Collectors.toList());
        if (multiSelectItems == null) {
            return null;
        }
        String strGroupingsArray = jsonObject.optString(JsonFormConstants.MultiSelectUtils.GROUPINGS);
        boolean sort = jsonObject.optBoolean(JsonFormConstants.MultiSelectUtils.SORT);
        if (!StringUtils.isBlank(strGroupingsArray) && sort) {//no grouping without sorting
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(strGroupingsArray);
            } catch (JSONException e) {
                Timber.e(e);
            }
            RevealMultiSelectListUtils.addGroupings(multiSelectItems, jsonArray);
        }
        if (sort) {
            String sortClass = jsonObject.optString(JsonFormConstants.MultiSelectUtils.SORTING_CLASS);
            if (StringUtils.isBlank(sortClass)) {
                sortClass = JsonFormConstants.MultiSelectUtils.ALPHABET_SORTING;
            }
            try {
                Class<?> aClass = Class.forName(sortClass);
                Collections.sort(multiSelectItems, (Comparator<? super MultiSelectItem>) aClass.newInstance());
            } catch (IllegalAccessException e) {
                Timber.e(e);
            } catch (InstantiationException e) {
                Timber.e(e);
            } catch (ClassNotFoundException e) {
                Timber.e(e);
            }
        }
        return multiSelectItems;
    }


    protected void onPostExecute(List<RevealMultiSelectItem> multiSelectItems) {
        progressBar.dismiss();
        if (multiSelectItems != null) {
            RevealMultiSelectAccessory multiSelectListAccessory = RevealMultiSelectListFactory.getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
            multiSelectListAccessory.setItemList(multiSelectItems);
            multiSelectListFactory.updateListData(true);
        }
    }
}
