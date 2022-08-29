package org.smartregister.reveal.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.json.JSONObject;
import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.contract.OtherFormsfragmentContract;
import org.smartregister.reveal.presenter.OtherFormsFragmentPresenter;
import org.smartregister.reveal.util.Constants;
import org.smartregister.reveal.util.Constants.JsonForm;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.LocationUtils;
import org.smartregister.reveal.util.RevealJsonFormUtils;
import org.smartregister.reveal.util.Utils;
import org.smartregister.reveal.view.SummaryFormsActivity;

public class SummaryFormsFragment extends Fragment implements OtherFormsfragmentContract.View, View.OnClickListener {
    
    private OtherFormsFragmentPresenter presenter;

    private RevealJsonFormUtils jsonFormUtils;

    private ProgressDialog progressDialog;

    private LocationUtils locationUtils;

    private Button btnDailySummary;

    private Button btnTeamLeaderDos;

    private Button btnCbSprayArea;

    private Button btnIrsSaDecision;

    private Button btnMobilization;

    private Button btnIrsFieldOfficer;

    private Button btnVerificationForm;

    private Button btnTabletAccountabilityForm;

    private Button btnFPPForm;

    private Button btnSupervisorDailySummary;

    private Button btnDailySummaryMorning;

    private Button btnDailySummaryEvening;

    private Button btnHfwLevelReferral;

    private Button btnCDDSupervisorChecklist;

    private Button btnHFWSupervisorChecklist;

    private Button btnDrugAllocationForm;

    public static SummaryFormsFragment newInstance(Bundle bundle) {

        SummaryFormsFragment fragment = new SummaryFormsFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = new OtherFormsFragmentPresenter(this);
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);

        locationUtils =  new LocationUtils(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_summary_forms, container, false);
        initializeViews(rootView);
        return rootView;
    }

    private void initializeViews(View view)
    {
        btnDailySummary = view.findViewById(R.id.summary_daily_summary);
        btnTeamLeaderDos = view.findViewById(R.id.summary_team_leader_dos);
        btnCbSprayArea = view.findViewById(R.id.summary_cb_spray_area);
        btnIrsSaDecision = view.findViewById(R.id.summary_irs_sa_decision);
        btnMobilization = view.findViewById(R.id.summary_mobilization_form);
        btnIrsFieldOfficer = view.findViewById(R.id.summary_irs_field_officer);
        btnVerificationForm = view.findViewById(R.id.summary_verification_form);
        btnTabletAccountabilityForm = view.findViewById(R.id.summary_tablet_accountability_form);
        btnSupervisorDailySummary = view.findViewById(R.id.supervisor_daily_summary);
        btnFPPForm = view.findViewById(R.id.fpp_form);


        btnDailySummaryMorning = view.findViewById(R.id.summary_daily_summary_morning);
        btnDailySummaryEvening = view.findViewById(R.id.summary_daily_summary_evening);
        btnHfwLevelReferral =  view.findViewById(R.id.hfw_level_referral);
        btnCDDSupervisorChecklist = view.findViewById(R.id.cdd_supervisor_checklist);
        btnHFWSupervisorChecklist = view.findViewById(R.id.hfw_supervisor_checklist);

        btnDrugAllocationForm = view.findViewById(R.id.cdd_drug_allocation_form);

        if(Utils.isZambiaIRSLite()){
            btnSupervisorDailySummary.setVisibility(View.VISIBLE);
        } else if(Utils.isZambiaIRSFull()){
            btnDailySummary.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator2).setVisibility(View.VISIBLE);
            btnTeamLeaderDos.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator3).setVisibility(View.VISIBLE);
            btnIrsSaDecision.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator5).setVisibility(View.VISIBLE);
            btnMobilization.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator6).setVisibility(View.VISIBLE);
            btnIrsFieldOfficer.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator7).setVisibility(View.VISIBLE);
            btnFPPForm.setVisibility(View.VISIBLE);
        } else if(Utils.isMDALite()){
            btnTabletAccountabilityForm.setVisibility(View.VISIBLE);
            btnDrugAllocationForm.setVisibility(View.VISIBLE);
        } else if(BuildConfig.BUILD_COUNTRY == Country.SENEGAL || BuildConfig.BUILD_COUNTRY == Country.SENEGAL_EN){
            btnDailySummary.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator2).setVisibility(View.VISIBLE);
            btnIrsSaDecision.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator5).setVisibility(View.VISIBLE);
        } else if(BuildConfig.BUILD_COUNTRY == Country.NIGERIA){
            btnDailySummaryMorning.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator11).setVisibility(View.VISIBLE);
            btnDailySummaryEvening.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator12).setVisibility(View.VISIBLE);
            btnHfwLevelReferral.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator13).setVisibility(View.VISIBLE);
            btnCDDSupervisorChecklist.setVisibility(View.VISIBLE);
            view.findViewById(R.id.separator14).setVisibility(View.VISIBLE);
            btnHFWSupervisorChecklist.setVisibility(View.VISIBLE);
        }
        setClickListeners();
    }

    private void setClickListeners() {
            btnTabletAccountabilityForm.setOnClickListener(this);
            btnDailySummary.setOnClickListener(this);
            btnTeamLeaderDos.setOnClickListener(this);
            btnCbSprayArea.setOnClickListener(this);
            btnIrsSaDecision.setOnClickListener(this);
            btnMobilization.setOnClickListener(this);
            btnIrsFieldOfficer.setOnClickListener(this);
            btnVerificationForm.setOnClickListener(this);
            btnFPPForm.setOnClickListener(this);
            btnSupervisorDailySummary.setOnClickListener(this);
            btnDailySummaryMorning.setOnClickListener(this);
            btnDailySummaryEvening.setOnClickListener(this);
            btnHfwLevelReferral.setOnClickListener(this);
            btnCDDSupervisorChecklist.setOnClickListener(this);
            btnHFWSupervisorChecklist.setOnClickListener(this);
            btnDrugAllocationForm.setOnClickListener(this);
    }

    @Override
    public void displayToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public RevealJsonFormUtils getJsonFormUtils() {
        return jsonFormUtils;
    }

    @Override
    public void startForm(JSONObject formName) {
        ((SummaryFormsActivity) getActivity()).startFormActivity(formName);
    }

    @Override
    public void displayError(int title, int message) {
        new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(message).create().show();
    }

    @Override
    public Location getUserCurrentLocation() {
        return locationUtils.getLastLocation();
    }

    @Override
    public void showProgressDialog(int title, int message) {
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
        // Do nothing
    }

    public void setJsonFormUtils(RevealJsonFormUtils jsonFormUtils) {
        this.jsonFormUtils = jsonFormUtils;
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.hfw_level_referral:
                presenter.showBasicForm(Constants.JsonForm.HFW_LEVEL_REFERRAL);
                break;
            case R.id.cdd_supervisor_checklist:
                presenter.showBasicForm(Constants.JsonForm.CDD_SUPERVISOR_CHECKLIST);
                break;
            case R.id.hfw_supervisor_checklist:
                presenter.showBasicForm(Constants.JsonForm.HFW_SUPERVISOR_CHECKLIST);
                break;
            case R.id.summary_daily_summary_morning:
                presenter.showBasicForm(Constants.JsonForm.DAILY_SUMMARY_MORNING);
                break;
            case R.id.summary_daily_summary_evening:
                presenter.showBasicForm(Constants.JsonForm.DAILY_SUMMARY_EVENING);
                break;
            case R.id.summary_daily_summary:
            case R.id.supervisor_daily_summary:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    if(Utils.isZambiaIRSLite()){
                        presenter.showBasicForm(Constants.JsonForm.DAILY_SUMMARY_ZAMBIA_LITE);
                    } else {
                        presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.DAILY_SUMMARY_ZAMBIA);
                    }
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.DAILY_SUMMARY_SENEGAL);
                }else if(BuildConfig.BUILD_COUNTRY == Country.SENEGAL_EN){
                    presenter.showBasicForm(Constants.JsonForm.DAILY_SUMMARY_SENEGAL_EN);
                }
                break;
            case R.id.summary_team_leader_dos:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.TEAM_LEADER_DOS_ZAMBIA);
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.TEAM_LEADER_DOS_SENEGAL);
                }
                break;
            case R.id.summary_cb_spray_area:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.CB_SPRAY_AREA_ZAMBIA);
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.CB_SPRAY_AREA_SENEGAL);
                }
                break;
            case R.id.summary_irs_sa_decision:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.IRS_SA_DECISION_ZAMBIA);
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.IRS_SA_DECISION_SENEGAL);
                } else if(BuildConfig.BUILD_COUNTRY == Country.SENEGAL_EN){
                    presenter.showBasicForm(Constants.JsonForm.IRS_SA_DECISION_SENEGAL_EN);
                }
                break;
            case R.id.summary_mobilization_form:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.MOBILIZATION_FORM_ZAMBIA);
                } else {
                    presenter.showBasicForm(Constants.JsonForm.MOBILIZATION_FORM_SENEGAL);
                }
                break;
            case R.id.summary_irs_field_officer:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.IRS_FIELD_OFFICER_ZAMBIA);
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.IRS_FIELD_OFFICER_SENEGAL);
                } else if(BuildConfig.BUILD_COUNTRY == Country.SENEGAL_EN){
                    presenter.showBasicForm(Constants.JsonForm.IRS_FIELD_OFFICER_SENEGAL_EN);
                }
                break;
            case R.id.summary_verification_form:
                if (BuildConfig.BUILD_COUNTRY == Country.ZAMBIA) {
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.VERIFICATION_FORM_ZAMBIA);
                } else if (BuildConfig.BUILD_COUNTRY == Country.SENEGAL){
                    presenter.showBasicForm(Constants.JsonForm.VERIFICATION_FORM_SENEGAL);
                }
                break;
            case R.id.summary_tablet_accountability_form:
                if(BuildConfig.BUILD_COUNTRY == Country.KENYA){
                    presenter.showBasicForm(org.smartregister.reveal.util.Constants.JsonForm.TABLET_ACCOUNTABILITY_FORM);
                } else if (BuildConfig.BUILD_COUNTRY == Country.RWANDA){
                    presenter.showBasicForm(Constants.JsonForm.TABLET_ACCOUNTABILITY_FORM_RWANDA);
                } else if(BuildConfig.BUILD_COUNTRY == Country.RWANDA_EN){
                    presenter.showBasicForm(Constants.JsonForm.TABLET_ACCOUNTABILITY_FORM_RWANDA_EN);
                }
                break;
            case R.id.fpp_form:
                presenter.showBasicForm(Constants.JsonForm.FPP_FORM_ZAMBIA);
                break;
            case R.id.cdd_drug_allocation_form:
                presenter.showBasicForm(JsonForm.CDD_DRUG_ALLOCAITON_FORM);
                break;
            default:
                break;
        }
    }
}
