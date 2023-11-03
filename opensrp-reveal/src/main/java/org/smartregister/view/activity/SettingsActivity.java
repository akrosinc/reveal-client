package org.smartregister.view.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.gson.Gson;

import org.smartregister.reveal.R;
import org.smartregister.reveal.activity.LoginActivity;
import org.smartregister.reveal.model.EnvironmentDetails;
import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.util.LangUtils;

import java.util.Arrays;
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
        List<String> options = Arrays.asList(preferenceUtil.getStringPreference("env_keys").split(","));
        preference.setEntries((String[]) options.toArray());
        preference.setEntryValues((String[]) options.toArray());

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

        private static PreferencesUtil preferenceUtil = PreferencesUtil.getInstance();
        Context context;

        private static Gson gson = new Gson();

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
                    if (newValue != null) {

                        EnvironmentDetails details = gson.fromJson(preferenceUtil.getStringPreference(newValue.toString()),EnvironmentDetails.class);
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
