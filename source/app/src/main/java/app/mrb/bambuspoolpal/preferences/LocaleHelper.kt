/**
 * Copyright (C) 2025 BambuSpoolPal
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package app.mrb.bambuspoolpal.preferences

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * A helper object for managing and applying locale (language) settings across the application.
 * This allows the app to dynamically support multilingual environments.
 */
object LocaleHelper {

    /**
     * Applies the saved locale to the given context when the app or activity is created.
     *
     * @param context The context to which the saved locale should be applied.
     * @return A context with the appropriate locale configuration.
     */
    fun onAttach(context: Context): Context {
        val language = getPersistedLanguage(context)
        return setLocale(context, language)
    }

    /**
     * Updates the application's locale configuration and persists the selected language.
     *
     * @param context The context to which the locale change should apply.
     * @param language The ISO 639 language code (e.g., "en", "fr").
     *                If null, the default language is used.
     * @return A context with the updated locale.
     */
    @SuppressLint("ObsoleteSdkInt")
    fun setLocale(context: Context, language: String?): Context {
        val langToUse = language ?: Constants.DEFAULT_LANGUAGE
        persistLanguage(context, langToUse)

        val locale = Locale(langToUse)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocales(LocaleList.forLanguageTags(langToUse))
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }

    /**
     * Retrieves the saved language preference from SharedPreferences.
     * If no language is saved, the method defaults to the system language,
     * or the app's default language if the system language is unsupported.
     *
     * @param context The context used to access SharedPreferences.
     * @return A valid language code as a String.
     */
    fun getPersistedLanguage(context: Context): String {
        val preferences = context.getSharedPreferences(Constants.APPLICATION_PARAM, Context.MODE_PRIVATE)

        val systemLanguage = Locale.getDefault().language
        val availableLanguages = Constants.AVAILABLE_LANGUAGES

        val selectedLanguage = if (availableLanguages.contains(systemLanguage)) {
            systemLanguage
        } else {
            Constants.DEFAULT_LANGUAGE
        }

        return preferences.getString(Constants.DEFAULT_LANGUAGE_PARAM, selectedLanguage)
            ?: Constants.DEFAULT_LANGUAGE
    }

    /**
     * Saves the selected language code to SharedPreferences so it can be reapplied
     * on subsequent launches.
     *
     * @param context The context used to access SharedPreferences.
     * @param language The language code to save.
     */
    private fun persistLanguage(context: Context, language: String) {
        val preferences = context.getSharedPreferences(Constants.APPLICATION_PARAM, Context.MODE_PRIVATE)
        preferences.edit()
            .putString(Constants.DEFAULT_LANGUAGE_PARAM, language)
            .apply()
    }
}
