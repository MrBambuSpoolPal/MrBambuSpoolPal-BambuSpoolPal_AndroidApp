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

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import app.mrb.bambuspoolpal.shared.FilamentData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * ConfigManager is responsible for saving and loading application configuration settings
 * in SharedPreferences.
 */
class ConfigManager(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(Constants.APPLICATION_PARAM, Context.MODE_PRIVATE)

    /**
     * Save a String configuration setting to SharedPreferences.
     * @param key The key to associate with the configuration setting.
     * @param value The String value to save for the specified key.
     */
    fun saveConfig(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    /**
     * Save a Boolean configuration setting to SharedPreferences.
     * @param key The key to associate with the configuration setting.
     * @param value The Boolean value to save for the specified key.
     */
    fun saveConfig(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Retrieve a String configuration setting from SharedPreferences.
     * @param key The key of the configuration setting to retrieve.
     * @param defaultValue The value to return if the key is not found. Default is an empty string.
     * @return The value associated with the key, or the default value if not found.
     */
    fun getConfig(key: String, defaultValue: String = ""): String {
        return sharedPreferences.getString(key, defaultValue) ?: defaultValue
    }

    /**
     * Retrieve a Boolean configuration setting from SharedPreferences.
     * @param key The key of the configuration setting to retrieve.
     * @param defaultValue The value to return if the key is not found.
     * @return The Boolean value associated with the key, or the default value if not found.
     */
    fun getConfig(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    /**
     * Save the list of filament data in SharedPreferences as a JSON string.
     * @param data The list of [FilamentData] to save.
     */
    fun saveFilamentDataLocally(data: List<FilamentData>) {
        // Get the SharedPreferences object
        val sharedPreferences = context.getSharedPreferences("filament_data", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()

        // Convert the list to a JSON string
        val json = gson.toJson(data)

        // Store the JSON string
        editor.putString("saved_filament_data", json)
        editor.apply()

        // Log the saved data for debugging
        Log.d("PreferencesManager", "FilamentData saved: $json")
    }

    /**
     * Load the filament data stored in SharedPreferences.
     * @return The list of [FilamentData] objects, or an empty list if not found.
     */
    fun loadFilamentDataLocally(): List<FilamentData> {
        val sharedPreferences = context.getSharedPreferences("filament_data", Context.MODE_PRIVATE)
        val gson = Gson()

        // Retrieve the JSON string
        val json = sharedPreferences.getString("saved_filament_data", null)

        return if (json != null) {
            // Deserialize the JSON string back to a list of FilamentData
            val type = object : TypeToken<List<FilamentData>>() {}.type
            val data = gson.fromJson<List<FilamentData>>(json, type)

            // Log the loaded data
            Log.d("PreferencesManager", "FilamentData loaded: $data")
            data
        } else {
            // Log and return an empty list if no data is found
            Log.d("PreferencesManager", "No FilamentData found in SharedPreferences")
            emptyList()
        }
    }
}
