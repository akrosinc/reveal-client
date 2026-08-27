package org.smartregister.family.activity;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.vijay.jsonwizard.activities.NoLocaleFormConfigurationJsonWizardFormActivity;

import org.json.JSONObject;
import org.smartregister.reveal.R;
import org.smartregister.family.util.Constants;
import org.smartregister.family.util.JsonFormUtils;
import org.smartregister.util.LangUtils;

import timber.log.Timber;

public class NoLocaleFamilyWizardFormActivity extends NoLocaleFormConfigurationJsonWizardFormActivity {

    private Boolean enableOnCloseDialog = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        enableOnCloseDialog = getIntent()
                .getBooleanExtra(Constants.WizardFormActivity.EnableOnCloseDialog, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.setConfirmCloseTitle(this.getString(R.string.confirm_form_close));
        this.setConfirmCloseMessage(this.getString(R.string.confirm_form_close_explanation));

        try {
            JSONObject form = new JSONObject(this.currentJsonState());
            String et = form.getString(JsonFormUtils.ENCOUNTER_TYPE);
            if (et.trim().toLowerCase().contains("update")) {
                this.setConfirmCloseMessage(this.getString(R.string.any_changes_you_make));
            }
        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e.toString());
        }
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        if (toolbar != null){
            toolbar.setContentInsetStartWithNavigation(0);
        }
        super.setSupportActionBar(toolbar);
    }

    /**
     * Conditionaly display the confirmation dialog
     */
    @Override
    public void onBackPressed() {
        if (enableOnCloseDialog){
            super.onBackPressed();
        }else {
            this.finish();
        }
    }

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs
        String lang = LangUtils.getLanguage(base.getApplicationContext());
        Configuration newConfiguration = LangUtils.setAppLocale(base, lang);

        super.attachBaseContext(base);

        this.applyOverrideConfiguration(newConfiguration);
    }

}
