package it.cynomys.cfmandroid

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LocaleManager {

    private const val PREFS = "locale_prefs"
    private const val KEY_LANGUAGE = "language"

    fun saveLanguage(context: Context, language: String) {
        context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .apply()
    }

    fun getLanguage(context: Context): String {
        return context
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "en") ?: "en"
    }

    fun wrapContext(context: Context): Context {
        val language = getLanguage(context)
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }
}
