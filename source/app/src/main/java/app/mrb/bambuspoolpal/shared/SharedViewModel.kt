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

package app.mrb.bambuspoolpal.shared

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * SharedViewModel is used to manage and share the FilamentDetail across different parts of the app.
 * This ViewModel holds a MutableStateFlow that can be observed by Composables or other ViewModels.
 * It also provides a function to update the FilamentDetail value.
 */
class SharedViewModel @Inject constructor(application: Application) : AndroidViewModel(application) {

    // StateFlow to share and manage the filamentDetail.
    // It's mutable internally but exposed as immutable externally.
    private val _filamentDetail = MutableStateFlow<FilamentDetail?>(null)
    val filamentDetail: StateFlow<FilamentDetail?> = _filamentDetail

    private val _context = mutableStateOf<Context?>(null)
    val context: State<Context?> = _context

    /**
     * Sets the application context.
     *
     * @param context The application context to set.
     */
    fun setContext(context: Context) {
        _context.value = context
    }

    /**
     * Returns a non-nullable context, using a default context if necessary.
     */
    val contextOrDefault: Context
        get() = _context.value ?: getApplication<Application>().applicationContext

    /**
     * Function to update the filamentDetail.
     * This method allows modification of the current filamentDetail state.
     *
     * @param newFilamentDetail the new FilamentDetail object to set.
     */
    fun updateFilamentDetail(newFilamentDetail: FilamentDetail?, force: Boolean) : Boolean {
        // Manage re-scan
        if (newFilamentDetail != null) {
            if (force || newFilamentDetail.trayUID != _filamentDetail.value?.trayUID) {
                Log.d("auto", "shared filament has been updated")
                _filamentDetail.value = newFilamentDetail

                return true
            }
        }
        return false
    }
}
