package org.smartregister.family.activity;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import org.smartregister.reveal.R;
import org.smartregister.family.adapter.ViewPagerAdapter;
import org.smartregister.family.contract.FamilyOtherMemberContract;
import org.smartregister.family.util.Utils;
import org.smartregister.helper.ImageRenderHelper;
import org.smartregister.view.activity.BaseProfileActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public abstract class BaseFamilyOtherMemberProfileActivity extends BaseProfileActivity implements FamilyOtherMemberContract.View {

    private TextView nameView;
    private TextView detailOneView;
    private TextView detailTwoView;
    private TextView detailThreeView;
    private CircleImageView imageView;

    private View familyHead;
    private View primaryCaregiver;

    protected ViewPagerAdapter adapter;

    @Override
    protected void onCreation() {
        setContentView(R.layout.activity_family_other_member_profile);

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

        familyHead = findViewById(R.id.family_head);
        primaryCaregiver = findViewById(R.id.primary_caregiver);

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
    public void setProfileImage(String baseEntityId, String entityType) {
        imageRenderHelper.refreshProfileImage(baseEntityId, imageView, Utils.getMemberProfileImageResourceIDentifier(entityType));
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

    @Override
    public void toggleFamilyHead(boolean show) {
        if (show) {
            familyHead.setVisibility(View.VISIBLE);
        } else {
            familyHead.setVisibility(View.GONE);
        }
    }

    @Override
    public void togglePrimaryCaregiver(boolean show) {
        if (show) {
            primaryCaregiver.setVisibility(View.VISIBLE);
        } else {
            primaryCaregiver.setVisibility(View.GONE);
        }
    }

    @Override
    public FamilyOtherMemberContract.Presenter presenter() {
        return ( FamilyOtherMemberContract.Presenter) presenter;
    }
}
