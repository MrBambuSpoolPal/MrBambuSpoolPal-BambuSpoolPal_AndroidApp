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

package app.mrb.bambuspoolpal.spoolman

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.network.ApiResponse
import app.mrb.bambuspoolpal.network.performAction
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.preferences.Constants
import app.mrb.bambuspoolpal.shared.FilamentDetail
import app.mrb.bambuspoolpal.shared.SharedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * Factory class for creating instances of SpoolmanViewModel.
 * This factory provides the necessary dependencies when creating the ViewModel.
 *
 * @property configViewModel ViewModel for managing configuration settings, used to access Spoolman API URLs and settings.
 * @property sharedViewModel ViewModel for sharing data across different parts of the application, used to access the application context.
 */
class SpoolmanViewModelFactory(
    private val configViewModel: ConfigViewModel,
    private val sharedViewModel: SharedViewModel
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of SpoolmanViewModel.
     * This method is called by the Android framework when it needs to create an instance of the ViewModel.
     *
     * @param modelClass The class of the ViewModel to create. This should be SpoolmanViewModel::class.java.
     * @return A new instance of SpoolmanViewModel.
     * @throws IllegalArgumentException if the ViewModel class is unknown or not SpoolmanViewModel.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SpoolmanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SpoolmanViewModel(configViewModel, sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel for managing Spoolman operations, including fetching and updating data related to spools and filaments.
 * This ViewModel interacts with the Spoolman API through the SpoolmanRepository.
 *
 * @property configViewModel ViewModel for managing configuration settings, providing access to Spoolman API endpoints and settings.
 * @property sharedViewModel ViewModel for sharing data across different parts of the application, providing access to the application context.
 */
class SpoolmanViewModel(
    private val configViewModel: ConfigViewModel,
    private val sharedViewModel: SharedViewModel
) : ViewModel () {

    /**
     * LiveData to indicate whether a check operation with the Spoolman API is in progress.
     */
    private val _isSpoolmanCheck = MutableLiveData(false)
    val isSpoolmanCheck: LiveData<Boolean> get() = _isSpoolmanCheck

    /**
     * LiveData to indicate whether a create or update operation with the Spoolman API is in progress.
     */
    private val _isSpoolmanCreateOrUpdate = MutableLiveData(false)
    val isSpoolmanCreateOrUpdate: LiveData<Boolean> get() = _isSpoolmanCreateOrUpdate

    /**
     * LiveData for sending transient messages (toasts) to the UI to provide feedback to the user.
     */
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    /**
     * LiveData for sending error messages to the UI to inform the user about failures.
     */
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    /**
     * Provides access to the application context through the SharedViewModel.
     */
    val context get() = sharedViewModel.contextOrDefault

    /**
     * A generic function to fetch data from the Spoolman API using a provided suspend function.
     * Handles different API response states and throws exceptions for errors.
     *
     * @param fetch A suspend function that performs the API call and returns an ApiResponse containing a list of type T.
     * @return A list of data objects of type T.
     * @throws Exception if the API returns an error (validation, network, server, client, or unknown).
     */
    private suspend fun <T> fetchData(fetch: suspend () -> ApiResponse<List<T>>): List<T> {
        return when (val result = withContext(Dispatchers.IO) { fetch() }) {
            is ApiResponse.Success -> result.data
            is ApiResponse.ValidationError -> throw Exception(
                context.getString(
                    R.string.validation_error,
                    result.validationError?.detail?.joinToString()
                ))
            is ApiResponse.NetworkError -> throw Exception(context.getString(R.string.network_error, result.message))
            is ApiResponse.ServerError -> throw Exception(context.getString(R.string.server_error, result.message))
            is ApiResponse.ClientError -> throw Exception(context.getString(R.string.client_error, result.message))
            is ApiResponse.UnknownError -> throw Exception(context.getString(R.string.network_error, result.message))
        }
    }

    /**
     * Fetches spools from the Spoolman API based on their unique ID (lot number).
     *
     * @param id The unique ID (lot number) of the spool to search for.
     * @return A list of SpoolResponse objects matching the provided ID.
     */
    private suspend fun fetchSpoolByUID(id: String): List<SpoolResponse> {
        val query = SpoolSearchQueryParams(lotNr = id)
        return fetchData { SpoolmanRepositoryImpl.getSpools(query) }
    }

    /**
     * Fetches filaments from the Spoolman API based on their external database ID.
     *
     * @param id The external database ID of the filament to search for.
     * @return A list of FilamentResponse objects matching the provided ID.
     */
    private suspend fun fetchFilamentByDatabaseId(id: String): List<FilamentResponse> {
        val query = FilamentQueryParams(external_id = id)
        return fetchData { SpoolmanRepositoryImpl.getFilaments(query) }
    }

    /**
     * Fetches vendors from the Spoolman API based on their name.
     *
     * @param name The name of the vendor to search for.
     * @return A list of Vendor objects matching the provided name.
     */
    private suspend fun fetchVendorByName(name: String): List<Vendor> {
        return fetchData { SpoolmanRepositoryImpl.getVendorsByName(name) }
    }

    /**
     * Adds a new vendor to the Spoolman API.
     *
     * @param vendor The VendorRequestBody containing the data for the new vendor.
     * @return The created Vendor object returned by the API.
     */
    private suspend fun addVendor(vendor: VendorRequestBody): Vendor {
        return performAction { SpoolmanRepositoryImpl.addVendor(vendor) }
    }

    /**
     * Adds a new filament to the Spoolman API.
     *
     * @param filament The FilamentRequestBody containing the data for the new filament.
     * @return The created FilamentResponse object returned by the API.
     */
    private suspend fun addFilament(filament: FilamentRequestBody): FilamentResponse {
        return performAction { SpoolmanRepositoryImpl.addFilament(filament) }
    }

    /**
     * Adds a new spool to the Spoolman API.
     *
     * @param spool The SpoolRequestBody containing the data for the new spool.
     * @return The created SpoolResponse object returned by the API.
     */
    private suspend fun addSpool(spool: SpoolRequestBody): SpoolResponse {
        return performAction { SpoolmanRepositoryImpl.addSpool(spool) }
    }

    /**
     * Updates an existing spool in the Spoolman API.
     *
     * @param id The ID of the spool to update.
     * @param spool The SpoolRequestBody containing the updated data for the spool.
     * @return The updated SpoolResponse object returned by the API.
     */
    private suspend fun updateSpool(id: Int, spool: SpoolRequestBody): SpoolResponse {
        return performAction { SpoolmanRepositoryImpl.updateSpool(id, spool) }
    }

    /**
     * Updates the usage information of an existing spool in the Spoolman API.
     *
     * @param id The ID of the spool to update.
     * @param spool The UseFilamentRequest containing the usage data (e.g., used weight).
     * @return The updated SpoolResponse object returned by the API.
     */
    private suspend fun updateSpoolUsage(id: Int, spool: UseFilamentRequest): SpoolResponse {
        return performAction { SpoolmanRepositoryImpl.updateSpoolUsage(id, spool) }
    }

    /**
     * Retrieves an existing vendor or creates a new one if it doesn't exist based on the provided FilamentDetail.
     *
     * @param filament The FilamentDetail object containing vendor information (vendorName, emptyWeight).
     * @return The FilamentDetail object with the vendorId set.
     * @throws Exception if the vendor name is unknown or if unable to retrieve or create the vendor.
     */
    private suspend fun getOrCreateVendor(filament: FilamentDetail): FilamentDetail {
        if (filament.vendorName == null) {
            throw Exception(context.getString(R.string.vendor_name_is_unknown))
        }

        val vendorList = fetchVendorByName(filament.vendorName)

        return if (vendorList.isEmpty()) {
            val vendorRequest = VendorRequestBody(
                name = filament.vendorName,
                comment = "",
                empty_spool_weight = filament.emptyWeight?.toDouble(),
                external_id = null,
                extra = null
            )
            filament.copy(vendorId = addVendor(vendorRequest).id)
        } else {
            vendorList.firstOrNull()?.let { filament.copy(vendorId = it.id) }
                ?: throw Exception(
                    context.getString(
                        R.string.unable_to_get_vendor_id_for,
                        filament.vendorName
                    ))
        }
    }

    /**
     * Retrieves an existing filament or creates a new one if it doesn't exist based on the provided FilamentDetail.
     *
     * @param filament The FilamentDetail object containing filament information.
     * @return The FilamentDetail object with the filamentId set.
     * @throws Exception if the filament database ID is unknown or if unable to retrieve or create the filament.
     */
    private suspend fun getOrCreateFilament(filament: FilamentDetail): FilamentDetail {
        if (filament.filamentDatabaseId == null) {
            throw Exception(context.getString(R.string.filament_database_id_is_unknown))
        }

        val filamentList = fetchFilamentByDatabaseId(filament.filamentDatabaseId)
        return if (filamentList.isEmpty()) {
            val filamentRequest = FilamentRequestBody(
                name = filament.colorName,
                vendor_id = filament.vendorId!!,
                material = filament.detailedFilamentType ?: filament.filamentType,
                price = null,
                density = filament.computedFilamentDensity ?: filament.filamentDensity ?: 0.0,
                diameter = filament.filamentDiameter?.toDouble() ?: 1.75,
                weight = filament.spoolWeight.toDouble(),
                spool_weight = filament.emptyWeight?.toDouble() ?: 250.0,
                article_number = null,
                comment = null,
                settings_extruder_temp = null,
                settings_bed_temp = null,
                color_hex = filament.colorHexString,
                multi_color_hexes = null,
                multi_color_direction = null,
                external_id = filament.filamentDatabaseId,
                extra = null
            )
            filament.copy(filamentId = addFilament(filamentRequest).id)
        } else {
            filamentList.firstOrNull()?.let { filament.copy(filamentId = it.id) }
                ?: throw Exception(
                    context.getString(
                        R.string.unable_to_get_filament_id_for,
                        filament.filamentDatabaseId
                    ))
        }
    }

    /**
     * Retrieves an existing spool or creates a new one if it doesn't exist based on the provided FilamentDetail.
     *
     * @param filament The FilamentDetail object containing spool information.
     * @return The FilamentDetail object with the spoolId set.
     * @throws Exception if the spool unique ID is unknown or if unable to retrieve, create, or update the spool.
     */
    private suspend fun getOrCreateSpool(filament: FilamentDetail): FilamentDetail {
        if (filament.trayUID == null) {
            throw Exception(context.getString(R.string.spool_unique_id_is_unknown))
        }

        val spoolList = fetchSpoolByUID(filament.trayUID!!)

        val lastUsed: String? = if (filament.usedWeight == 0) null else LocalDateTime.now().toString()

        val spoolRequest = SpoolRequestBody(
            last_used = lastUsed,
            filament_id = filament.filamentId!!,
            initial_weight = filament.spoolWeight.toDouble(),
            spool_weight = filament.emptyWeight?.toDouble(),
            used_weight = filament.usedWeight.toDouble(),
            lot_nr = filament.trayUID
        )

        val spool: SpoolResponse

        if (spoolList.isEmpty()) {

            spoolRequest.first_used = LocalDateTime.now().toString()

            spool = addSpool(spoolRequest)
            _toastMessage.value = context.getString(R.string.spool_was_created, spool.id)

            return filament.copy(spoolId = spool.id)

        } else {

            val spoolId = spoolList.first().id

            spool = updateSpool(spoolId, spoolRequest)
            _toastMessage.value = context.getString(R.string.spool_was_updated, spoolId)

            return filament.copy(spoolId = spool.id)
        }

    }

    /**
     * Retrieves the spool details from the Spoolman API based on the provided FilamentDetail's tray UID.
     *
     * @param filament The FilamentDetail object containing spool identification information.
     * @return The FilamentDetail object with spoolId and actualWeight updated from the retrieved SpoolResponse.
     * @throws Exception if the spool unique ID is unknown or if the spool is not found in Spoolman.
     */
    private suspend fun getSpool(filament: FilamentDetail): FilamentDetail {
        if (filament.trayUID == null) {
            throw Exception(context.getString(R.string.spool_unique_id_is_unknown))
        }

        val spoolList = fetchSpoolByUID(filament.trayUID!!)

        val firstSpool = spoolList.firstOrNull()
        if (firstSpool != null) {
            return filament.copy(
                spoolId = firstSpool.id,
                actualWeight = (firstSpool.spool_weight ?: 0) + (firstSpool.remaining_weight?.toInt() ?: 0),
                filamentDatabaseId = firstSpool.filament?.external_id,
                filamentId = firstSpool.filament?.id,
                vendorId = firstSpool.filament?.vendor?.id
            )
        } else {
            _toastMessage.value = context.getString(R.string.spool_not_found_in_spoolman)
            return filament
        }
    }

    /**
     * Automatically triggers the creation or update of a spool in Spoolman if auto-triggering is enabled in the configuration.
     *
     * @param filamentDetail The FilamentDetail object containing the information needed to create or update the spool.
     */
    fun autoCreateOrUpdateSpool(filamentDetail: FilamentDetail?) {
        if (configViewModel.getSpoolmanAutoTrigger()) {
            Log.d("auto", "auto update spool")

            _toastMessage.value = context.getString(R.string.auto_updating_spool)
            createOrUpdateSpool(filamentDetail)
        }
    }

    /**
     * Checks if the auto-triggering feature for Spoolman operations is enabled in the configuration.
     *
     * @return True if auto-triggering is enabled, false otherwise.
     */
    fun autoTriggerSpoolman() : Boolean {
        return configViewModel.getSpoolmanAutoTrigger()
    }

    /**
     * Creates a new spool in the Spoolman system or updates an existing one if a spool with the same unique ID is found.
     *
     * @param filamentDetail The FilamentDetail object containing the spool information to create or update.
     */
    fun createOrUpdateSpool(filamentDetail: FilamentDetail?) {
        viewModelScope.launch {
            try {
                _isSpoolmanCreateOrUpdate.value = true
                if (filamentDetail == null) {
                    _toastMessage.value = context.getString(R.string.you_need_to_scan_a_spool_first)
                    return@launch
                }

                var filament = filamentDetail
                if (filament.vendorName == null) {
                    filament = filament.copy(vendorName = Constants.MANUFACTURER_BAMBU_LAB)
                }

                filament = getOrCreateVendor(filament)
                filament = getOrCreateFilament(filament)
                sharedViewModel.updateFilamentDetail(getOrCreateSpool(filament), true)

            } catch (e: Exception) {
                _toastMessage.value =
                    context.getString(R.string.error_while_creating_or_updating_spool, e.message)
            } finally {
                _isSpoolmanCreateOrUpdate.value = false
            }
        }
    }

    /**
     * Automatically triggers the checking of spool details in Spoolman if auto-triggering is enabled and a FilamentDetail is provided.
     *
     * @param filamentDetail The FilamentDetail object containing the information needed to check the spool.
     * @return The (potentially updated) FilamentDetail object.
     */
    fun autoCheckSpool(filamentDetail: FilamentDetail?): FilamentDetail? {
        if (configViewModel.getSpoolmanAutoTrigger() && filamentDetail != null) {
            Log.d("auto", "auto check spool")

            _toastMessage.value = context.getString(R.string.auto_checking_spool)
            checkSpool(filamentDetail)
        }
        return filamentDetail
    }

    /**
     * Checks the details of a spool in the Spoolman system based on the provided FilamentDetail's tray UID.
     *
     * @param filamentDetail The FilamentDetail object containing the spool identification information.
     */
    fun checkSpool(filamentDetail: FilamentDetail?) {
        viewModelScope.launch {
            try {
                _isSpoolmanCheck.value = true
                if (filamentDetail == null) {
                    _toastMessage.value = context.getString(R.string.you_need_to_scan_a_spool_first)
                    return@launch
                }

                var filament = filamentDetail
                if (filament.vendorName == null) {
                    filament = filament.copy(vendorName = Constants.MANUFACTURER_BAMBU_LAB)
                }

                // Force update filament details
                sharedViewModel.updateFilamentDetail(getSpool(filament), true)

            } catch (e: Exception) {
                _toastMessage.value = context.getString(
                    R.string.error_while_retrieving_information_on_spool,
                    e.message
                )
            } finally {
                _isSpoolmanCheck.value = false
            }
        }
    }
}