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

package app.mrb.bambuspoolpal.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import app.mrb.bambuspoolpal.NfcScannerApp

/**
 * A composable function that provides a preview of the app's UI.
 * This function is used for previewing the NfcScannerApp in Android Studio's preview pane.
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    // This will display the NfcScannerApp in the preview
    NfcScannerApp()
}
