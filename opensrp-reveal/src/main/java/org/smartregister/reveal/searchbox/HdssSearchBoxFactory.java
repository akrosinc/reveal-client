package org.smartregister.reveal.searchbox;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.icu.util.Calendar;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.vijay.jsonwizard.widgets.NativeRadioButtonFactory;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;
import org.smartregister.CoreLibrary;
import org.smartregister.repository.HdssRepository;
import org.smartregister.reveal.R;
import org.smartregister.reveal.widget.RevealSearchBoxFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import timber.log.Timber;

public class HdssSearchBoxFactory extends RevealSearchBoxFactory {


    public static final String SEARCH_STRING = "searchString";
    public static final String GENDER = "gender";
    public static final String SEARCH_ONLINE = "search_online";
    public static final String DOB = "dob";
    public static final String NAME = "name";
    public static final String BATCH_NUMBER = "batchNumber";
    public static final String BATCH_SIZE = "batchSize";
    public static final String REVEAL_SEARCH_BOX = "reveal_search_box";
    private String gender;

    private String searchText = null;
    private String nameText = null;

    private JsonFormFragment formFragment;

    private String stepName;

    private boolean popup;

    private HdssRepository hdssRepository;

    private static int batchSize = 15;

    private int batchNumber = 0;

    private boolean isLoading = false;

    private RecyclerView recyclerView;

    private SearchItemAdapter searchItemAdapter;

    List<SearchItem> searchItems;

    EditText editTextDate;

    String localSearchDate;

    LinearLayout linearLayout;
    Dialog dialog;

    boolean searchOnlineChecked = false;
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY = 800;
    private Handler handler = new Handler();

    public HdssSearchBoxFactory() {
        this.hdssRepository = CoreLibrary.getInstance().context().getHdssRepository();
        HdssRepository.createSearchResultsTable(hdssRepository.getWritableDatabase());
    }

    @Override
    public List<View> attachJson(String stepName, Context context, JSONObject jsonObject, CommonListener listener, boolean popup, JsonFormFragment formFragment) throws JSONException {

        List<View> views = new ArrayList<>(1);
        LayoutInflater inflater = formFragment.getLayoutInflater();
        this.formFragment = formFragment;
        this.stepName = stepName;
        this.popup = popup;
        linearLayout = (LinearLayout) inflater.inflate(R.layout.search_box, null, false);
        addHeaderTextView(jsonObject);


        NativeEditText resultTextView = addTextView();
        addSearchEditText(resultTextView);
        addSearchNameEditText(resultTextView);

        addButton(context, jsonObject, resultTextView);
        addGenderSpinner(resultTextView);
        addClearButton(resultTextView);
        attachLogic(jsonObject, context, linearLayout);
        setTags(stepName, jsonObject, resultTextView, popup);

        addDatePickerEditText(context, resultTextView);
        addDateClearButton();
        addSearchOnlineCheckBox(context, linearLayout);

        views.add(linearLayout);
        formFragment.getJsonApi().addFormDataView(resultTextView);
        return views;
    }

    private void addSearchOnlineCheckBox(Context context, LinearLayout linearLayout) {
        CheckBox checkBox = linearLayout.findViewById(R.id.myCheckbox);
        checkBox.setOnCheckedChangeListener((v, isChecked) -> searchOnlineChecked = isChecked);
    }

    private void addDateClearButton() {
        Button clearButton = linearLayout.findViewById(R.id.buttonClear);
        clearButton.setOnClickListener(v -> {
            editTextDate.setText(null);
            localSearchDate = null;
        });
    }

    private void addDatePickerEditText(Context context, NativeEditText view) {
        editTextDate = linearLayout.findViewById(R.id.editTextDate);
        editTextDate.setOnClickListener(v -> showDatePickerDialog(context, view));

    }

    private void showDatePickerDialog(Context context, NativeEditText resultTextView) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, selectedYear, selectedMonth, selectedDay) -> {
            LocalDate searchDate = new LocalDate(selectedYear, selectedMonth + 1, selectedDay);

            DateTimeFormatter formatter = DateTimeFormat.forPattern("dd-MM-yyyy");
            String formattedDate = searchDate.toString(formatter);

            localSearchDate = formattedDate;

            editTextDate.setText(formattedDate);
            Timber.tag("hdsssearch").i("datePickerDialog");

//            enqueueSearchWork(getSearchRequest(), linearLayout.getContext(), resultTextView);

        }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void addGenderSpinner(NativeEditText resultTextView) {
        Spinner spinner = linearLayout.findViewById(R.id.gender_select);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    gender = null;
                } else {
                    gender = parent.getItemAtPosition(position).toString();
                }
                Timber.tag("hdsssearch").i("addGenderSpinner");
//                enqueueSearchWork(getSearchRequest(), linearLayout.getContext(), resultTextView);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gender = null;
            }
        });
    }

    private NativeEditText addTextView() {
        NativeEditText textView = linearLayout.findViewById(R.id.search_result_text);
        textView.setFocusable(false);
        textView.setFocusableInTouchMode(false);
        return textView;
    }

    private void addButton(Context context, JSONObject jsonObject, NativeEditText resultTextView) {
        Button button = linearLayout.findViewById(R.id.search_button);
        button.setOnClickListener(v -> {

            if (localSearchDate==null && ((searchText != null && !searchText.isEmpty() && searchText.length() < 3)
                    || (nameText != null  && !nameText.isEmpty() && nameText.length() < 3)))  {
                Toast.makeText(context, "must capture > 3 characters to search", Toast.LENGTH_LONG).show();
            } else {
                Timber.tag("hdsssearch").i("addButton");
                enqueueSearchWork(getSearchRequest(), v.getContext(), resultTextView);
            }
        });
    }

    private void addHeaderTextView(JSONObject jsonObject) {
        TextView textView = linearLayout.findViewById(R.id.header_text);
        String header = jsonObject.optString(HEADER);
        textView.setText(header);
    }

    private void addClearButton(NativeEditText resultTextView) {
        Button button = linearLayout.findViewById(R.id.clear_button);
        button.setOnClickListener(v -> {
            resultTextView.setText(null);

            Collection<View> formDataViews = formFragment.getJsonApi().getFormDataViews();
            for (View view : formDataViews) {
                if (view.getTag(R.id.key).equals("date_of_birth")) {
                    MaterialEditText textView = (MaterialEditText) view;
                    textView.setText("");
                }
                if (view.getTag(R.id.key).equals(GENDER)) {
                    MaterialEditText textView = (MaterialEditText) view;
                    textView.setText("");
                }
                if (view.getTag(R.id.key).equals("individual")) {
                    MaterialEditText textView = (MaterialEditText) view;
                    textView.setText("");
                }
            }

            formFragment.writeValue(stepName, "individual_ho", "", getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);
            formFragment.writeValue(stepName, "date_of_birth", "", getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);
            formFragment.writeValue(stepName, GENDER, "", getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);

        });
    }

    private void addSearchEditText(NativeEditText resultTextView) {
        EditText editText = linearLayout.findViewById(R.id.search_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {


                searchText = s.toString();

            }
        });
    }

    private void addSearchNameEditText(NativeEditText resultTextView) {
        EditText editText = linearLayout.findViewById(R.id.name_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(searchRunnable);
            }

            @Override
            public void afterTextChanged(Editable s) {


                nameText = s.toString();

            }
        });
    }

    @Override
    public void enqueueSearchWork(SearchRequest request, Context context, TextView resultTextView) {
        Timber.tag("hdsssearch").i("about to search");
        if (!isLoading) {
            if (searchItems != null) {
                searchItems.clear();
                if (searchItemAdapter != null) {
                    searchItemAdapter.notifyDataSetChanged();
                }
            }


            HdssSearchRequest hdssSearchRequest = (HdssSearchRequest) request;
            batchNumber = 0;
            Data inputData = getSearchRequest(hdssSearchRequest);
            Timber.tag("hdsssearch").i("submit search");
            isLoading = true;
            Toast.makeText(context, "Searching...", Toast.LENGTH_SHORT).show();
            OneTimeWorkRequest searchWorkRequest = new OneTimeWorkRequest.Builder(HdssSearchWorker.class).setInputData(inputData).build();
            WorkManager.getInstance(context).enqueue(searchWorkRequest);
            observeWorkInfo(context, resultTextView, searchWorkRequest);

        }
    }

    private void observeWorkInfo(Context context, TextView resultTextView, OneTimeWorkRequest searchWorkRequest) {
        WorkManager.getInstance(context)
                .getWorkInfoByIdLiveData(searchWorkRequest.getId())
                .observe(this.formFragment.getViewLifecycleOwner(), new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(WorkInfo workInfo) {
                        if (workInfo != null && workInfo.getState().isFinished()) {
                            // Handle the result or update the UI
                            Timber.tag("hdsssearch").i("got response");
                            handleWorkResult(workInfo, context, resultTextView);
                        }
                    }
                });
    }

    private void handleWorkResult(WorkInfo workInfo, Context context, TextView resultTextView) {
        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
            // Update the UI for success
            handleSucceeded(workInfo, context, resultTextView);

        } else if (workInfo.getState() == WorkInfo.State.FAILED) {
            handleFailure(workInfo, context);
        }
    }

    private static void handleFailure(WorkInfo workInfo, Context context) {
        Data outputData = workInfo.getOutputData();
        if (outputData.getString("error") != null) {
            Toast.makeText(context, outputData.getString("error"), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, "Error searching data", Toast.LENGTH_LONG).show();
        }
    }

    private void handleSucceeded(WorkInfo workInfo, Context context, TextView resultTextView) {
        Data outputData = workInfo.getOutputData();

        String json = outputData.getString("result");
        Gson gson = new Gson();

        if (searchItems != null) {
            searchItems.clear();
            if (searchItemAdapter != null) {
                searchItemAdapter.notifyDataSetChanged();
            }
        }
        List<SearchResponse> resultList = gson.fromJson(json, new TypeToken<List<SearchResponse>>() {
        }.getType());
        if (resultList != null && !resultList.isEmpty()) {
            searchItems = resultList.stream().map(HdssSearchBoxFactory::getSearchItem).collect(Collectors.toList());
            if (dialog == null) {
                dialog = setupDialog(context, resultTextView);
            }
            setupRecyclerView(context, resultTextView, dialog);

            dialog.show();
        } else {
            Toast.makeText(context, "No data return for search criteria", Toast.LENGTH_LONG).show();
        }
        isLoading = false;
    }

    private static @NonNull Dialog setupDialog(Context context, View anchorView) {
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

            int[] location = new int[2];
            anchorView.getLocationOnScreen(location);

            // Set the dialog position below the resultTextView
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.TOP | Gravity.START; // Align to top left
            params.x = location[0]; // X position (align with the left of the anchor)
            params.y = location[1] + anchorView.getHeight(); // Y position (just below the anchor)
            dialog.getWindow().setAttributes(params);
        }
        return dialog;
    }

    private void setupRecyclerView(Context context, TextView resultTextView, Dialog dialog) {
        recyclerView = dialog.findViewById(R.id.search_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (!isLoading && layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == searchItems.size() - 1) {
                    // Load next batch
                    batchNumber++;
                    Toast.makeText(context, "checking for more results", Toast.LENGTH_SHORT).show();
                    loadItems(batchNumber, batchSize, context);
                }
            }
        });

        searchItemAdapter = new SearchItemAdapter(context, searchItems, item -> handleItemOnClick(item, resultTextView, dialog));
        recyclerView.setAdapter(searchItemAdapter);
    }

    private void handleItemOnClick(SearchItem item, TextView resultTextView, Dialog dialog) {
        Collection<View> formDataViews = formFragment.getJsonApi().getFormDataViews();
        for (View view : formDataViews) {
            if (view.getTag(R.id.key).equals("date_of_birth")) {
                MaterialEditText textView = (MaterialEditText) view;
                textView.setText(item.getField5());
            }
            if (view.getTag(R.id.key).equals(GENDER)) {
                MaterialEditText textView = (MaterialEditText) view;
                textView.setText(item.getField4());
            }
            if (view.getTag(R.id.key).equals("individual")) {
                MaterialEditText textView = (MaterialEditText) view;
                textView.setText(item.getResult());
            }
            if (view.getTag(R.id.key).equals("name")) {
                MaterialEditText textView = (MaterialEditText) view;
                textView.setText(item.getField6());
            }

        }
        resultTextView.setText(item.getResult());
//        formFragment.writeValue(stepName, "individual_household_compound_search", item.getResult(), getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);


        DateTime dateOfBirth = DateTime.parse(item.getField5(), DateTimeFormat.forPattern("yyyy-MM-dd"));

        formFragment.writeValue(stepName, "date_of_birth", dateOfBirth.toString(), getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);
        formFragment.writeValue(stepName, GENDER, item.getField4(), getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);
        formFragment.writeValue(stepName, "individual", item.getResult(), getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);
        formFragment.writeValue(stepName, "name", item.getField6(), getOpenMrsEntityParent(), getOpenMrsEntity(), getOpenMrsEntityId(), popup);

        if (searchItems != null) {
            searchItems.clear();
            if (searchItemAdapter != null) {
                searchItemAdapter.notifyDataSetChanged();
            }
        }

        dialog.dismiss();
    }

    private @NonNull Data getSearchRequest(HdssSearchRequest hdssSearchRequest) {
        Data.Builder builder = new Data.Builder();

        if (hdssSearchRequest.getSearchString() != null) {
            builder.putString(SEARCH_STRING, hdssSearchRequest.getSearchString());
        }

        if (hdssSearchRequest.getGender() != null) {
            builder.putString(GENDER, hdssSearchRequest.getGender());
        }

        if (hdssSearchRequest.getDob() != null) {
            builder.putString(DOB, hdssSearchRequest.getDob());
        }

        if (hdssSearchRequest.getNameString() != null) {
            builder.putString(NAME, hdssSearchRequest.getNameString());
        }

        builder.putInt(BATCH_NUMBER, batchNumber);
        builder.putInt(BATCH_SIZE, batchSize);

        builder.putBoolean(SEARCH_ONLINE, searchOnlineChecked);

        Data inputData = builder.build();
        return inputData;
    }

    private static @NonNull SearchItem getSearchItem(SearchResponse result) {
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
        if (result.getName() != null) {
            searchItem.setLabel6("Name");
            searchItem.setField6(result.getName());
        }
        return searchItem;
    }

    private void loadItems(int batchNumber, int batchSize, Context context) {
        isLoading = true;

        new Handler().postDelayed(() -> {
            // Replace this with your actual data fetching logic
            List<SearchItem> nextBatch = fetchData(batchNumber, batchSize);
            searchItems.addAll(nextBatch);
            searchItemAdapter.notifyDataSetChanged();
            isLoading = false;
            if (nextBatch.isEmpty()) {
                Toast.makeText(context, "no more items", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "loaded new results", Toast.LENGTH_SHORT).show();
            }
        }, 2000);
    }

    private List<SearchItem> fetchData(int batchNumber, int batchSize) {
        List<SearchResponse> searchResultsInBatches = hdssRepository.getSearchResultsInBatches(batchSize, batchSize * batchNumber);

        return searchResultsInBatches.stream().map(HdssSearchBoxFactory::getSearchItem).collect(Collectors.toList());
    }

    @Override
    public SearchRequest getSearchRequest() {

        HdssSearchRequest request = new HdssSearchRequest();
        request.setGender(gender);
        request.setNameString(nameText);
        request.setSearchString(searchText);
        request.setDob(localSearchDate);


        return request;
    }

    @lombok.Data
    @Setter
    @Getter
    @AllArgsConstructor
    public static class SearchResponse implements Serializable {
        private String id;
        private String individualId;
        private String compoundId;
        private String householdId;
        private String dob;
        private String gender;
        private String name;
    }
}
