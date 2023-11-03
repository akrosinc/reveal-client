package org.smartregister.view.activity;

import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.appcompat.app.AppCompatActivity;

import org.smartregister.util.LangUtils;

import timber.log.Timber;


/**
 * Use this class to ensure your activities have multi-language support
 *
 * @author Rodgers Andati
 * @since 2019-05-03
 */
public class MultiLanguageActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context base) {
        // get language from prefs

        String lang = LangUtils.getLanguage(base.getApplicationContext());
        Configuration newConfiguration = null;
        if (base != null && base.getSharedPreferences("language", MODE_PRIVATE) != null) {
            SharedPreferences sharedPreferences = base.getSharedPreferences("language", MODE_PRIVATE);

            if (sharedPreferences != null) {
                String sharedLang = sharedPreferences.getString("languageToLoad", "en");
                if (sharedLang != null) {
                    newConfiguration = LangUtils.setAppLocale(base, sharedLang);

                } else {
                    newConfiguration = LangUtils.setAppLocale(base, lang);
                }

            } else {
                newConfiguration = LangUtils.setAppLocale(base, lang);
            }
        }

        super.attachBaseContext(base);

        applyOverrideConfiguration(newConfiguration);
    }

}
