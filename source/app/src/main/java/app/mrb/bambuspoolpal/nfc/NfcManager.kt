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

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import app.mrb.bambuspoolpal.MainActivity
import app.mrb.bambuspoolpal.R

/**
 * NfcManager handles NFC tag detection and the associated callbacks.
 * It manages enabling/disabling NFC listening and processes NFC tags.
 */
class NfcManager(private val activity: MainActivity, val context: Context) : NfcManagerInterface {

    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)
    private val pendingIntent: PendingIntent = PendingIntent.getActivity(
        activity, 0, Intent(activity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        },
        PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    private val intentFilters = arrayOf(IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED))
    private val tagProcessor = NfcTagProcessor()

    private var callback: ((NfcTagData) -> Unit)? = null
    private var stateCallback: ((Boolean) -> Unit)? = null
    private var errorCallback: ((String) -> Unit)? = null

    private var lastTagData: ByteArray? = null
    private var isListening = false

    /**
     * Centralizes error handling by invoking the error callback with the provided message.
     *
     * @param errorMessage The error message to report.
     */
    private fun handleError(errorMessage: String) {
        errorCallback?.invoke(errorMessage)
    }

    /**
     * Sets the callback to be invoked when an NFC tag is detected.
     *
     * @param callback A lambda function that takes NfcTagData as input.
     */
    override fun setTagDetectedCallback(callback: (tagData: NfcTagData) -> Unit) {
        this.callback = callback
    }

    /**
     * Sets the callback to be invoked when the NFC listening state changes.
     *
     * @param callback A lambda function that takes a boolean (true if scanning, false otherwise) as input.
     */
    override fun setStateCallback(callback: (isScanning: Boolean) -> Unit) {
        this.stateCallback = callback
    }

    /**
     * Sets the callback to be invoked when an error occurs during NFC operations.
     *
     * @param callback A lambda function that takes an error message (String) as input.
     */
    override fun setErrorCallback(callback: (errorMessage: String) -> Unit) {
        this.errorCallback = callback
    }

    /**
     * Enables NFC tag listening. It sets up foreground dispatch to handle NFC intents.
     */
    override fun enableNfcListening() {
        if (nfcAdapter == null) {
            handleError(context.getString(R.string.nfc_not_available_on_this_device))
            return
        }

        val activity = activity as? ComponentActivity
        if (activity != null) {
            val lifecycle = activity.lifecycle
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
                stateCallback?.invoke(true)
                isListening = true
            } else {
                stateCallback?.invoke(false)
                activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, intentFilters, null)
                        stateCallback?.invoke(true)
                        isListening = true
                    }
                })
            }
        } else {
            handleError("The provided context is not an instance of ComponentActivity.")
        }
    }

    /**
     * Disables NFC tag listening by disabling foreground dispatch.
     */
    override fun disableNfcListening() {
        val activity = activity as? ComponentActivity
        activity?.let {
            if (nfcAdapter != null && nfcAdapter.isEnabled && isListening) {
                try {
                    nfcAdapter.disableForegroundDispatch(it)
                    isListening = false // Marks that listening is disabled
                } catch (e: IllegalStateException) {
                    handleError(context.getString(R.string.error_disabling_nfc_dispatch, e.message))
                }
            }
        }
    }

    /**
     * Processes a detected NFC tag from the provided intent and invokes the tag detected callback.
     *
     * @param intent The intent containing the NFC tag information.
     * @param disableAfterScan Whether to disable NFC listening after successfully scanning a tag.
     */
    override fun handleTagDetected(intent: Intent, disableAfterScan: Boolean) {
        try {
            if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                }

                tag?.let {
                    val tagData = tagProcessor.processTag(it)
                    lastTagData = tagData.bytes
                    callback?.invoke(tagData)
                } ?: handleError(context.getString(R.string.no_nfc_tag_detected))

                if (disableAfterScan) disableNfcListening()
            }
        } catch (e: Exception) {
            handleError(context.getString(R.string.there_was_an_error_reading_tag, e.message))
        }
    }

    /**
     * Clears all the currently set callbacks (tag detected, state, error).
     *
     * @return The current NfcManager instance for chaining.
     */
    override fun clearCallbacks(): NfcManagerInterface {
        callback = null
        stateCallback = null
        errorCallback = null
        return this
    }

    /**
     * Checks if NFC is available on the device.
     *
     * @return True if NFC adapter is not null, false otherwise.
     */
    override fun isNfcAvailable(): Boolean {
        return nfcAdapter != null
    }

    /**
     * Retrieves the raw data (bytes) from the last detected NFC tag.
     *
     * @return The byte array representing the last detected tag data, or null if no tag has been detected.
     */
    fun getLastTagData(): ByteArray? {
        return lastTagData
    }
}
