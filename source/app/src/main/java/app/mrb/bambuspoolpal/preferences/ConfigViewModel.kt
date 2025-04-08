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

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.network.ApiResponse
import app.mrb.bambuspoolpal.shared.FilamentData
import app.mrb.bambuspoolpal.shared.SharedViewModel
import app.mrb.bambuspoolpal.spoolman.FilamentDatabase
import app.mrb.bambuspoolpal.spoolman.SpoolmanApiClient
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Extension function to create a MutableStateFlow for a String configuration value.
 * This allows observing changes to a String configuration setting.
 * @receiver ConfigManager The instance of ConfigManager to extend.
 * @param key The key of the configuration to retrieve.
 * @param defaultValue The default value to use if the configuration is not found.
 * @return A MutableStateFlow holding the current String configuration value.
 */
fun ConfigManager.getConfigFlow(key: String, defaultValue: String): MutableStateFlow<String> {
    return MutableStateFlow(getConfig(key, defaultValue))
}

/**
 * Extension function to create a MutableStateFlow for a Boolean configuration value.
 * This allows observing changes to a Boolean configuration setting.
 * @receiver ConfigManager The instance of ConfigManager to extend.
 * @param key The key of the configuration to retrieve.
 * @param defaultValue The default value to use if the configuration is not found.
 * @return A MutableStateFlow holding the current Boolean configuration value.
 */
fun ConfigManager.getConfigFlow(key: String, defaultValue: Boolean): MutableStateFlow<Boolean> {
    return MutableStateFlow(getConfig(key, defaultValue))
}

/**
 * Factory for creating instances of ConfigViewModel.
 * This factory is responsible for providing the necessary dependencies to the ViewModel.
 * @property configManager The ConfigManager instance used by the ViewModel.
 * @property sharedViewModel The SharedViewModel instance used by the ViewModel.
 */
class ConfigViewModelFactory(
    private val configManager: ConfigManager,
    private val sharedViewModel: SharedViewModel
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of ConfigViewModel.
     * @param modelClass The class of the ViewModel to create. This should be ConfigViewModel::class.java.
     * @return A new instance of ConfigViewModel.
     * @throws IllegalArgumentException if the ViewModel class is unknown or not ConfigViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConfigViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConfigViewModel(configManager, sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for managing application configurations and preferences.
 * It interacts with ConfigManager to load, save, and reset configurations.
 * It also handles fetching and filtering filament data from a database or API.
 * @property configManager The ConfigManager instance for managing configurations.
 * @property sharedViewModel The SharedViewModel instance for accessing shared application context.
 */
class ConfigViewModel(private val configManager: ConfigManager, private val sharedViewModel: SharedViewModel) : ViewModel() {

    /**
     * Provides access to the application context through the SharedViewModel.
     */
    val context get() = sharedViewModel.contextOrDefault

    /**
     * LiveData holding the currently selected language.
     */
    private val _language = MutableLiveData(LocaleHelper.getPersistedLanguage(context))
    val language: LiveData<String> = _language

    /**
     * SharedFlow to notify observers when the language has changed.
     */
    private val _languageChanged = MutableSharedFlow<Unit>(replay = 0)
    val languageChanged = _languageChanged

    /**
     * Map holding the initial default values for various configuration keys.
     */
    private val initialConfigValues = mapOf(
        Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM to Constants.SPOOLMAN_DB_FILAMENTS_URL,
        Constants.MANUFACTURER_BAMBU_LAB_PARAM to Constants.MANUFACTURER_BAMBU_LAB,
        Constants.SPOOLMAN_PROXY_BASE_URL_PARAM to Constants.SPOOLMAN_PROXY_BASE_URL,
        Constants.SPOOLMAN_PROXY_ENDPOINT_PARAM to Constants.SPOOLMAN_PROXY_ENDPOINT,
        Constants.SPOOLMAN_BASE_API_URL_PARAM to Constants.SPOOLMAN_BASE_API_URL,
        Constants.SPOOLMAN_API_SELF_SIGNED_PARAM to Constants.SPOOLMAN_API_SELF_SIGNED,
        Constants.SPOOLMAN_API_AUTO_TRIGGERING_PARAM to Constants.SPOOLMAN_API_AUTO_TRIGGERING,
        Constants.SHOW_SPOOL_UID_PARAM to Constants.SHOW_SPOOL_UID,
        Constants.SHOW_SPLASH_SCREEN_PARAM to Constants.SHOW_SPLASH_SCREEN
    )

    /**
     * Map to store MutableStateFlows for each configuration value.
     * This allows for observing changes to individual configuration settings.
     */
    private val configFlows = initialConfigValues.mapValues { (key, defaultValue) ->
        when (defaultValue) {
            is String -> configManager.getConfigFlow(key, defaultValue)
            is Boolean -> configManager.getConfigFlow(key, defaultValue)
            else -> null // Default to String Flow if type is unknown
        }
    }

    /**
     * Map of configuration keys to their immutable StateFlow representations.
     * Exposed for read-only access to configuration values.
     */
    val configStateFlows = configFlows.mapValues { it.value?.asStateFlow() }

    /**
     * MutableLiveData for displaying transient messages (toasts) in the UI.
     */
    private val _toastMessage = MutableLiveData<String>()

    /**
     * LiveData to observe toast messages for UI feedback.
     */
    val toastMessage: LiveData<String> get() = _toastMessage

    /**
     * Retrieves the base URL of the Spoolman database from the configuration.
     * Extracts the base URL by removing the last segment (the endpoint).
     * @return The base URL of the Spoolman database.
     */
    fun getSpoolmanBaseUrl(): String {
        return configManager.getConfig(Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM, initialConfigValues[Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM].toString()).substringBeforeLast("/")
            .plus("/")
    }

    /**
     * Retrieves the endpoint of the Spoolman database from the configuration.
     * Extracts the endpoint by taking the part after the last "/".
     * @return The endpoint of the Spoolman database.
     */
    fun getSpoolmanEndpoint(): String {
        return configManager.getConfig(Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM, initialConfigValues[Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM].toString()).substringAfterLast("/", "")
    }

    /**
     * Retrieves the configured Bambu Lab manufacturer value.
     * @return The Bambu Lab manufacturer value.
     */
    fun getManufacturer(): String {
        return configManager.getConfig(Constants.MANUFACTURER_BAMBU_LAB_PARAM, initialConfigValues[Constants.MANUFACTURER_BAMBU_LAB_PARAM].toString())
    }

    /**
     * Determines whether to show the spool's unique ID in the UI based on the configuration.
     * @return True if the spool UID should be shown, false otherwise.
     */
    fun showSpoolUid(): Boolean {
        return (getConfiguration(Constants.SHOW_SPOOL_UID_PARAM) as? Boolean) ?: Constants.SHOW_SPOOL_UID
    }

    /**
     * Retrieves the configured proxy endpoint for communication with Spoolman.
     * @return The proxy endpoint URL.
     */
    fun getProxyEndpoint(): String {
        return configManager.getConfig(Constants.SPOOLMAN_PROXY_ENDPOINT_PARAM, initialConfigValues[Constants.SPOOLMAN_PROXY_ENDPOINT_PARAM].toString())
    }

    /**
     * MutableLiveData to hold the count of available filament data items.
     */
    private val _filamentDataCount = MutableLiveData(0)

    /**
     * LiveData holding the count of filament data items.
     */
    val filamentDataCount: LiveData<Int> get() = _filamentDataCount

    /**
     * MutableLiveData to hold the list of available filament data.
     */
    private val _filamentDataList = MutableLiveData<List<FilamentData>>()

    /**
     * LiveData holding a list of filament data.
     */
    val filamentDataList: LiveData<List<FilamentData>> get() = _filamentDataList

    /**
     * MutableLiveData to indicate whether a data sending operation is in progress.
     */
    private val _isSending = MutableLiveData(false)

    /**
     * LiveData representing whether data is being sent to the ROA (Remote Object API).
     */
    val isSending: LiveData<Boolean> get() = _isSending

    /**
     * Initialization block executed when the ViewModel is created.
     * Loads initial configurations, initializes the Spoolman API client, and loads filament data.
     */
    init {
        try {
            loadConfigurations() // Load configurations from persistent storage
            SpoolmanApiClient.initialize(this) // Initialize the Spoolman API client with this ViewModel
            loadFilamentData() // Load locally saved filament data

        } catch (e: Exception) {
            // Handle any exceptions that occur during initialization
            _toastMessage.value = context.getString(R.string.error_while_initialising_preferences)
        }
    }

    /**
     * Retrieves the currently selected language.
     * @return The language code of the selected language.
     */
    fun getLanguage(): String {
        return LocaleHelper.getPersistedLanguage(context)
    }

    /**
     * Sets the application language and updates the persisted setting.
     * Emits an event to notify observers that the language has changed.
     * @param newLanguage The new language code to set.
     */
    fun setLanguage(newLanguage: String) {
        LocaleHelper.setLocale(context, newLanguage) // Save the language to preferences via LocaleHelper
        _language.value = newLanguage
        viewModelScope.launch {
            _languageChanged.emit(Unit) // Emit an event indicating that the language has changed
        }
    }

    /**
     * Saves the current configuration values to persistent storage (SharedPreferences).
     */
    fun saveConfig() {
        viewModelScope.launch {
            try {
                configFlows.forEach { (key, flow) ->
                    if (flow != null) {
                        when (val value = flow.value) {
                            is String -> configManager.saveConfig(key, value)
                            is Boolean -> configManager.saveConfig(key, value)
                            else -> {} // Ignore if type is unknown
                        }
                    }
                }

                _toastMessage.postValue(context.getString(R.string.configuration_saved_successfully))
            } catch (e: Exception) {
                _toastMessage.postValue(context.getString(R.string.error_while_saving_preferences))
            }
        }
    }

    /**
     * Resets all configuration values to their initial default states.
     */
    fun resetConfig() {
        viewModelScope.launch {
            try {
                configFlows.forEach { (key, flow) ->
                    when (val defaultValue = initialConfigValues[key]) {
                        is String -> (flow as? MutableStateFlow<String>)?.value = configManager.getConfig(key, defaultValue)
                        is Boolean -> (flow as? MutableStateFlow<Boolean>)?.value = configManager.getConfig(key, defaultValue)
                        else -> configManager.getConfig(key, "") // Default to String
                    }
                }
                _toastMessage.postValue(context.getString(R.string.changes_have_been_discarded))
            } catch (e: Exception) {
                // Handle the exception (e.g., display an error message)
                _toastMessage.postValue(context.getString(R.string.error_while_resetting_preferences))
            }
        }
    }

    /**
     * Updates a specific configuration key with a new value and saves it.
     * Also updates the corresponding StateFlow to reflect the change.
     * @param key The configuration key to update.
     * @param value The new value for the configuration key.
     */
    fun updateConfig(key: String, value: Any) {
        viewModelScope.launch {
            try {
                when (value) {
                    is String -> {
                        configManager.saveConfig(key, value)
                        (configFlows[key] as? MutableStateFlow<String>)?.value = value
                    }
                    is Boolean -> {
                        configManager.saveConfig(key, value)
                        (configFlows[key] as? MutableStateFlow<Boolean>)?.value = value
                    }
                    else -> {} // Ignore if type is unknown
                }
            } catch (e: Exception) {
                Log.e("ConfigScreen", "Error: ${e.message}", e)
            }
        }
    }

    /**
     * Loads configurations from ConfigManager and updates the corresponding StateFlows.
     */
    private fun loadConfigurations() {
        try {
            configFlows.forEach { (key, flow) ->
                when (val defaultValue = initialConfigValues[key]) {
                    is String -> (flow as MutableStateFlow<String>).value =
                        configManager.getConfig(key, defaultValue)

                    is Boolean -> (flow as MutableStateFlow<Boolean>).value =
                        configManager.getConfig(key, defaultValue)
                }
            }
        } catch (e: Exception) {
            _toastMessage.postValue(context.getString(R.string.error_while_loading_preferences))
        }
    }

    /**
     * Retrieves a specific configuration value as an Any?.
     * This provides a way to access configuration values dynamically.
     * @param name The name of the configuration to retrieve.
     * @return The configuration value or null if not found.
     */
    private fun getConfiguration(name: String): Any? {
        try {
            return configStateFlows[name]?.value
        } catch (e: Exception) {
            // Handle the exception (e.g., display an error message)
            Log.e("ConfigScreen", "Error: ${e.message}", e)
        }
        return null
    }

    /**
     * Retrieves the configured Spoolman API base URL.
     * Reads the value from the configuration and ensures it ends with a "/".
     * @return The Spoolman API base URL.
     */
    fun getSpoolmanApiBaseUrl(): String {
        val baseUrlAny = getConfiguration(Constants.SPOOLMAN_BASE_API_URL_PARAM) ?: initialConfigValues[Constants.SPOOLMAN_BASE_API_URL_PARAM]
        val baseUrl = baseUrlAny?.toString() ?: "" // Cast to String and handle null

        return if (!baseUrl.endsWith("/")) "$baseUrl/" else baseUrl
    }

    /**
     * Retrieves the configured setting for accepting self-signed certificates for the Spoolman API.
     * @return True if self-signed certificates should be accepted, false otherwise.
     */
    fun getSpoolmanApiSelfSigned(): Boolean {
        return (getConfiguration(Constants.SPOOLMAN_API_SELF_SIGNED_PARAM) as? Boolean) ?: Constants.SPOOLMAN_API_SELF_SIGNED
    }

    var showSplashScreen: Boolean
        get() = (getConfiguration(Constants.SHOW_SPLASH_SCREEN_PARAM) as? Boolean) ?: Constants.SHOW_SPLASH_SCREEN
        set(value) = configManager.saveConfig(Constants.SHOW_SPLASH_SCREEN_PARAM, value)

    /**
     * Retrieves the configured setting for automatically triggering Spoolman actions.
     * @return True if auto-triggering for Spoolman is enabled, false otherwise.
     */
    fun getSpoolmanAutoTrigger(): Boolean {
        return (getConfiguration(Constants.SPOOLMAN_API_AUTO_TRIGGERING_PARAM) as? Boolean) ?: Constants.SPOOLMAN_API_AUTO_TRIGGERING
    }

    /**
     * Fetches and filters filament data from the database or API.
     * Updates LiveData with the fetched and filtered data and shows a toast message to the user.
     * @param configViewModel The ConfigViewModel instance providing necessary configurations.
     */
    fun fetchAndFilterFilamentData(configViewModel: ConfigViewModel) {
        viewModelScope.launch {

            _isSending.postValue(true) // Indicate that data sending is starting

            try {

                when (val result = FilamentDatabase.getFilamentData(configViewModel)) {
                    is ApiResponse.Success -> {
                        val filteredData = result.data
                        configManager.saveFilamentDataLocally(filteredData) // Save the fetched data locally
                        _filamentDataList.postValue(filteredData) // Update the LiveData with the fetched data
                        _filamentDataCount.postValue(filteredData.size) // Update the count of filament data items
                        _toastMessage.postValue(context.getString(R.string.data_fetched_successfully)) // Show a success message
                    }
                    is ApiResponse.NetworkError -> {
                        _toastMessage.postValue(
                            context.getString(R.string.network_error, result.message)
                        )
                    }
                    is ApiResponse.ServerError -> {
                        _toastMessage.postValue(
                            context.getString(R.string.server_error, result.message)
                        )
                    }
                    is ApiResponse.ClientError -> {
                        _toastMessage.postValue(
                            context.getString(R.string.client_error, result.message)
                        )
                    }
                    is ApiResponse.UnknownError -> {
                        _toastMessage.postValue(
                            context.getString(R.string.unknown_error, result.message)
                        )
                    }
                    else -> {
                        // Handle unexpected ApiResponse subtypes
                        _toastMessage.postValue(context.getString(R.string.unexpected_error_occurred))
                    }
                }
            } catch (e: Exception) {
                _toastMessage.postValue(context.getString(R.string.error_fetching_data, e.message)) // Handle any exceptions during data fetching
            }

            _isSending.postValue(false) // Indicate that data sending has finished
        }
    }

    /**
     * Initialization block that loads locally saved filament data when the ViewModel is created.
     */
    init {
        loadFilamentData() // Load filament data from local storage
    }

    /**
     * Loads filament data from SharedPreferences and updates the corresponding LiveData.
     */
    private fun loadFilamentData() {
        viewModelScope.launch {
            try {
                val savedFilamentData = configManager.loadFilamentDataLocally()
                _filamentDataList.postValue(savedFilamentData) // Update LiveData with the loaded data
                _filamentDataCount.postValue(savedFilamentData.size) // Update the count of filament data items
                if (savedFilamentData.isEmpty()) {
                    _toastMessage.postValue(context.getString(R.string.local_filament_database_is_empty_please_use_fetch_spoolman_database_from_configuration_screen))
                } else {
                    _toastMessage.postValue(context.getString(R.string.data_fetched_successfully)) // Show a success message
                }
            } catch (e: Exception) {
                _toastMessage.postValue(
                    context.getString(R.string.error_loading_filament_data, e.message) // Show an error message if loading fails
                )
            }
        }
    }
}