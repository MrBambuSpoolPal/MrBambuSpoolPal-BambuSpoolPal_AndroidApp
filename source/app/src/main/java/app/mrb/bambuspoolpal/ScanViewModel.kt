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

package app.mrb.bambuspoolpal

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.mrb.bambuspoolpal.bambu.TagDecoder
import app.mrb.bambuspoolpal.network.performAction
import app.mrb.bambuspoolpal.nfc.NfcManager
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.preferences.Constants
import app.mrb.bambuspoolpal.proxy.SpoolManProxy
import app.mrb.bambuspoolpal.shared.FilamentData
import app.mrb.bambuspoolpal.shared.SharedViewModel
import kotlinx.coroutines.launch
import java.time.LocalDateTime

/**
 * Factory class for creating instances of ScanViewModel.
 *
 * @property nfcManager The NFC manager responsible for handling NFC operations.
 * @property configViewModel The view model responsible for managing configuration settings.
 * @property sharedViewModel The view model responsible for sharing data across the application.
 */
class ScanViewModelFactory(
    private val nfcManager: NfcManager,
    private val configViewModel: ConfigViewModel,
    private val sharedViewModel: SharedViewModel
) : ViewModelProvider.Factory {

    /**
     * Creates a new instance of ScanViewModel.
     *
     * @param modelClass The class of the ViewModel to be created.
     * @return A new instance of ScanViewModel.
     * @throws IllegalArgumentException if the provided ViewModel class is unknown.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScanViewModel(nfcManager, configViewModel, sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

/**
 * ViewModel to manage the NFC scanning process and handle communication with the UI.
 * This ViewModel processes NFC tag data, manages user input, and communicates with the Spoolman API.
 *
 * @property nfcManager The NFC manager responsible for handling NFC operations.
 * @property configViewModel The view model responsible for managing configuration settings.
 * @property sharedViewModel The view model responsible for sharing data across the application.
 */
class ScanViewModel(
    private val nfcManager: NfcManager,
    private val configViewModel: ConfigViewModel,
    private val sharedViewModel: SharedViewModel
) : ViewModel()  {

    /**
     * Provides access to the application context through the SharedViewModel.
     */
    val context get() = sharedViewModel.contextOrDefault

    /**
     * LiveData to indicate whether NFC scanning is currently active.
     */
    private val _isScanning = MutableLiveData(false)
    val isScanning: LiveData<Boolean> get() = _isScanning

    /**
     * LiveData to indicate whether data is currently being sent to the ROA (Remote Object API).
     */
    private val _isRoaSending = MutableLiveData(false)
    val isRoaSending: LiveData<Boolean> get() = _isRoaSending

    /**
     * LiveData for sending toast messages to the UI, used for displaying user feedback.
     */
    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    /**
     * LiveData to hold the Spoolman database base URL, retrieved from application settings.
     */
    private val spoolmanDatabaseBaseUrl = MutableLiveData<String>().apply {
        value = configViewModel.getProxyEndpoint()
    }

    /**
     * LiveData to hold the vendor name, retrieved from application settings.
     */
    private val vendorName = MutableLiveData<String>().apply {
        value = configViewModel.getManufacturer()
    }

    /**
     * Starts the NFC scanning process by enabling NFC listening and setting callbacks.
     */
    fun startScan() {
        _isScanning.value = true
        nfcManager.enableNfcListening()
        nfcManager.setTagDetectedCallback { tagData ->
            // update force to false in case of re-scan
            if (sharedViewModel.updateFilamentDetail(configViewModel.filamentDataList.value?.let { dataList ->
                TagDecoder.parseTagDetails(tagData, dataList).copy(vendorName = vendorName.value)
            }, false)) {
                _toastMessage.value = context.getString(R.string.spool_scan_finished)
            } else {
                _toastMessage.value = context.getString(R.string.same_spool_has_been_scanned)
            }

            val spoolWeight = sharedViewModel.filamentDetail.value?.spoolWeight
            val emptyWeight = sharedViewModel.filamentDetail.value?.emptyWeight

            if (sharedViewModel.filamentDetail.value?.actualWeight == null && spoolWeight != null && emptyWeight != null) {
                Log.d("auto", "startScan has changed weight (no input)")
                onWeightChanged(spoolWeight + emptyWeight, false)
            }

        }
        nfcManager.setErrorCallback { error ->
            _toastMessage.value = context.getString(R.string.unable_to_read_rfid_tag_please)
        }
    }

    /**
     * Stops the NFC scanning process by disabling NFC listening.
     */
    fun stopScan() {
        _isScanning.value = false
        nfcManager.disableNfcListening()
    }

    /**
     * Handles user input for weight and updates the filament detail with the new weight.
     *
     * @param weight The weight input by the user.
     * @param isInput Indicates whether the weight was manually input by the user.
     */
    fun onWeightChanged(weight: Int, isInput: Boolean) {

        sharedViewModel.filamentDetail.value?.let {
            sharedViewModel.updateFilamentDetail(
                it.copy(
                    actualWeight = weight,
                    inputWeight = if (isInput) LocalDateTime.now() else it.inputWeight
                ), true
            )
        }
    }

    /**
     * Handles the selection of an item from the database and updates the filament detail with the selected item's data.
     *
     * @param item The selected item's ID from the database.
     */
    fun onDatabaseItemSelection(item: String) {
        sharedViewModel.updateFilamentDetail(sharedViewModel.filamentDetail.value?.let { filamentDetail ->
            val selectedItem = configViewModel.filamentDataList.value?.find { it.id == item }

            selectedItem?.let { filamentData: FilamentData ->
                Log.d("auto", "onDatabaseItemSelection - filamentDatabaseId set to $item")
                filamentDetail.copy(
                    filamentDatabaseId = item,
                    emptyWeight = filamentData.empty_spool_weight,
                    colorName = filamentData.name,
                    filamentDensity = filamentData.density,
                    defaultFilamentDatabaseId = false
                )
            } ?: filamentDetail.copy(filamentDatabaseId = item)
        }, true)
    }

    /**
     * Sends the current filament data to the ROA (Remote Object API) using the Spoolman proxy.
     */
    fun sendToRoa() {
        viewModelScope.launch {
            _isRoaSending.value = true

            val tagData = nfcManager.getLastTagData()
            val weightValue = sharedViewModel.filamentDetail.value?.actualWeight

            if (tagData == null || weightValue == null) {
                _toastMessage.value = context.getString(R.string.tag_data_or_weight_is_missing)
                _isRoaSending.value = false
                return@launch
            }

            val result = try {
                performAction {
                    SpoolManProxy.uploadFilamentData(
                        spoolmanDatabaseBaseUrl.value ?: Constants.SPOOLMAN_PROXY_ENDPOINT,
                        tagData,
                        weightValue
                    )
                }
                true // Indicates successful execution of performAction
            } catch (e: Exception) {
                _toastMessage.value = context.getString(R.string.error_during_upload, e.message)
                Log.e("FilamentViewModel", "Upload error", e)
                false // Indicates failed execution of performAction
            } finally {
                _isRoaSending.value = false
            }

            if (result) {
                _toastMessage.value = context.getString(R.string.data_uploaded_successfully)
            }
        }
    }

    /**
     * Handles the detection of an NFC tag from an intent.
     *
     * @param intent The intent containing the NFC tag information.
     * @param disableAfterScan Whether to disable NFC listening after scanning the tag.
     */
    fun handleTag(intent: Intent, disableAfterScan: Boolean) {
        nfcManager.handleTagDetected(intent, disableAfterScan = disableAfterScan)
    }
}
