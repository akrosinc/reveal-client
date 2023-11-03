package org.smartregister.util;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import org.smartregister.repository.AllSharedPreferences;

import java.util.Locale;

import org.smartregister.reveal.util.Country;
import org.smartregister.reveal.util.PreferencesUtil;
import timber.log.Timber;

public class LangUtils {

    public static void saveLanguage(Context ctx, String language) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(ctx));
        allSharedPreferences.saveLanguagePreference(language);
        // update context
        setAppLocale(ctx, language);
    }

    public static String getLanguage(Context ctx) {
        AllSharedPreferences allSharedPreferences = new AllSharedPreferences(PreferenceManager.getDefaultSharedPreferences(ctx));
        return allSharedPreferences.fetchLanguagePreference();
    }

    public static Configuration setAppLocale(Context context, String language) {
        Resources res = context.getResources();
        Configuration configuration = res.getConfiguration();

        try {
            Locale locale = new Locale(language);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(locale);

                LocaleList localeList = new LocaleList(locale);
                LocaleList.setDefault(localeList);
                configuration.setLocales(localeList);

                context.createConfigurationContext(configuration);

            } else {
                configuration.locale = locale;
                res.updateConfiguration(configuration, res.getDisplayMetrics());
            }


        } catch (Exception e) {
            Timber.tag("Reveal Exception").w(e);
        }

        return configuration;
    }

    public static void setLanguage(final Context base) {
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
    }

    private static Country getBuildCountry() {
        return PreferencesUtil.getInstance().getBuildCountry();
    }

}
