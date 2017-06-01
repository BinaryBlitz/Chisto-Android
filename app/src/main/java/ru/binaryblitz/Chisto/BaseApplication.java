package ru.binaryblitz.Chisto;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Locale;

public class BaseApplication extends Application {

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLanguage(this, "ru");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        setLanguage(this, "ru");
    }

    @SuppressWarnings("deprecation")
    public void setSystemLocaleLegacy(Configuration config, Locale locale) {
        config.locale = locale;
    }

    @TargetApi(Build.VERSION_CODES.N)
    public void setSystemLocale(Configuration config, Locale locale) {
        config.setLocale(locale);
    }

    @SuppressWarnings("deprecation")
    public void setLanguage(Context context, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setSystemLocale(config, locale);
        else setSystemLocaleLegacy(config, locale);

        context.getApplicationContext().getResources().updateConfiguration(config,
                context.getResources().getDisplayMetrics());
    }
}
