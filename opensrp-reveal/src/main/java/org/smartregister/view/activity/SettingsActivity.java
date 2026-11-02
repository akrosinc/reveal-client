package org.smartregister.view.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.gson.Gson;


import org.smartregister.reveal.BuildConfig;
import org.smartregister.reveal.R;
import org.smartregister.reveal.activity.LoginActivity;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.EnvironmentDetails;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.util.LangUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import timber.log.Timber;


public class SettingsActivity extends MultiLanguageActivity {

    private static PreferencesUtil preferenceUtil = PreferencesUtil.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().add(android.R.id.content,new MyPreferenceFragment()).commit();
    }

    @Override
    protected void attachBaseContext(Context base) {
        LangUtils.setLanguage(base);
        super.attachBaseContext(base);
    }


    protected static void setPreferenceData(ListPreference preference) {
        String envKeys = preferenceUtil.getStringPreference("env_keys");
        List<String> options = new ArrayList<>();
        if (envKeys != null && !envKeys.trim().isEmpty()) {
            for (String key : envKeys.split(",")) {
                if (key != null && !key.trim().isEmpty()) {
                    options.add(key.trim());
                }
            }
        }
        String[] entries = options.toArray(new String[0]);
        preference.setEntries(entries);
        preference.setEntryValues(entries);

    }

    protected static void setLanguageData(ListPreference preference) {
        List<String> langNames = new LinkedList<>();
        langNames.add("English");
        langNames.add("French");
        langNames.add("Portuguese");

        List<String> langValues = new LinkedList<>();
        langValues.add("en");
        langValues.add("fr");
        langValues.add("pt-rMZ");

        preference.setEntries(langNames.toArray(new String[]{}));
        preference.setEntryValues(langValues.toArray(new String[]{}));

    }


    public static class MyPreferenceFragment extends PreferenceFragmentCompat {

        private static final RevealApplication revealApplication = RevealApplication.getInstance();


        private static PreferencesUtil preferenceUtil = PreferencesUtil.getInstance();
        Context context;

        private static final Gson gson = new Gson();

        private static final org.smartregister.Context openSRPcontext = org.smartregister.Context.getInstance();



        @Override
        public void onStart(){
            super.onStart();
            this.context = getContext();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);

            Preference baseUrlPreference = findPreference("reveal_instance_key");
            if (baseUrlPreference != null) {
                ListPreference envPreference = (ListPreference) baseUrlPreference;
                setPreferenceData(envPreference);

                envPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    if (newValue != null && !newValue.toString().trim().isEmpty()) {

                        String environmentJson = preferenceUtil.getStringPreference(newValue.toString());
                        EnvironmentDetails details = environmentJson != null && !environmentJson.trim().isEmpty()
                                ? gson.fromJson(environmentJson, EnvironmentDetails.class)
                                : null;

                        if (details == null) {
                            // No stored environment data for the selected key (e.g. environments
                            // failed to load from the config server). Don't update the base URL and
                            // reject the change instead of crashing with an NPE.
                            Timber.w("No environment details found for key: %s", newValue);
                            return false;
                        }

                        preferenceUtil.setBaseURL(details.getRevealServerUrl());
                        preferenceUtil.setBuildCountry(details.getBuildCountry() != null ?  details.getBuildCountry().toString() : Country.ZAMBIA.toString() );

                    }

                    return true;
                });

            }

            Preference languagePreference = findPreference("reveal_language_key");
            if (languagePreference != null) {
                ListPreference languagePreferenceList = (ListPreference) languagePreference;
                setLanguageData(languagePreferenceList);
                languagePreferenceList.setOnPreferenceChangeListener((preference, newValue) -> {
                    LangUtils.setAppLocale(context,newValue.toString());

                    SharedPreferences languagepref =  context.getSharedPreferences("language",MODE_PRIVATE);
                    SharedPreferences.Editor editor = languagepref.edit();
                    editor.putString("languageToLoad",newValue.toString() );
                    editor.commit();

                    Intent refresh = new Intent(context, LoginActivity.class);
                    startActivity(refresh);
                    return true;
                });
            }

        }

    }
}
