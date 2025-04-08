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

import AboutScreen
import ConfigScreen
import app.mrb.bambuspoolpal.ui.MainScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.shared.SharedViewModel
import app.mrb.bambuspoolpal.spoolman.SpoolmanViewModel

// Composable function for the main NFC scanner app
// This function sets up the navigation and UI for the app, integrating the main screen and configuration screen.
@Composable
fun NfcScannerApp(viewModel: ScanViewModel = viewModel(), configViewModel: ConfigViewModel = viewModel(), spoolmanViewModel: SpoolmanViewModel = viewModel(), sharedViewModel: SharedViewModel = viewModel()) {

    // Create the navigation controller for the app
    val navController = rememberNavController()

    // Set up navigation with different screens
    NavHost(navController = navController, startDestination = "main") {
        // Main screen with NFC scanner functionality
        composable("main") {
            MainScreen(navController, viewModel, spoolmanViewModel, sharedViewModel, configViewModel)
        }
        // Configuration screen to adjust settings
        composable("config") {
            ConfigScreen(configViewModel, navController)
        }

        composable("about") {
            AboutScreen()
        }

    }
}
