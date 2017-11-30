package ru.binaryblitz.Chisto

import android.annotation.TargetApi
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.support.multidex.MultiDex
import net.danlew.android.joda.JodaTimeAndroid
import java.util.*

class BaseApplication : Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLanguage(this, "ru")
    }

    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        setLanguage(this, "ru")
    }

    fun setSystemLocaleLegacy(config: Configuration, locale: Locale) {
        config.locale = locale
    }

    @TargetApi(Build.VERSION_CODES.N)
    fun setSystemLocale(config: Configuration, locale: Locale) {
        config.setLocale(locale)
    }

    fun setLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            setSystemLocale(config, locale)
        } else {
            setSystemLocaleLegacy(config, locale)
        }

        context.applicationContext.resources.updateConfiguration(config,
                context.resources.displayMetrics)
    }
}
