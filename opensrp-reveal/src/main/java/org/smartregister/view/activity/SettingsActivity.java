package org.smartregister.view.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import java.util.Arrays;
import java.util.List;
import org.smartregister.reveal.R;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.util.LangUtils;

public class SettingsActivity extends PreferenceActivity
        implements OnPreferenceChangeListener {


    private static android.preference.ListPreference listPreference;

    private static PreferencesUtil preferenceUtil = PreferencesUtil.getInstance();

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs

        String lang = LangUtils.getLanguage(base.getApplicationContext());
        Configuration newConfiguration = LangUtils.setAppLocale(base, lang);

        super.attachBaseContext(base);

        applyOverrideConfiguration(newConfiguration);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (newValue != null) {
            preferenceUtil.setBaseURL(preferenceUtil.getEnvironmentURL(newValue.toString()));
        }
        return true;
    }

    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            Preference baseUrlPreference = findPreference("reveal_instance_key");
            if (baseUrlPreference != null) {
                listPreference = (ListPreference) baseUrlPreference;
                setPreferenceData(listPreference);
                preferenceUtil.setBaseURL(
                        preferenceUtil.getEnvironmentURL(((ListPreference) baseUrlPreference).getValue()));
                listPreference.setOnPreferenceChangeListener((SettingsActivity) getActivity());
            }
        }

    }

    protected static void setPreferenceData(ListPreference preference) {
        List<String> options = Arrays.asList(preferenceUtil.getEnvironmentURL("env_keys").split(","));
        preference.setEntries((String[]) options.toArray());
        preference.setEntryValues((String[]) options.toArray());

    }

}