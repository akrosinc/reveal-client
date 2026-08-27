package org.smartregister.reveal.widget;

import android.app.Dialog;
import android.content.Context;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.customviews.MaterialSpinner;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.presenters.JsonFormFragmentPresenter;
import com.vijay.jsonwizard.utils.ValidationStatus;
import com.vijay.jsonwizard.views.JsonFormFragmentView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.domain.Location;
import org.smartregister.domain.PlanDefinition;
import org.smartregister.repository.LocationRepository;
import org.smartregister.repository.PlanDefinitionRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.hierarchydatareceiver.HierarchyDataFetcher;
import org.smartregister.reveal.hierarchydatareceiver.LocationChildrenDataFetcher;
import org.smartregister.reveal.util.AppExecutors;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.reveal.view.RevealMapView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import timber.log.Timber;


public class HierarchyHttpDataRetrieverFactory implements FormWidgetFactory {

    public static final String HIERARCHY_HTTP_DATA_RETRIEVER = "hierarchy_http_data_retriever";


    private JsonApi jsonApi;

    private Dialog dialog;

    private LinearLayout mainLinearLayout;

    private JsonFormFragment formFragment;

    private String stepName;

    private String key;

    private boolean popup;

    TextView textView1;
    TextView textView2;
    TextView textView3;
    TextView textView4;
    TextView textView5;
    TextView textView6;
    TextView textView7;
    TextView textView8;
    TextView textView9;
    TextView textView10;
    TextView targetView;

    EditText mainTargetView;

    Map<Integer, LinearLayout> integerLinearLayoutMap;

    LinearLayout linearLayout1;
    LinearLayout linearLayout2;
    LinearLayout linearLayout3;
    LinearLayout linearLayout4;
    LinearLayout linearLayout5;
    LinearLayout linearLayout6;
    LinearLayout linearLayout7;
    LinearLayout linearLayout8;
    LinearLayout linearLayout9;
    LinearLayout linearLayout10;

    MaterialSpinner materialSpinner1;
    MaterialSpinner materialSpinner2;
    MaterialSpinner materialSpinner3;
    MaterialSpinner materialSpinner4;
    MaterialSpinner materialSpinner5;
    MaterialSpinner materialSpinner6;
    MaterialSpinner materialSpinner7;
    MaterialSpinner materialSpinner8;
    MaterialSpinner materialSpinner9;
    MaterialSpinner materialSpinner10;

    HierarchyDataFetcher hierarchyDataFetcher;

    private AppExecutors appExecutors;

    private LocationRepository locationRepository;

    private PlanDefinitionRepository planDefinitionRepository;

    private PreferencesUtil preferencesUtil;

    private String hierarchyId;

    private String target;

    private Context thisContext;

    TextView errorTextView;

    public HierarchyHttpDataRetrieverFactory() {
    }

    @NonNull
    @Override
    public Set<String> getCustomTranslatableWidgetFields() {
        return null;
    }


    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener, boolean popup) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, popup, formFragment);

    }

    @Override
    public List<View> getViewsFromJson(String stepName, Context context, JsonFormFragment formFragment, JSONObject jsonObject, CommonListener listener) throws Exception {
        return attachJson(stepName, context, jsonObject, listener, false, formFragment);
    }


    public List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, boolean popup, JsonFormFragment formFragment) throws JSONException {

        List<View> views = new ArrayList<>(1);
        LayoutInflater inflater = formFragment.getLayoutInflater();
        this.formFragment = formFragment;
        this.stepName = stepName;
        this.popup = popup;
        mainLinearLayout = (LinearLayout) inflater.inflate(R.layout.hierarchy_http_data_receiver_initiater, null, false);
        mainTargetView = mainLinearLayout.findViewById(R.id.result);
        mainTargetView.setEnabled(false);
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.hierarchy_http_data_receiver);
        targetView = dialog.findViewById(R.id.target);
        errorTextView = dialog.findViewById(R.id.error);

        String key = jsonObject.optString(JsonFormConstants.KEY);
        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT, null);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY, null);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID, null);

        appExecutors = RevealApplication.getInstance().getAppExecutors();
        locationRepository = RevealApplication.getInstance().getLocationRepository();
        planDefinitionRepository = RevealApplication.getInstance().getPlanDefinitionRepository();
        preferencesUtil = PreferencesUtil.getInstance();
        integerLinearLayoutMap = new HashMap<>();
        thisContext = context;

        setTags(stepName, jsonObject, mainTargetView, popup,context);

        String strRepositoryClass = jsonObject.optString(JsonFormConstants.MultiSelectUtils.REPOSITORY_CLASS);
        try {
            Class<?> aClass = Class.forName(strRepositoryClass);
            hierarchyDataFetcher = (HierarchyDataFetcher) aClass.newInstance();
        } catch (Exception e) {

        }

        getSpinnerViews(jsonObject);
        addSpinnerLogic(context);

        Button initiateDialogButton = mainLinearLayout.findViewById(R.id.initiate_dialog);
        initiateDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        Button saveFetchButton = dialog.findViewById(R.id.saveButton);
        saveFetchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorTextView.setText(R.string.you_must_select_a_cluster_in_order_to_save);
                if (mainTargetView.getText().toString().isEmpty()){
                    errorTextView.setVisibility(View.VISIBLE);
                } else {
                    errorTextView.setVisibility(View.GONE);
                    formFragment.writeValue(stepName,key,mainTargetView.getText().toString(),openMrsEntityParent,openMrsEntity,openMrsEntityId,popup);
                    dialog.dismiss();
                }
            }
        });

        ImageButton closeButton = dialog.findViewById(R.id.close_button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.TOP | Gravity.START; // Align to top left
            dialog.getWindow().setAttributes(params);
        }

        views.add(mainLinearLayout);

        return views;
    }

    protected void setTags(String stepName, JSONObject jsonObject, View view, boolean popup, Context context) throws JSONException {

        String openMrsEntityParent = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_PARENT, null);
        String openMrsEntity = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY, null);
        String openMrsEntityId = jsonObject.optString(JsonFormConstants.OPENMRS_ENTITY_ID, null);
        String relevance = jsonObject.optString(JsonFormConstants.RELEVANCE);

        JSONArray canvasIds = new JSONArray();
        mainLinearLayout.setId(ViewUtil.generateViewId());
        canvasIds.put(mainLinearLayout.getId());

        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.extraPopup, popup);
        view.setTag(R.id.canvas_ids, canvasIds.toString());
        view.setTag(R.id.key, jsonObject.getString(JsonFormConstants.KEY));
        view.setTag(R.id.type, jsonObject.getString(JsonFormConstants.TYPE));
        view.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
        view.setTag(R.id.openmrs_entity, openMrsEntity);
        view.setTag(R.id.openmrs_entity_id, openMrsEntityId);
        view.setTag(R.id.address, stepName + ":" + jsonObject.getString(JsonFormConstants.KEY));
        if (relevance != null) {
            Timber.tag("RevealMap").i("relevance is not null");
            view.setTag(com.vijay.jsonwizard.R.id.relevance, relevance);
            ((JsonApi) context).addSkipLogicView(view);
        }
        ((JsonApi) context).addFormDataView(mainTargetView);

    }
//    public static ValidationStatus validate(JsonFormFragmentView formFragmentView, HierarchyHttpDataRetrieverFactory mapView, JsonFormFragmentPresenter presenter) {
//
//    }
    private void addSpinnerLogic(Context context) {
        if (materialSpinner1 != null) {
            appExecutors.networkIO().execute(() -> {

                PlanDefinition planDefinitionById = planDefinitionRepository.findPlanDefinitionById(preferencesUtil.getCurrentPlanId());

                if (planDefinitionById != null) {
                    List<PlanDefinition.UseContext> useContexts = planDefinitionById.getUseContext();
                    if (!useContexts.isEmpty()) {
                        for (PlanDefinition.UseContext useContext : useContexts) {
                            if (useContext.getCode().equals(Constants.UseContextCode.LOCATION_HIERARCHY)) {
                                hierarchyId = useContext.getValueCodableConcept();
                            }
                        }
                    }
                }

                List<Pair<String, String>> locationLevel1 = new ArrayList<>();
                List<Location> highestLocationsList = locationRepository.getHighestLocationsList();
                locationLevel1.add(new Pair<>("unselected","Select..."));
                for (Location location : highestLocationsList) {
                    locationLevel1.add(new Pair<>(location.getId(), location.getProperties().getName()));
                }

                appExecutors.mainThread().execute(() -> {
                    if (!locationLevel1.isEmpty()) {
                        PairArrayAdapter stringAdapter1 = new PairArrayAdapter(context, locationLevel1);
                        materialSpinner1.setAdapter(stringAdapter1);
                        populateNextSpinner(materialSpinner2, R.id.linear_layout_2, materialSpinner1, context);
                        populateNextSpinner(materialSpinner3, R.id.linear_layout_3, materialSpinner2, context);
                        populateNextSpinner(materialSpinner4, R.id.linear_layout_4, materialSpinner3, context);
                        populateNextSpinner(materialSpinner5, R.id.linear_layout_5, materialSpinner4, context);
                        populateNextSpinner(materialSpinner6, R.id.linear_layout_6, materialSpinner5, context);
                        populateNextSpinner(materialSpinner7, R.id.linear_layout_7, materialSpinner6, context);
                        populateNextSpinner(materialSpinner8, R.id.linear_layout_8, materialSpinner7, context);
                        populateNextSpinner(materialSpinner9, R.id.linear_layout_9, materialSpinner8, context);
                        populateNextSpinner(materialSpinner10, R.id.linear_layout_10, materialSpinner9, context);
                    }

                });
            });
        }
    }

    private void populateNextSpinner(MaterialSpinner materialSpinnerNext, int linear_layout, MaterialSpinner materialSpinnerBefore, Context context) {

        LinearLayout linearLayout = this.dialog.findViewById(linear_layout);
//        integerLinearLayoutMap.put(linear_layout, linearLayout);
        if (materialSpinnerBefore != null) {
            materialSpinnerBefore.setOnItemSelectedListener(getListener(context, materialSpinnerNext, materialSpinnerBefore, linearLayout));
        }

    }

    private AdapterView.OnItemSelectedListener getListener(Context context, MaterialSpinner materialSpinnerNext, MaterialSpinner materialSpinnerBefore, LinearLayout linearLayout) {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                errorTextView.setVisibility(View.GONE);
                TextView textView = (TextView) view;
                String string = textView.getText().toString();
                Timber.tag("RevealMap").i("HierarchyHttpDataRetrieverFactory string <%s>", string);
                if (materialSpinnerBefore != null && !string.isEmpty()) {
                    String tag = materialSpinnerBefore.getTag(R.string.identifier).toString();
                    Timber.tag("RevealMap").i("the tag here: %s and target: %s", tag, target);
                    if (tag.equals(target) && !string.equals("Select...")) {
                        targetView.setText(textView.getText());
                        mainTargetView.setText(textView.getText());
                    } else {
                        targetView.setText("");
                        mainTargetView.setText("");
                    }
                    resetItemsBelow(materialSpinnerBefore);
                }

                if (materialSpinnerNext != null && !string.isEmpty() && !string.equals("Select...")) {
                    String parentId = textView.getTag(R.string.location_id).toString();
                    Timber.tag("RevealMap").i("HierarchyHttpDataRetrieverFactory tag <%s>", parentId);
                    appExecutors.networkIO().execute(() -> {
                        List<Pair<String, String>> values = new ArrayList<>();
                        try {
                            values = getChildrenFromParent(parentId);
                            if (!values.isEmpty()) {
                                values.add(0, new Pair<>("unselected", "Select..."));
                            }
                        } catch (Exception e) {
                            if (materialSpinnerBefore!=null) {
                                resetItemsBelow(materialSpinnerBefore);
                            }
                            Toast.makeText(context, "unable to get children data", Toast.LENGTH_LONG).show();
                        }
                        List<Pair<String, String>> finalValues = values;
                        appExecutors.mainThread().execute(() -> {
                            if (finalValues != null && !finalValues.isEmpty()) {
                                loadLevel(context, materialSpinnerNext, finalValues);
                                linearLayout.setVisibility(View.VISIBLE);
                            } else {
                                if (materialSpinnerBefore!=null) {
                                    resetItemsBelow(materialSpinnerBefore);
                                }
                                Toast.makeText(context, "no data returned", Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
    }

    private void resetItemsBelow(MaterialSpinner materialSpinnerBefore) {
        Object tagObj = materialSpinnerBefore.getTag(R.string.next_identifier);
        if (tagObj!=null){
            String tag = tagObj.toString();
            switch (tag){
                case "level2":
                    clearSpinnerAndView(R.id.material_spinner_2,R.id.linear_layout_2,tag);
                    clearSpinnerAndView(R.id.material_spinner_3,R.id.linear_layout_3,tag);
                    clearSpinnerAndView(R.id.material_spinner_4,R.id.linear_layout_4,tag);
                    clearSpinnerAndView(R.id.material_spinner_5,R.id.linear_layout_5,tag);
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level3":
                    clearSpinnerAndView(R.id.material_spinner_3,R.id.linear_layout_3,tag);
                    clearSpinnerAndView(R.id.material_spinner_4,R.id.linear_layout_4,tag);
                    clearSpinnerAndView(R.id.material_spinner_5,R.id.linear_layout_5,tag);
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level4":
                    clearSpinnerAndView(R.id.material_spinner_4,R.id.linear_layout_4,tag);
                    clearSpinnerAndView(R.id.material_spinner_5,R.id.linear_layout_5,tag);
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level5":
                    clearSpinnerAndView(R.id.material_spinner_5,R.id.linear_layout_5,tag);
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level6":
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_6,R.id.linear_layout_6,tag);
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level7":
                    clearSpinnerAndView(R.id.material_spinner_7,R.id.linear_layout_7,tag);
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level8":
                    clearSpinnerAndView(R.id.material_spinner_8,R.id.linear_layout_8,tag);
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level9":
                    clearSpinnerAndView(R.id.material_spinner_9,R.id.linear_layout_9,tag);
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
                case "level10":
                    clearSpinnerAndView(R.id.material_spinner_10,R.id.linear_layout_10,tag);
                    break;
            }
            Object tagIdentifierObj = materialSpinnerBefore.getTag(R.string.identifier);
            if (tagIdentifierObj!=null){
                String tagIdentifier = tagIdentifierObj.toString();
                if (!tagIdentifier.equals(target)){
                    targetView.setText("");
                    mainTargetView.setText("");
                }
            }
        }
    }

    private void clearSpinnerAndView( int spinnerId, int layoutId, String tag) {
        // Find and clear the spinner
        MaterialSpinner materialSpinner = dialog.findViewById(spinnerId);
        if (materialSpinner!=null) {

            materialSpinner.setAdapter(new PairArrayAdapter(thisContext, new ArrayList<>()));  // Clear the spinner's adapter
        }
        // Find and hide the corresponding LinearLayout
        LinearLayout linearLayout = dialog.findViewById(layoutId);
        if (linearLayout!=null) {
            linearLayout.setVisibility(View.GONE);  // Hide the layout
        }
    }

    private void loadLevel(Context context, MaterialSpinner materialSpinner, List<Pair<String, String>> values) {
        PairArrayAdapter stringAdapter1 = new PairArrayAdapter(context, values);
        materialSpinner.setAdapter(stringAdapter1);

    }

    private List<Pair<String, String>> getChildrenFromParent(String parentId) throws Exception {
        Map<String, String> requestDetails = new HashMap<>();
        requestDetails.put("parentId", parentId);
        requestDetails.put("hierarchyId", hierarchyId);
        Set<LocationChildrenDataFetcher.DataFetcherItem> children = hierarchyDataFetcher.getChildren(requestDetails);

        return children.stream().map(child -> new Pair<>(child.getId(), child.getName())).collect(Collectors.toList());
    }

    private void getSpinnerViews(JSONObject jsonObject) {
        String level1 = jsonObject.optString("level1");
        String level2 = jsonObject.optString("level2");
        String level3 = jsonObject.optString("level3");
        String level4 = jsonObject.optString("level4");
        String level5 = jsonObject.optString("level5");
        String level6 = jsonObject.optString("level6");
        String level7 = jsonObject.optString("level7");
        String level8 = jsonObject.optString("level8");
        String level9 = jsonObject.optString("level9");
        String level10 = jsonObject.optString("level10");

        target = jsonObject.optString("targetSelection");

        if (!level1.isEmpty()) {
            textView1 = dialog.findViewById(R.id.text_1);
            textView1.setText(level1);
            materialSpinner1 = dialog.findViewById(R.id.material_spinner_1);
            materialSpinner1.setTag(R.string.identifier,"level1");
            materialSpinner1.setTag(R.string.next_identifier, "level2");
            linearLayout1 = dialog.findViewById(R.id.linear_layout_1);
            linearLayout1.setVisibility(View.VISIBLE);

        }
        if (!level2.isEmpty()) {
            textView2 = dialog.findViewById(R.id.text_2);
            textView2.setText(level2);
            materialSpinner2 = dialog.findViewById(R.id.material_spinner_2);
            materialSpinner2.setTag(R.string.identifier,"level2");
            materialSpinner2.setTag(R.string.next_identifier, "level3");
        }
        if (!level3.isEmpty()) {
            textView3 = dialog.findViewById(R.id.text_3);
            textView3.setText(level3);
            materialSpinner3 = dialog.findViewById(R.id.material_spinner_3);
            materialSpinner3.setTag(R.string.identifier,"level3");
            materialSpinner3.setTag(R.string.next_identifier, "level4");
        }
        if (!level4.isEmpty()) {
            textView4 = dialog.findViewById(R.id.text_4);
            textView4.setText(level4);
            materialSpinner4 = dialog.findViewById(R.id.material_spinner_4);
            materialSpinner4.setTag(R.string.identifier,"level4");
            materialSpinner4.setTag(R.string.next_identifier, "level5");
        }
        if (!level5.isEmpty()) {
            textView5 = dialog.findViewById(R.id.text_5);
            textView5.setText(level5);
            materialSpinner5 = dialog.findViewById(R.id.material_spinner_5);
            materialSpinner5.setTag(R.string.identifier,"level5");
            materialSpinner5.setTag(R.string.next_identifier, "level6");
        }
        if (!level6.isEmpty()) {
            textView6 = dialog.findViewById(R.id.text_6);
            textView6.setText(level6);
            materialSpinner6 = dialog.findViewById(R.id.material_spinner_6);
            materialSpinner6.setTag(R.string.identifier,"level6");
            materialSpinner6.setTag(R.string.next_identifier, "level7");
        }
        if (!level7.isEmpty()) {
            textView7 = dialog.findViewById(R.id.text_7);
            textView7.setText(level7);
            materialSpinner7 = dialog.findViewById(R.id.material_spinner_7);
            materialSpinner7.setTag(R.string.identifier,"level7");
            materialSpinner7.setTag(R.string.next_identifier, "level8");
        }
        if (!level8.isEmpty()) {
            textView8 = dialog.findViewById(R.id.text_8);
            textView8.setText(level8);
            materialSpinner8 = dialog.findViewById(R.id.material_spinner_8);
            materialSpinner8.setTag(R.string.identifier,"level8");
            materialSpinner8.setTag(R.string.next_identifier, "level9");
        }
        if (!level9.isEmpty()) {
            textView9 = dialog.findViewById(R.id.text_9);
            textView9.setText(level9);
            materialSpinner9 = dialog.findViewById(R.id.material_spinner_9);
            materialSpinner9.setTag(R.string.identifier,"level9");
            materialSpinner9.setTag(R.string.next_identifier, "level10");
        }
        if (!level10.isEmpty()) {
            textView10 = dialog.findViewById(R.id.text_10);
            textView10.setText(level10);
            materialSpinner10 = dialog.findViewById(R.id.material_spinner_10);
            materialSpinner10.setTag(R.string.identifier,"level10");
        }


//        setLevelTextAndSpinner(dialog, level1, R.id.text_1, R.id.material_spinner_1, "level1","level2");
//        setLevelTextAndSpinner(dialog, level2, R.id.text_2, R.id.material_spinner_2, "level2","level3");
//        setLevelTextAndSpinner(dialog, level3, R.id.text_3, R.id.material_spinner_3, "level3","level4");
//        setLevelTextAndSpinner(dialog, level4, R.id.text_4, R.id.material_spinner_4, "level4","level5");
//        setLevelTextAndSpinner(dialog, level5, R.id.text_5, R.id.material_spinner_5, "level5","level6");
//        setLevelTextAndSpinner(dialog, level6, R.id.text_6, R.id.material_spinner_6, "level6","level7");
//        setLevelTextAndSpinner(dialog, level7, R.id.text_7, R.id.material_spinner_7, "level7","level8");
//        setLevelTextAndSpinner(dialog, level8, R.id.text_8, R.id.material_spinner_8, "level8","level9");
//        setLevelTextAndSpinner(dialog, level9, R.id.text_9, R.id.material_spinner_9, "level9","level10");
//        setLevelTextAndSpinner(dialog, level10, R.id.text_10, R.id.material_spinner_10, "level10", null);
    }
    public static class HierarchyTextView extends AppCompatTextView {
        public HierarchyTextView(Context context) {
            super(context);
        }
    }
}

class PairArrayAdapter extends ArrayAdapter<Pair<String, String>> {
    private Context context;
    private List<Pair<String, String>> values;

    public PairArrayAdapter(Context context, List<Pair<String, String>> values) {
        super(context, android.R.layout.simple_spinner_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view;
        String tagValue = values.get(position).first;
        Timber.tag("RevealMap").i("PairArrayAdapter getDropDownView tagValue 1 %s", tagValue);
        textView.setTag(R.string.location_id, tagValue);
        textView.setText(values.get(position).second); // Display second element of the Pair
        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        String tagValue = values.get(position).first;
        textView.setTag(R.string.location_id, tagValue);
        Timber.tag("RevealMap").i("PairArrayAdapter getView tagValue 2 %s", tagValue);
        textView.setText(values.get(position).second); // Display second element of the Pair
        return view;
    }
}

