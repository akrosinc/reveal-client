package org.smartregister.reveal.searchbox;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.vijay.jsonwizard.customviews.NativeEditText;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.JsonApi;

import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.reveal.widget.RevealSearchBoxFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class HdssSearchBoxFactory extends RevealSearchBoxFactory {


    public static String REVEAL_SEARCH_BOX = "reveal_search_box";
    private String gender;

    private String searchText;

    private JsonFormFragment formFragment;

    private String stepName;

    private boolean popup;

    @Override
    public List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, boolean popup, JsonFormFragment formFragment) throws JSONException {

        List<View> views = new ArrayList<>(1);
        LayoutInflater inflater = formFragment.getLayoutInflater();
        this.formFragment = formFragment;

        this.stepName = stepName;
        this.popup = popup;

        LinearLayout linearLayout = (LinearLayout) inflater.inflate(R.layout.search_box, null, false);

        addHeaderTextView(linearLayout, jsonObject);

        addSpinner(linearLayout);

        addEditText(linearLayout);

        NativeEditText resultTextView = addTextView(linearLayout);

        addButton(context, linearLayout, jsonObject, resultTextView);

        addClearButton(linearLayout, resultTextView);

        attachLogic(jsonObject, context, linearLayout);

        setTags(stepName, jsonObject, resultTextView, popup);

        views.add(linearLayout);

        formFragment.getJsonApi().addFormDataView(resultTextView);
        return views;


    }

    private void addSpinner(LinearLayout view) {
        Spinner spinner = view.findViewById(R.id.gender_select);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {

                } else {
                    gender = parent.getItemAtPosition(position).toString();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private NativeEditText addTextView(LinearLayout view) {
        NativeEditText textView = view.findViewById(R.id.search_result_text);
        textView.setFocusable(false);
        textView.setFocusableInTouchMode(false);
        return textView;
    }

    private void addButton(Context context, LinearLayout view, JSONObject jsonObject, NativeEditText resultTextView) {
        Button button = view.findViewById(R.id.search_button);
        button.setOnClickListener(v -> {
            if (searchText.length() < 3) {
                Toast.makeText(context, "must capture > 3 characters to search", Toast.LENGTH_LONG).show();
            } else {
                enqueueSearchWork(getSearchRequest(), v.getContext(), resultTextView);
            }
        });
    }

    private void addHeaderTextView(LinearLayout view, JSONObject jsonObject) {
        TextView textView = view.findViewById(R.id.header_text);
        String header = jsonObject.optString(HEADER);
        textView.setText(header);
    }

    private void addClearButton(LinearLayout view, NativeEditText resultTextView) {
        Button button = view.findViewById(R.id.clear_button);
        button.setOnClickListener(v -> {
            resultTextView.setText(null);
        });
    }

    private void addEditText(LinearLayout view) {
        EditText editText = view.findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = s.toString();
            }
        });
    }

    @Override
    public void enqueueSearchWork(SearchRequest request, Context context, TextView resultTextView) {
        HdssSearchRequest hdssSearchRequest = (HdssSearchRequest) request;

        Data.Builder builder = new Data.Builder();

        if (hdssSearchRequest.getSearchString() != null) {
            builder.putString("searchString", hdssSearchRequest.getSearchString());
        }

        if (hdssSearchRequest.getGender() != null) {
            builder.putString("gender", hdssSearchRequest.getGender());
        }

        if (hdssSearchRequest.getDob() != null) {
            builder.putString("dob", hdssSearchRequest.getDob());
        }

        Data inputData = builder.build();

        OneTimeWorkRequest searchWorkRequest = new OneTimeWorkRequest.Builder(HdssSearchWorker.class).setInputData(inputData).build();

        WorkManager.getInstance(context).enqueue(searchWorkRequest);

        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(searchWorkRequest.getId())
                .observe(this.formFragment.getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            // Handle the result or update the UI
                            if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                                // Update the UI for success
                                Data outputData = workInfo.getOutputData();

                                String json = outputData.getString("result");
                                Gson gson = new Gson();

                                List<SearchResponse> resultList = gson.fromJson(json, new TypeToken<List<SearchResponse>>() {
                                }.getType());
                                if (resultList != null && !resultList.isEmpty()) {
                                    List<SearchItem> searchItems = resultList.stream().map(result -> {
                                        SearchItem searchItem = new SearchItem();
                                        if (result.getCompoundId() != null) {
                                            searchItem.setLabel1("Compound Id");
                                            searchItem.setField1(result.getCompoundId());
                                        }
                                        if (result.getHouseholdId() != null) {
                                            searchItem.setLabel2("Household Id");
                                            searchItem.setField2(result.getHouseholdId());
                                        }
                                        if (result.getIndividualId() != null) {
                                            searchItem.setResult(result.getIndividualId());
                                            searchItem.setLabel3("Individual Id");
                                            searchItem.setField3(result.getIndividualId());
                                        }
                                        if (result.getGender() != null) {
                                            searchItem.setLabel4("Gender");
                                            searchItem.setField4(result.getGender());
                                        }
                                        if (result.getDob() != null) {
                                            searchItem.setLabel5("Date of Birth");
                                            searchItem.setField5(result.getDob());
                                        }
                                        return searchItem;
                                    }).collect(Collectors.toList());

                                    Dialog dialog = new Dialog(context);
                                    dialog.setContentView(R.layout.search_dialog);

                                    TextView headerTextView = dialog.findViewById(R.id.dialogTitle);
                                    headerTextView.setText("Results");

                                    Button closebutton = dialog.findViewById(R.id.closeButton);
                                    closebutton.setText("Close");
                                    closebutton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            dialog.dismiss();
                                        }
                                    });


                                    if (dialog.getWindow() != null) {
                                        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    }

                                    RecyclerView recyclerView = dialog.findViewById(R.id.search_recycler_view);
                                    recyclerView.setLayoutManager(new LinearLayoutManager(context));

                                    SearchItemAdapter searchItemAdapter = new SearchItemAdapter(context, searchItems, new SearchItemAdapter.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(SearchItem item) {
                                            Collection<View> formDataViews = formFragment.getJsonApi().getFormDataViews();
                                            for (View view : formDataViews){
                                                if (view.getTag(R.id.key).equals("date_of_birth")){
                                                    MaterialEditText textView = (MaterialEditText) view;
                                                    textView.setText(item.getField5());
                                                }
                                                if (view.getTag(R.id.key).equals("gender")){
                                                    MaterialEditText textView = (MaterialEditText) view;
                                                    textView.setText(item.getField4());
                                                }
                                            }
                                            resultTextView.setText(item.getResult());
                                            formFragment.writeValue(stepName,"individual_household_compound_search",item.getResult(),getOpenMrsEntityParent(),getOpenMrsEntity(),getOpenMrsEntityId(),popup);
                                            formFragment.writeValue(stepName,"date_of_birth",item.getField5(),getOpenMrsEntityParent(),getOpenMrsEntity(),getOpenMrsEntityId(),popup);
                                            formFragment.writeValue(stepName,"gender",item.getField4(),getOpenMrsEntityParent(),getOpenMrsEntity(),getOpenMrsEntityId(),popup);

                                            dialog.dismiss();
                                        }
                                    });
                                    recyclerView.setAdapter(searchItemAdapter);

                                    dialog.show();
                                } else {
                                    Toast.makeText(context, "No data return for search criteria", Toast.LENGTH_LONG).show();
                                }

                            } else if (workInfo.getState() == WorkInfo.State.FAILED) {
                                Data outputData = workInfo.getOutputData();
                                if (outputData.getString("error")!=null){
                                    Toast.makeText(context, outputData.getString("error"), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(context, "Error searching data", Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });

    }

    @Override
    public SearchRequest getSearchRequest() {

        HdssSearchRequest request = new HdssSearchRequest();
        request.setGender(gender);
        request.setSearchString(searchText);

        return request;
    }

    @lombok.Data
    @Setter
    @Getter
    public static class SearchResponse implements Serializable {
        private String id;
        private String individualId;
        private String compoundId;
        private String householdId;
        private String dob;
        private String gender;
    }
}
