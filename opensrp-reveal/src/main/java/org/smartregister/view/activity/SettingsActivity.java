package org.smartregister.view.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.smartregister.reveal.R;
import org.smartregister.reveal.application.RevealApplication;
import org.smartregister.reveal.model.Environment;
import org.smartregister.reveal.util.PreferencesUtil;
import org.smartregister.util.LangUtils;
import timber.log.Timber;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {


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
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().get().url("https://reveal-environments.azurewebsites.net/envs")
                .build();
        RevealApplication.getInstance().getAppExecutors().networkIO().execute(new Runnable() {
            @Override
            public void run() {
                try (Response response = client.newCall(request).execute()) {
                    final List<Environment> servers = new Gson()
                            .fromJson(response.body().string(), new TypeToken<List<Environment>>() {
                            }.getType());
                    RevealApplication.getInstance().getAppExecutors().mainThread().execute(
                            () -> servers.forEach(server -> preferenceUtil.setEnvironment(server.getKey(), server.getData().getRevealServerUrl())));
                } catch (Exception e) {
                    Timber.e("failed to fetch envs...");
                }
            }
        });

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
                preferenceUtil.setBaseURL(
                        preferenceUtil.getEnvironmentURL(((ListPreference) baseUrlPreference).getValue()));
                listPreference.setOnPreferenceChangeListener((SettingsActivity) getActivity());

            }
        }

    }

}