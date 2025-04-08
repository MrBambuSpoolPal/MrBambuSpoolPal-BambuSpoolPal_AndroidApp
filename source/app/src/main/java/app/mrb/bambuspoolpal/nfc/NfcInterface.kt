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

package app.mrb.bambuspoolpal.nfc

import android.content.Intent

/**
 * Interface for managing NFC (Near Field Communication) operations.
 * This includes enabling/disabling NFC scanning, handling detected tags,
 * setting various callbacks for tag detection, state changes, and error handling.
 */
interface NfcManagerInterface {

    /**
     * Enable NFC listening to start scanning for NFC tags.
     * This function should begin the NFC scanning process.
     */
    fun enableNfcListening()

    /**
     * Disable NFC listening to stop scanning for NFC tags.
     * This function should stop the NFC scanning process.
     */
    fun disableNfcListening()

    /**
     * Handle the detected NFC tag and process it based on the given intent.
     *
     * @param intent The intent containing NFC tag data.
     * @param disableAfterScan If true, disable NFC listening after a tag is detected.
     */
    fun handleTagDetected(intent: Intent, disableAfterScan: Boolean)

    /**
     * Set a callback that will be invoked when a tag is detected.
     * The callback will receive the NFC tag data.
     *
     * @param callback The callback function to handle tag data.
     */
    fun setTagDetectedCallback(callback: (tagData: NfcTagData) -> Unit)

    /**
     * Set a callback for state changes (whether NFC scanning is active or not).
     * The callback will notify whether NFC scanning is enabled or disabled.
     *
     * @param callback The callback function to handle state changes.
     */
    fun setStateCallback(callback: (isScanning: Boolean) -> Unit)

    /**
     * Set a callback for error messages, to handle any issues during NFC operations.
     *
     * @param callback The callback function to handle error messages.
     */
    fun setErrorCallback(callback: (errorMessage: String) -> Unit)

    /**
     * Clear all the previously set callbacks.
     * This will remove any active listeners or handlers.
     *
     * @return The instance of NfcManagerInterface for chaining.
     */
    fun clearCallbacks(): NfcManagerInterface

    /**
     * Check if NFC is available on the device.
     *
     * @return True if NFC is available, false otherwise.
     */
    fun isNfcAvailable(): Boolean
}
