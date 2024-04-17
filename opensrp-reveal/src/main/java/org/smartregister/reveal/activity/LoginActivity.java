package org.smartregister.reveal.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.presenter.LoginPresenter;
import org.smartregister.reveal.view.ListTasksActivity;
import org.smartregister.task.SaveTeamLocationsTask;
import org.smartregister.view.activity.BaseLoginActivity;
import org.smartregister.view.contract.BaseLoginContract;

public class LoginActivity extends BaseLoginActivity implements BaseLoginContract.View {

    private ImageView mainLogo;

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String someValue = intent.getStringExtra("someName");
        }
    };

    private static final int STORAGE_PERMISSION_CODE = 23;
    @Override
    protected int getContentView() {
        return R.layout.activity_login;
    }

    @Override
    protected void initializePresenter() {
        mLoginPresenter = new LoginPresenter(this);
    }

    @Override
    public void goToHome(boolean remote) {
        if (remote) {
            org.smartregister.util.Utils.startAsyncTask(new SaveTeamLocationsTask(), null);
        }
        RevealApplication.getInstance().getContext().anmLocationController().evict();
        Intent intent = new Intent(this, ListTasksActivity.class);
        startActivity(intent);

        finish();

        RevealApplication.getInstance().processServerConfigs();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLoginPresenter.processViewCustomizations();
        if (!mLoginPresenter.isUserLoggedOut()) {
            goToHome(false);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == STORAGE_PERMISSION_CODE){
//            if(grantResults.length > 0){
//                boolean write = grantResults[0] == PackageManager.PERMISSION_GRANTED;
//                boolean read = grantResults[1] == PackageManager.PERMISSION_GRANTED;
//
//                if(read && write){
//                    Toast.makeText(LoginActivity.this, "Storage Permissions Granted", Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(LoginActivity.this, "Storage Permissions Denied", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainLogo = findViewById(R.id.login_logo);
        setUpLogos();

//        boolean hasPermission = PermissionUtils.checkStoragePermissions(getContext());
//
//
//        if (!hasPermission){
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                            Manifest.permission.READ_EXTERNAL_STORAGE
//                    },
//                    STORAGE_PERMISSION_CODE
//            );
//        }
    }

    private void setUpLogos() {
        mainLogo.setBackgroundResource(R.drawable.ic_logo_login);
    }
}
