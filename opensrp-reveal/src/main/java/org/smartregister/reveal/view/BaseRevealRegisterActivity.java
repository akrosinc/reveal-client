package org.smartregister.reveal.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.util.Map;
import org.json.JSONObject;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.util.LangUtils;
import org.smartregister.view.activity.BaseRegisterActivity;

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
        if (getBuildCountry() == Country.THAILAND) {
            LangUtils.saveLanguage(base.getApplicationContext(), "th");
        } else if(getBuildCountry() == Country.SENEGAL ) {
            LangUtils.saveLanguage(base.getApplicationContext(), "fr");
        } else if(getBuildCountry() == Country.RWANDA){
            LangUtils.saveLanguage(base.getApplicationContext(), "rw");
        } else if(getBuildCountry() == Country.MOZAMBIQUE){
            LangUtils.saveLanguage(base.getApplicationContext(), "pt");
        } else {
            LangUtils.saveLanguage(base.getApplicationContext(), "en");
        }
        super.attachBaseContext(base);
    }

    @NonNull
    private Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }
}
