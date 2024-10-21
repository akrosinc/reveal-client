package org.smartregister.reveal.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.adapter.MultiSelectListSelectedAdapter;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.MultiSelectItem;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.interfaces.MultiSelectListRepository;
import com.vijay.jsonwizard.utils.Utils;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;
import com.vijay.jsonwizard.widgets.MultiSelectListFactory;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.util.JsonFormUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import timber.log.Timber;

public class RevealMultiSelectListFactory extends MultiSelectListFactory {

    private static HashMap<String, RevealMultiSelectAccessory> multiSelectListAccessoryHashMap = new HashMap<>();
    private JsonFormFragment jsonFormFragment;

    public RevealMultiSelectListFactory(){

    }

    @Override
    public List<View> getViewsFromJson(@NonNull String stepName, @NonNull Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject, @NonNull CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, formFragment, jsonObject, listener, popup);
    }

    @Override
    public void handleClickEventOnListData(@NonNull MultiSelectItem multiSelectItem) {
        updateSelectedData(multiSelectItem, false);
        writeToForm();
        getAlertDialog().dismiss();

    }

    public void writeToForm() {
        RevealMultiSelectListUtils.writeToForm(currentAdapterKey, jsonFormFragment, getRevealMultiSelectListAccessoryHashMap());
    }

    @Override
    public List<MultiSelectItem> loadListItems(@Nullable String source) {
        if (StringUtils.isBlank(source)) {
            return RevealMultiSelectListUtils.loadOptionsFromJsonForm(jsonObject);
        } else {
            try {
                String strRepositoryClass = jsonObject.optString(JsonFormConstants.MultiSelectUtils.REPOSITORY_CLASS);
                Class<?> aClass = Class.forName(strRepositoryClass);
                MultiSelectListRepository multiSelectListRepository = (MultiSelectListRepository) aClass.newInstance();
                List<MultiSelectItem> fetchedMultiSelectItems = multiSelectListRepository.fetchData();

                if (fetchedMultiSelectItems == null || fetchedMultiSelectItems.isEmpty()) {
                    Activity activity = jsonFormFragment.getActivity();
                    if (activity != null) {
                        jsonFormFragment.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Utils.showToast(context, context.getString(com.vijay.jsonwizard.R.string.multi_select_list_msg_data_source_invalid));
                            }
                        });
                    }
                    return null;
                }
                return fetchedMultiSelectItems;
            } catch (IllegalAccessException e) {
                Timber.e(e);
            } catch (InstantiationException e) {
                Timber.e(e);
            } catch (ClassNotFoundException e) {
                Timber.e(e);
            }
            return null;
        }
    }

    public void updateSelectedData(@NonNull MultiSelectItem selectedData, boolean clearData) {

        if (clearData) {
            getRevealMultiSelectListSelectedAdapter().getData().clear();
        }
        List<MultiSelectItem> multiSelectItems = getRevealMultiSelectListSelectedAdapter().getData();
        if (multiSelectItems.contains(selectedData)) {
            Utils.showToast(context, String.format(context.getString(com.vijay.jsonwizard.R.string.multiselect_already_added_msg), selectedData.getText()));
            return;
        }
        getRevealMultiSelectListSelectedAdapter().getData().add(selectedData);
        Utils.showToast(context, selectedData.getText() + " " + context.getString(com.vijay.jsonwizard.R.string.multiselect_msg_on_item_added));
        getRevealMultiSelectListSelectedAdapter().notifyDataSetChanged();
    }

    public static ValidationStatus validate(JsonFormFragmentView fragmentView, RelativeLayout multiselectLayout) {
        String error = "Required field";
        JSONObject currentJsonState = null;
        try {
            currentJsonState = new JSONObject(fragmentView.getCurrentJsonState());
            JSONArray fields = JsonFormUtils.fields(currentJsonState);
            JSONObject fieldToCheck = null;
            for (int i = 0; i < fields.length(); i++) {
                JSONObject field = fields.getJSONObject(i);
                if (field.optString("type").equals("reveal_multi_select_list")) {
                    fieldToCheck = field;
                    break;
                }
            }
            if (fieldToCheck.optString("value").equals("[]")) {
                return new ValidationStatus(false, error, fragmentView, multiselectLayout);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ValidationStatus(true, null, fragmentView, multiselectLayout);
    }

    private List<View> attachJson(@NonNull String stepName, @NonNull Context context, @NonNull JsonFormFragment formFragment, @NonNull JSONObject jsonObject,
                                  @NonNull CommonListener listener, boolean popup) throws JSONException {
        this.jsonFormFragment = formFragment;
        this.jsonObject = jsonObject;
        this.currentAdapterKey = jsonObject.optString(JsonFormConstants.KEY);
        this.context = context;
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID);
        prepareMultiSelectHashMap(stepName, popup, openMrsEntity, openMrsEntityParent, openMrsEntityId);



        RelativeLayout actionView = createActionView(context);
        setUpDialog(context,actionView);
        RecyclerView recyclerView = createSelectedRecyclerView(context);

        String strMaxSelectable = jsonObject.optString(JsonFormConstants.MultiSelectUtils.MAX_SELECTABLE);
        int maxSelectable;
        if (!TextUtils.isEmpty(strMaxSelectable)) {
            maxSelectable = Integer.parseInt(strMaxSelectable);
            MultiSelectListSelectedAdapter revealMultiSelectListSelectedAdapter = getRevealMultiSelectListSelectedAdapter();
            List<MultiSelectItem> multiSelectItems = revealMultiSelectListSelectedAdapter.getData();
            if ((multiSelectItems.size() >= maxSelectable) && !multiSelectItems.isEmpty()) {
                actionView.setVisibility(View.GONE);
            }
        }

        List<View> views = new ArrayList<View>(Arrays.asList(recyclerView, actionView));

        populateTags(actionView, stepName, popup, openMrsEntity, openMrsEntityParent, openMrsEntityId);

        prepareViewChecks(actionView, context);
        addRequiredValidator(actionView, jsonObject);
        ((JsonApi) context).addFormDataView(actionView);
        return views;
    }
    private void populateTags(@NonNull View view, @NonNull String stepName, boolean popUp, String openmrsEntity, String openmrsEntityParent, String openmrsEntityId) {
        JSONArray canvasIds = new JSONArray();
        view.setId(ViewUtil.generateViewId());
        canvasIds.put(view.getId());
        view.setTag(com.vijay.jsonwizard.R.id.canvas_ids, canvasIds.toString());
        view.setTag(com.vijay.jsonwizard.R.id.key, jsonObject.optString(JsonFormConstants.KEY));
        view.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_parent, openmrsEntityParent);
        view.setTag(com.vijay.jsonwizard.R.id.openmrs_entity, openmrsEntity);
        view.setTag(com.vijay.jsonwizard.R.id.openmrs_entity_id, openmrsEntityId);
        view.setTag(com.vijay.jsonwizard.R.id.type, jsonObject.optString(JsonFormConstants.TYPE));
        view.setTag(com.vijay.jsonwizard.R.id.extraPopup, popUp);
        view.setTag(com.vijay.jsonwizard.R.id.address, stepName + ":" + jsonObject.optString(JsonFormConstants.KEY));
        view.setTag(com.vijay.jsonwizard.R.id.is_multiselect_relative_layout, true);
        view.setTag(R.id.is_reveal_multiselect_relative_layout, true);
    }
    private void addRequiredValidator(RelativeLayout relativeLayout, JSONObject jsonObject) throws JSONException {
        JSONObject requiredObject = jsonObject.optJSONObject(JsonFormConstants.V_REQUIRED);
        if (requiredObject != null) {
            boolean requiredValue = requiredObject.getBoolean(JsonFormConstants.VALUE);
            if (Boolean.TRUE.equals(requiredValue)) {
                relativeLayout.setTag(R.id.error, requiredObject.optString(JsonFormConstants.ERR, null));
            }
        }
    }


    private void prepareViewChecks(@NonNull RelativeLayout view, @NonNull Context context) {
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);
        String constraints = jsonObject.optString(JsonFormConstants.CONSTRAINTS);
        String calculation = jsonObject.optString(JsonFormConstants.CALCULATION);

        if (!TextUtils.isEmpty(relevance) && context instanceof JsonApi) {
            view.setTag(R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }

        if (!TextUtils.isEmpty(constraints) && context instanceof JsonApi) {
            view.setTag(R.id.constraints, constraints);
            ((JsonApi) context).addConstrainedView(view);
        }

        if (!TextUtils.isEmpty(calculation) && context instanceof JsonApi) {
            view.setTag(R.id.calculation, calculation);
            ((JsonApi) context).addCalculationLogicView(view);
        }
    }


    protected RelativeLayout createActionView(@NonNull Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final RelativeLayout relativeLayout = (RelativeLayout) layoutInflater.inflate(R.layout.multi_select_list_action_layout, null);
        relativeLayout.setTag(R.id.key, currentAdapterKey);
        Button btn_multi_select_action = relativeLayout.findViewById(R.id.btn_multi_select_action);
        btn_multi_select_action.setText(jsonObject.optString(JsonFormConstants.MultiSelectUtils.BUTTON_TEXT));
        btn_multi_select_action.setTypeface(Typeface.DEFAULT);
        btn_multi_select_action.setTag(R.id.maxSelectable, jsonObject.optString(JsonFormConstants.MultiSelectUtils.MAX_SELECTABLE));
        btn_multi_select_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strMaxSelectable = (String) v.getTag(R.id.maxSelectable);
                currentAdapterKey = (String) relativeLayout.getTag(R.id.key);
                int maxSelectable;
                if (!TextUtils.isEmpty(strMaxSelectable)) {
                    maxSelectable = Integer.parseInt(strMaxSelectable);
                    MultiSelectListSelectedAdapter revealMultiSelectListSelectedAdapter = getRevealMultiSelectListSelectedAdapter();
                    List<MultiSelectItem> multiSelectItems = revealMultiSelectListSelectedAdapter.getData();
                    if ((multiSelectItems.size() >= maxSelectable) && !multiSelectItems.isEmpty()) {
                        return;
                    }
                }
                updateListData(true);
                showListDataDialog();
            }
        });

        return relativeLayout;
    }

    @Override
    public void updateListData(boolean clearData) {
        RevealMultiSelectAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (clearData) {
            getRevealMultiSelectListAdapter().getData().clear();
        }
        getRevealMultiSelectListAdapter().getData().addAll(multiSelectListAccessory.getItemList());
        getRevealMultiSelectListAdapter().notifyDataSetChanged();
    }

    private void showListDataDialog() {
        if (getAlertDialog() != null) {
            getAlertDialog().show();
        }
    }

    public AlertDialog getAlertDialog() {
        RevealMultiSelectAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            return multiSelectListAccessory.getAlertDialog();
        }
        return null;
    }

    public MultiSelectListSelectedAdapter getRevealMultiSelectListSelectedAdapter() {
        HashMap<String, RevealMultiSelectAccessory> revealMultiSelectListAccessoryHashMap = getRevealMultiSelectListAccessoryHashMap();
        RevealMultiSelectAccessory revealMultiSelectAccessory = revealMultiSelectListAccessoryHashMap.get(currentAdapterKey);

        if (revealMultiSelectAccessory != null) {
            return revealMultiSelectAccessory.getSelectedAdapter();
        }
        return null;
    }

    private void setUpDialog(final Context context,RelativeLayout actionView) {
        if (jsonFormFragment == null) {
            return;
        }
        LayoutInflater inflater = jsonFormFragment.getLayoutInflater();
        View view = inflater.inflate(com.vijay.jsonwizard.R.layout.multiselectlistdialog, null);
        ImageView imgClose = view.findViewById(com.vijay.jsonwizard.R.id.multiSelectListCloseDialog);
        TextView txtMultiSelectListDialogTitle = view.findViewById(com.vijay.jsonwizard.R.id.multiSelectListDialogTitle);
        txtMultiSelectListDialogTitle.setText(jsonObject.optString(JsonFormConstants.MultiSelectUtils.DIALOG_TITLE));
        SearchView searchViewMultiSelect = view.findViewById(com.vijay.jsonwizard.R.id.multiSelectListSearchView);
        searchViewMultiSelect.setQueryHint(jsonObject.optString(JsonFormConstants.MultiSelectUtils.SEARCH_HINT));
        final RecyclerView recyclerView = view.findViewById(com.vijay.jsonwizard.R.id.multiSelectListRecyclerView);
        AlertDialog.Builder builder = new AlertDialog.Builder(context, com.vijay.jsonwizard.R.style.FullScreenDialogStyle);
        builder.setView(view);
        builder.setCancelable(true);
        final AlertDialog alertDialog = builder.create();
        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();


            }
        });

        final RevealMultiSelectListAdapter multiSelectListAdapter = getRevealMultiSelectListAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(multiSelectListAdapter);
        searchViewMultiSelect.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                multiSelectListAdapter.getFilter().filter(newText);
                return true;
            }
        });

        multiSelectListAdapter.setOnClickListener(new RevealMultiSelectListAdapter.ClickListener() {
            @Override
            public void onItemClick(View view) {
                int position = recyclerView.getChildLayoutPosition(view);
                handleClickEventOnListData(getRevealMultiSelectListAdapter().getItemAt(position));
                String strMaxSelectable = jsonObject.optString(JsonFormConstants.MultiSelectUtils.MAX_SELECTABLE);
                int maxSelectable;
                if (!TextUtils.isEmpty(strMaxSelectable)) {
                    maxSelectable = Integer.parseInt(strMaxSelectable);
                    MultiSelectListSelectedAdapter revealMultiSelectListSelectedAdapter = getRevealMultiSelectListSelectedAdapter();
                    List<MultiSelectItem> multiSelectItems = revealMultiSelectListSelectedAdapter.getData();
                    if ((multiSelectItems.size() >= maxSelectable) && !multiSelectItems.isEmpty()) {
                        actionView.setVisibility(View.GONE);
                    }
                }
            }
        });

        RevealMultiSelectAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        multiSelectListAccessory.setAlertDialog(alertDialog);
        updateRevealMultiSelectListAccessoryHashMap(multiSelectListAccessory);
    }

    protected List<RevealMultiSelectItem> prepareRevealListData() {
        new RevealMultiSelectListLoadTask(this).execute();
        return new ArrayList<>();
    }

    private void prepareMultiSelectHashMap(@NonNull String stepName, boolean popup, String openmrsEntity, String openmrsEntityParent, String openmrsEntityId) {
        RevealMultiSelectAccessory multiSelectListAccessory = new RevealMultiSelectAccessory(
                new MultiSelectListSelectedAdapter(new ArrayList<MultiSelectItem>(), this),
                new RevealMultiSelectListAdapter(prepareRevealListData()),
                null,
                new ArrayList<MultiSelectItem>(),
                new ArrayList<RevealMultiSelectItem>(),null);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JsonFormConstants.STEPNAME, stepName);
            jsonObject.put(JsonFormConstants.IS_POPUP, popup);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_PARENT, openmrsEntityParent);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY, openmrsEntity);
            jsonObject.put(JsonFormConstants.OPENMRS_ENTITY_ID, openmrsEntityId);
            multiSelectListAccessory.setFormAttributes(jsonObject);
        } catch (JSONException e) {
            Timber.e(e);
        }

        updateRevealMultiSelectListAccessoryHashMap(multiSelectListAccessory);
    }

    @Override
    protected RecyclerView createSelectedRecyclerView(@NonNull Context context) {
        List<MultiSelectItem> multiSelectItems = prepareSelectedData();
        MultiSelectListSelectedAdapter multiSelectListSelectedAdapter = new MultiSelectListSelectedAdapter(multiSelectItems, this);

        RevealMultiSelectAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        multiSelectListAccessory.setSelectedAdapter(multiSelectListSelectedAdapter);
        updateRevealMultiSelectListAccessoryHashMap(multiSelectListAccessory);

        writeToForm();

        final RecyclerView recyclerView = new RecyclerView(context);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation());
        dividerItemDecoration.setDrawable(context.getResources().getDrawable(com.vijay.jsonwizard.R.drawable.multi_select_list_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(multiSelectListSelectedAdapter);
        return recyclerView;
    }

    private void updateRevealMultiSelectListAccessoryHashMap(@NonNull RevealMultiSelectAccessory multiSelectListAccessory) {
        getRevealMultiSelectListAccessoryHashMap().put(currentAdapterKey, multiSelectListAccessory);
    }

    public RevealMultiSelectListAdapter getRevealMultiSelectListAdapter() {
        RevealMultiSelectAccessory multiSelectListAccessory = getRevealMultiSelectListAccessoryHashMap().get(currentAdapterKey);
        if (multiSelectListAccessory != null) {
            return multiSelectListAccessory.getListAdapter();
        }
        return null;
    }

    public static HashMap<String, RevealMultiSelectAccessory> getRevealMultiSelectListAccessoryHashMap() {
        return multiSelectListAccessoryHashMap;
    }
}