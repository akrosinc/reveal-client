package org.smartregister.reveal.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.smartregister.domain.Task;
import org.smartregister.reveal.R;
import org.smartregister.reveal.adapter.StructureTaskAdapter;
import org.smartregister.reveal.contract.StructureTasksContract;
import org.smartregister.reveal.model.StructureTaskDetails;
import org.smartregister.reveal.presenter.StructureTasksPresenter;
import org.smartregister.reveal.util.LocationUtils;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.Utils;

import java.util.List;

import io.ona.kujaku.listeners.BaseLocationListener;
import io.ona.kujaku.utils.Constants;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;
import static org.smartregister.reveal.util.Constants.JSON_FORM_PARAM_JSON;
import static org.smartregister.reveal.util.Constants.REQUEST_CODE_GET_JSON_FRAGMENT;

/**
 * Created by samuelgithengi on 4/8/19.
 */
public class StructureTasksFragment extends Fragment implements StructureTasksContract.View {

    private static final String TAG = StructureTasksFragment.class.getName();
    private RecyclerView taskRecyclerView;
    private StructureTaskAdapter adapter;

    private StructureTasksContract.Presenter presenter;
    private ProgressDialog progressDialog;

    private RevealJsonFormUtils jsonFormUtils;

    private LocationUtils locationUtils;
    private boolean hasRequestedLocation;

    private TabLayout tabLayout;

    public static StructureTasksFragment newInstance(Bundle bundle, Context context) {
        StructureTasksFragment fragment = new StructureTasksFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
            fragment.setPresenter(new StructureTasksPresenter(fragment, context));
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            tabLayout = getActivity().findViewById(R.id.tabs);
        }
        initDependencies();
    }

    protected void initDependencies() {
        jsonFormUtils = new RevealJsonFormUtils();
        locationUtils = new LocationUtils(getActivity());
        locationUtils.requestLocationUpdates(new BaseLocationListener());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_structure_task, container, false);
        setUpViews(view);
        initializeAdapter();
        return view;
    }

    private void initializeAdapter() {
        adapter = new StructureTaskAdapter(onClickListener);
        taskRecyclerView.setAdapter(adapter);
    }

    private void setUpViews(View view) {
        TextView interventionType = view.findViewById(R.id.intervention_type);
        interventionType.setText(getString(Utils.getInterventionLabel()));
        taskRecyclerView = view.findViewById(R.id.task_recyclerView);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            StructureTaskDetails details = (StructureTaskDetails) view.getTag(R.id.task_details);
            presenter.onTaskSelected(details);
        }
    };

    @Override
    public void setStructure(String structureId) {
        presenter.findTasks(structureId);
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void showProgressDialog(@StringRes int title, @StringRes int message) {
        if (progressDialog != null) {
            progressDialog.setTitle(title);
            progressDialog.setMessage(getString(message));
            progressDialog.show();
        }
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void requestUserLocation() {
        hasRequestedLocation = true;
        locationUtils.checkLocationSettingsAndStartLocationServices(getActivity(), new BaseLocationListener());
    }


    @Override
    public Location getUserCurrentLocation() {
        return locationUtils.getLastLocation();
    }

    @Override
    public RevealJsonFormUtils getJsonFormUtils() {
        return jsonFormUtils;
    }

    @Override
    public void startForm(JSONObject formJSON) {
        jsonFormUtils.startJsonForm(formJSON, getActivity(), REQUEST_CODE_GET_JSON_FRAGMENT);
    }

    @Override
    public void displayError(int title, int message) {
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message).create().show();
    }

    @Override
    public void setTaskDetailsList(List<StructureTaskDetails> taskDetailsList) {
        adapter.setTaskDetailsList(taskDetailsList);
        if (tabLayout != null && tabLayout.getTabAt(1) != null) {
            tabLayout.getTabAt(1).setText(getString(R.string.tasks, taskDetailsList.size()));
        }
    }

    @Override
    public void updateTask(String taskID, Task.TaskStatus taskStatus, String businessStatus) {
        adapter.updateTask(taskID, taskStatus, businessStatus);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RequestCode.LOCATION_SETTINGS && hasRequestedLocation) {
            if (resultCode == RESULT_OK) {
                locationUtils.requestLocationUpdates(new BaseLocationListener());
                presenter.getLocationPresenter().waitForUserLocation();
            } else if (resultCode == RESULT_CANCELED) {
                presenter.getLocationPresenter().onGetUserLocationFailed();
            }
            hasRequestedLocation = false;
        } else if (requestCode == REQUEST_CODE_GET_JSON_FRAGMENT && resultCode == RESULT_OK && data.hasExtra(JSON_FORM_PARAM_JSON)) {
            String json = data.getStringExtra(JSON_FORM_PARAM_JSON);
            Log.d(TAG, json);
            presenter.saveJsonForm(json);
        }
    }

    public void refreshTasks(String structureId) {
        presenter.findTasks(structureId);
    }

    public void setPresenter(StructureTasksContract.Presenter presenter) {
        this.presenter = presenter;
    }
}