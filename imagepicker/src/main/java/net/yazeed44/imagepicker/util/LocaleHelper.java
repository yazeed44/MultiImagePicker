package net.yazeed44.imagepicker.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;

import java.util.Locale;

public class LocaleHelper {

    /**
     * @param context Should be Application Context
     * @return wrapped context
     */
    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());

        return setLocale(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLocale(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLocale(Context context, String language) {
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    public static Context setLocaleDontWrap(Context context) {
        String language = getLanguage(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResourcesDontWrap(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    public static Context setLocale(Context context) {
        String language = getLanguage(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }

        return updateResourcesLegacy(context, language);
    }

    public static String getPersistedData(Context context, String defaultLanguage) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString("ls_language", defaultLanguage);
    }

    public static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("ls_language", language);
        editor.apply();

    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Context updateResources(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        LocaleList localeList = new LocaleList(locale);
        configuration.setLocales(localeList);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        return LocaleContextWrapper.wrap(context.createConfigurationContext(configuration), locale);
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Context updateResourcesDontWrap(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Configuration configuration = context.getResources().getConfiguration();
        configuration.setLocale(locale);
        configuration.setLayoutDirection(locale);
        LocaleList localeList = new LocaleList(locale);
        configuration.setLocales(localeList);
        context.getResources().updateConfiguration(configuration, context.getResources().getDisplayMetrics());

        return context.createConfigurationContext(configuration);
    }

    public static Context updateResourcesLegacy(Context context, String language) {
        Locale locale = new Locale(language);
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = resources.getConfiguration();
        configuration.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale);
        }

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}