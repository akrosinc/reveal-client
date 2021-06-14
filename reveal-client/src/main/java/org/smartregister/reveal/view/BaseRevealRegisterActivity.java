package com.revealprecision.reveal.view;

import android.content.Context;

import androidx.fragment.app.Fragment;

import org.json.JSONObject;
import com.revealprecision.reveal.BuildConfig;
import com.revealprecision.reveal.util.Country;
import org.smartregister.util.LangUtils;
import org.smartregister.view.activity.BaseRegisterActivity;

import java.util.Map;

/**
 * Created by samuelgithengi on 7/30/20.
 */
public abstract class BaseRevealRegisterActivity extends BaseRegisterActivity {

    @Override
    protected Fragment[] getOtherFragments() {
        return new Fragment[0];
    }

    @Override
    public void startFormActivity(String s, String s1, Map<String, String> map) {//not used
    }

    @Override
    public void startFormActivity(JSONObject jsonObject) {//not used
    }

    @Override
    public void startRegistration() {//not used
    }

    @Override
    protected void attachBaseContext(Context base) {
        if (BuildConfig.BUILD_COUNTRY == Country.THAILAND) {
            LangUtils.saveLanguage(base.getApplicationContext(), "th");
        } else {
            LangUtils.saveLanguage(base.getApplicationContext(), "en");
        }
        super.attachBaseContext(base);
    }
}
