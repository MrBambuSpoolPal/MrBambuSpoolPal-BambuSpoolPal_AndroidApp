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

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import app.mrb.bambuspoolpal.nfc.NfcManager
import app.mrb.bambuspoolpal.preferences.ConfigManager
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.preferences.ConfigViewModelFactory
import app.mrb.bambuspoolpal.preferences.LocaleHelper
import app.mrb.bambuspoolpal.shared.SharedViewModel
import app.mrb.bambuspoolpal.spoolman.SpoolmanViewModel
import app.mrb.bambuspoolpal.spoolman.SpoolmanViewModelFactory
import app.mrb.bambuspoolpal.ui.SplashScreen
import app.mrb.bambuspoolpal.ui.theme.BambuspoolpalTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * MainActivity is the main entry point of the application.
 * It handles NFC tag interaction, initializes ViewModels, observes app configuration changes,
 * and sets the main UI content using Jetpack Compose.
 */
class MainActivity : ComponentActivity() {

    // Managers and ViewModels used throughout the app
    private lateinit var nfcManager: NfcManager
    private lateinit var scanViewModel: ScanViewModel
    private lateinit var configManager: ConfigManager
    private lateinit var configViewModel: ConfigViewModel
    private lateinit var spoolmanViewModel: SpoolmanViewModel
    private lateinit var sharedViewModel: SharedViewModel

    // Flag used to determine if a language switch is in progress
    private var isLanguageChanging = false

    // Launcher for restarting the activity
    private lateinit var restartLauncher: ActivityResultLauncher<Intent>

    // Queue for storing toast messages to display later
    private val toastQueue = mutableListOf<String>()

    /**
     * Sets the localized context before the activity is created.
     * Ensures the app starts with the correct language setting.
     */
    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.onAttach(newBase)
        super.attachBaseContext(context)
    }

    /**
     * Called when the activity is being created.
     * Initializes managers, ViewModels, and the user interface.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val localizedContext = LocaleHelper.onAttach(this)

        // SharedViewModel is used to share state across multiple components
        sharedViewModel = SharedViewModel(application)

        // Set localized context in SharedViewModel
        sharedViewModel.setContext(localizedContext) // Pass the current localized context

        // Initialize the configuration manager used for accessing shared preferences
        configManager = ConfigManager(this)

        // Initialize ConfigViewModel with its factory
        configViewModel = ViewModelProvider(this, ConfigViewModelFactory(configManager, sharedViewModel))[ConfigViewModel::class.java]

        // Initialize NFC manager to handle NFC scanning
        nfcManager = NfcManager(this, localizedContext)

        // Initialize SpoolmanViewModel for managing ROA interactions
        spoolmanViewModel = ViewModelProvider(this, SpoolmanViewModelFactory(configViewModel, sharedViewModel))[SpoolmanViewModel::class.java]

        // Initialize ScanViewModel for handling scanning logic and NFC intent processing
        scanViewModel = ViewModelProvider(this, ScanViewModelFactory(nfcManager, configViewModel, sharedViewModel))[ScanViewModel::class.java]

        // Observe toast messages from all ViewModels
        observeToastMessages()

        // Register a launcher to handle restarting the activity after language change
        restartLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // No specific result handling is needed here
        }

        // Set Jetpack Compose content and handle language change events
        setContent {

            val showSplashScreenInitial = configViewModel.showSplashScreen
            var showSplash by remember { mutableStateOf(showSplashScreenInitial) }

            LaunchedEffect(Unit) {
                configViewModel.languageChanged.collectLatest {
                    if (!isLanguageChanging) {
                        isLanguageChanging = true
                        scanViewModel.stopScan()

                        // Restart activity with fade animation to apply language changes
                        val intent = Intent(this@MainActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                        val options = ActivityOptions.makeCustomAnimation(
                            this@MainActivity,
                            R.anim.fade_in,
                            R.anim.fade_out
                        )

                        startActivity(intent, options.toBundle())
                        finish()
                    }
                }
            }

            // Compose UI using custom theme and ViewModels
            BambuspoolpalTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (showSplash) {
                        SplashScreen {
                            configViewModel.showSplashScreen = false
                            showSplash = false

                            // Show toasts
                            toastQueue.forEach {
                                toast(it)
                            }
                        }
                    } else {
                        NfcScannerApp(
                            viewModel = scanViewModel,
                            configViewModel = configViewModel,
                            spoolmanViewModel = spoolmanViewModel,
                            sharedViewModel = sharedViewModel
                        )
                    }
                }
            }
        }
    }

    /**
     * Called when the activity receives a new intent while already running.
     * Handles NFC tag detection by passing the intent to the ScanViewModel.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        scanViewModel.handleTag(intent, disableAfterScan = false)
    }

    /**
     * Called when the activity enters the foreground.
     * Starts NFC scanning if necessary.
     */
    override fun onResume() {
        super.onResume()
        if (isLanguageChanging) {
            // Wait until the language change completes before scanning
            Handler(Looper.getMainLooper()).post {
                scanViewModel.startScan()
                isLanguageChanging = false
            }
        } else {
            // Resume scanning immediately
            scanViewModel.startScan()
        }
    }

    /**
     * Called when the activity goes into the background.
     * Stops NFC scanning to preserve resources and battery.
     */
    override fun onPause() {
        super.onPause()
        scanViewModel.stopScan()
    }

    /**
     * Observes toast messages emitted from ViewModels and displays them to the user.
     * Helps provide feedback from background operations.
     */
    private fun observeToastMessages() {
        val toastObserver = Observer<String> { message ->
            if (configViewModel.showSplashScreen) {
                // enqueue messages while splash screen is visible
                toastQueue.add(message)
            } else {
                // show the toast immediately if no splash screen is displayed
                toast(message)
            }
        }

        // Register observer for toast messages in each relevant ViewModel
        scanViewModel.toastMessage.observe(this, toastObserver)
        configViewModel.toastMessage.observe(this, toastObserver)
        spoolmanViewModel.toastMessage.observe(this, toastObserver)
    }

    /**
     * Helper function to display a toast message.
     */
    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
