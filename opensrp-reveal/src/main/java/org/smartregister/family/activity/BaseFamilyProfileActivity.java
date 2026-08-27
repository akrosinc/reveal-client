package org.smartregister.family.activity;

import android.content.Intent;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.vijay.jsonwizard.constants.JsonFormConstants;
import com.vijay.jsonwizard.domain.Form;

import org.json.JSONObject;
import org.smartregister.AllConstants;
import org.smartregister.domain.FetchStatus;
import org.smartregister.reveal.R;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.contract.FamilyProfileContract;
import org.smartregister.family.fragment.BaseFamilyProfileMemberFragment;
import org.smartregister.family.util.AppExecutors;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import timber.log.Timber;

public abstract class BaseFamilyProfileActivity extends BaseProfileActivity implements FamilyProfileContract.View {

    private TextView nameView;
    private TextView detailOneView;
    private TextView detailTwoView;
    private TextView detailThreeView;
    private CircleImageView imageView;

    protected ViewPagerAdapter adapter;

    private AppExecutors appExecutors = new AppExecutors();

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_family_profile);

        Toolbar toolbar = findViewById(R.id.family_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        appBarLayout = findViewById(R.id.toolbar_appbarlayout);

        imageRenderHelper = new ImageRenderHelper(this);

        initializePresenter();

        setupViews();
    }

    @Override
    protected void setupViews() {
        super.setupViews();

        detailOneView = findViewById(R.id.textview_detail_one);
        detailTwoView = findViewById(R.id.textview_detail_two);
        detailThreeView = findViewById(R.id.textview_detail_three);

        nameView = findViewById(R.id.textview_name);

        imageView = findViewById(R.id.imageview_profile);
        imageView.setBorderWidth(2);
    }

    @Override
    protected void onResumption() {
        super.onResumption();

        presenter().refreshProfileView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter() != null) {
            presenter().onDestroy(isChangingConfigurations());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.add_member) {
            startFormActivity(Utils.metadata().familyMemberRegister.formName, null, (String)null);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void fetchProfileData() {
        presenter().fetchProfileData();
    }

    @Override
    public void startFormActivity(String formName, String entityId, String metaData) {
        try {
            String locationId = Utils.context().allSharedPreferences().getPreference(AllConstants.CURRENT_LOCATION_ID);
            presenter().startForm(formName, entityId, metaData, locationId);

        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
            displayToast(R.string.error_unable_to_start_form);
        }
    }

    @Override
    public void startFormActivity(JSONObject jsonForm) {
        Intent intent = new Intent(this, Utils.metadata().familyMemberFormActivity);
        intent.putExtra(Constants.JSON_FORM_EXTRA.JSON, jsonForm.toString());


        Form form = new Form();
        form.setActionBarBackground(R.color.family_actionbar);
        form.setWizard(false);
        intent.putExtra(JsonFormConstants.JSON_FORM_KEY.FORM, form);


        startActivityForResult(intent, JsonFormUtils.REQUEST_CODE_GET_JSON);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == JsonFormUtils.REQUEST_CODE_GET_JSON && resultCode == RESULT_OK) {
            try {
                String jsonString = data.getStringExtra(Constants.JSON_FORM_EXTRA.JSON);
                Timber.d(jsonString);

                JSONObject form = new JSONObject(jsonString);
                if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyRegister.updateEventType)) {
                    presenter().updateFamilyRegister(jsonString);
                } else if (form.getString(JsonFormUtils.ENCOUNTER_TYPE).equals(Utils.metadata().familyMemberRegister.registerEventType)) {
                    presenter().saveFamilyMember(jsonString);
                }
            } catch (Exception e) {
                Timber.tag("Reveal Exception").w(e);
            }
        }
    }

    @Override
    public void refreshMemberList(final FetchStatus fetchStatus) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            BaseFamilyProfileMemberFragment memberFragment = getProfileMemberFragment();
            if (memberFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                memberFragment.refreshListView();
            }
        } else {
            appExecutors.mainThread().execute(new Runnable() {
                @Override
                public void run() {
                    BaseFamilyProfileMemberFragment memberFragment = getProfileMemberFragment();
                    if (memberFragment != null && fetchStatus.equals(FetchStatus.fetched)) {
                        memberFragment.refreshListView();
                    }
                }
            });
        }
    }

    @Override
    public void displayShortToast(int resourceId) {
        Utils.showShortToast(this, getString(resourceId));
    }

    @Override
    public void setProfileImage(String baseEntityId) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, Utils.getProfileImageResourceIDentifier());
    }

    @Override
    public void setProfileName(String fullName) {
        nameView.setText(fullName);
    }

    @Override
    public void setProfileDetailOne(String detailOne) {
        detailOneView.setText(detailOne);
    }

    @Override
    public void setProfileDetailTwo(String detailTwo) {
        detailTwoView.setText(detailTwo);
    }

    @Override
    public void setProfileDetailThree(String detailThree) {
        detailThreeView.setText(detailThree);
    }

    public BaseFamilyProfileMemberFragment getProfileMemberFragment() {
        Fragment fragment = adapter.getItem(0);
        if (fragment instanceof BaseFamilyProfileMemberFragment) {
            return ( BaseFamilyProfileMemberFragment ) fragment;
        }
        return null;
    }

    @Override
    public FamilyProfileContract.Presenter presenter() {
        return ( FamilyProfileContract.Presenter) presenter;
    }
}
