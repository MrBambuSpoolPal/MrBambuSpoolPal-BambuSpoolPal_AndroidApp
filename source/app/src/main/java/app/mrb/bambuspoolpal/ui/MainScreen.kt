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

import TooltipButton
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.ScanViewModel
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.shared.FilamentDetail
import app.mrb.bambuspoolpal.shared.SharedViewModel
import app.mrb.bambuspoolpal.spoolman.SpoolmanViewModel

/**
 * Main screen of the app displaying NFC scanning status, error messages, and other related information.
 * The screen includes buttons for controlling the scanning process and sending data to a remote API.
 *
 * @param navController Navigation controller to manage app navigation.
 * @param viewModel View model that manages the state of the scanning process.
 * @param spoolmanViewModel View model that manages the state of the Spoolman integration.
 */
@Composable
fun MainScreen(navController: NavHostController, viewModel: ScanViewModel, spoolmanViewModel: SpoolmanViewModel, sharedViewModel: SharedViewModel, configViewModel: ConfigViewModel) {

    // Observing scanning state, filament details, loading states, and weight from ViewModels.
    val isRoaSending by viewModel.isRoaSending.observeAsState(false)
    val isSpoolmanCheck by spoolmanViewModel.isSpoolmanCheck.observeAsState(false)
    val isSpoolmanCreateOrUpdate by spoolmanViewModel.isSpoolmanCreateOrUpdate.observeAsState(false)

    val filamentDetail by sharedViewModel.filamentDetail.collectAsState()

    // State to control the visibility of the help tooltip.
    val showTooltip = remember { mutableStateOf(false) }

    var recognizedNumberInMainScreen by remember { mutableStateOf("") }

    val numberRecognitionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            val validatedNumber = data?.getStringExtra("VALIDATED_NUMBER")
            validatedNumber?.let {
                recognizedNumberInMainScreen = it

                val newWeight = it.toIntOrNull()

                if (newWeight != null) {
                    Log.d("auto", "numberRecognitionLauncher has changed weight")
                    viewModel.onWeightChanged(newWeight, true)
                }
            }
        }
    }

    /**
     * Extension function to provide a default `FilamentDetail` instance if the observed value is null.
     *
     * @return A `FilamentDetail` instance, either the observed value or a default instance.
     */
    fun FilamentDetail?.orEmpty(): FilamentDetail {
        return this ?: FilamentDetail()
    }

    // Reset spoolmanSpool when filamentDetail changes, specifically when the tray UID changes.
    LaunchedEffect(filamentDetail?.trayUID) {
        //spoolmanViewModel.resetSpool()

        if (filamentDetail != null && filamentDetail?.filamentId == null) {
            Log.d("auto", "trayUID has changed, auto check spool")
            spoolmanViewModel.autoCheckSpool(filamentDetail)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.startScan()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopScan()
        }
    }

    // Scroll state for handling vertical scrolling in the UI.
    val scrollState = rememberScrollState()

    // Main column layout with vertical scrolling capability.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Custom top app bar with title and settings navigation.
        CustomTopAppBar(
            title = stringResource(R.string.app_name),
            onSettingsClick = { navController.navigate("config") },
            onHelpClick = { navController.navigate("about")  }
        )

        // Column for holding the main UI content.
        Column(modifier = Modifier.padding(4.dp)) {

            // Display filament details if available.
            filamentDetail.orEmpty().let { detail ->
                FilamentDetails(detail, viewModel, configViewModel)

                // Section for Spool Situation, including weight input and remaining filament info.
                Section(title = stringResource(R.string.spool_situation)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Calculate weight limits from filament details.
                        val minWeight = (filamentDetail?.emptyWeight ?: 0)
                        val maxWeight = (filamentDetail?.spoolWeight ?: 0) + (filamentDetail?.emptyWeight ?: 0)
                        val weight = (filamentDetail?.actualWeight ?: maxWeight)

                        // Styled box containing weight input field.
                        StyledBox {
                            WeightInputField(
                                viewModel = viewModel,
                                minWeight = minWeight,
                                initialWeight = weight,
                                maxWeight = maxWeight,
                                numberRecognitionLauncher = numberRecognitionLauncher
                            )
                        }

                        // Styled box displaying remaining filament length and percentage.
                        StyledBox {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val remainingLength = filamentDetail?.remainingFilamentLength ?: "0"
                                val percentRemaining = filamentDetail?.percentRemaining ?: "0"

                                Text(
                                    stringResource(
                                        R.string.remaining_filament_length_m,
                                        remainingLength,
                                        percentRemaining
                                    ),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            PercentageProgressBar(
                                sharedViewModel,
                                initialPercentage = filamentDetail?.percentRemaining ?: 0,
                                onPercentageChange = { newPercentage ->
                                    // Calculate the new weight based on the percentage.
                                    val newWeight = calculateWeightFromPercentage(newPercentage, filamentDetail)

                                    Log.d("auto", "PercentageProgressBar onPercentageChange has changed weight (no input)")
                                    viewModel.onWeightChanged(newWeight, false)
                                },
                                onDragFinished = { newPercentage ->
                                    val newWeight = calculateWeightFromPercentage(newPercentage, filamentDetail)

                                    Log.d("auto", "PercentageProgressBar onDragFinished has changed weight to $newWeight (input)")
                                    viewModel.onWeightChanged(newWeight, true)
                                }
                            )

                        }
                    }
                }
            }

            // Section for Spoolman communication functionalities.
            Section(title = stringResource(R.string.spoolman_communication)) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    StyledBox {
                        // Row containing buttons for Spoolman check and update.
                        CompactRow {

                            val isAutomationEnabled by remember { mutableStateOf(spoolmanViewModel.autoTriggerSpoolman()) }

                            TooltipButton(
                                onClick = { spoolmanViewModel.checkSpool(filamentDetail) },
                                modifier = Modifier.weight(1f),
                                tooltipMessage = "This button calls Spoolman APIs to get information about this Spool in the Database",
                                text = stringResource(R.string.check),
                                icon = Icons.Filled.Search,
                                running = isSpoolmanCheck,
                                showTooltip = showTooltip,
                                isToggled = isAutomationEnabled
                            )

                            LaunchedEffect(filamentDetail?.inputWeight) {
                                if (filamentDetail != null && filamentDetail!!.inputWeight != null) {
                                    Log.d("auto", "${filamentDetail?.inputWeight} has triggered an update spool")
                                    spoolmanViewModel.autoCreateOrUpdateSpool(filamentDetail)
                                }
                            }

                            TooltipButton(
                                onClick = {
                                    spoolmanViewModel.createOrUpdateSpool(
                                        filamentDetail
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                tooltipMessage = "This button calls Spoolman API to create or update the Filament and Spool in the Database",
                                text = stringResource(R.string.update),
                                icon = Icons.Filled.Sync,
                                running = isSpoolmanCreateOrUpdate,
                                showTooltip = showTooltip,
                                isToggled = isAutomationEnabled
                            )

                        }
                    }
                }

            }

            // Section for diagnostic tools.
            Section(title = "Diagnostics") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Row containing button to send data to ROA.
                    StyledBox {
                        CompactRow {
                            TooltipButton(
                                onClick = { viewModel.sendToRoa() },
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.send_to_backend),
                                icon = Icons.AutoMirrored.Filled.Send,
                                running = isRoaSending
                            )
                        }
                    }
                }
            }
        }
    }

}

/**
 * Calculates the weight of the filament based on the given percentage and filament details.
 *
 * @param percentage The remaining percentage of the filament on the spool.
 * @param filamentDetail Details of the filament, including empty weight and spool weight.
 * @return The calculated weight of the filament.
 */
fun calculateWeightFromPercentage(percentage: Int, filamentDetail: FilamentDetail?): Int {
    if (filamentDetail == null) return 0

    val emptyWeight = filamentDetail.emptyWeight ?: 0
    val spoolWeight = filamentDetail.spoolWeight

    return (emptyWeight + (spoolWeight * percentage / 100f)).toInt()
}

/**
 * Calculates the remaining percentage of the filament based on the current weight and filament details.
 *
 * @param weight The current weight of the filament.
 * @param filamentDetail Details of the filament, including empty weight and spool weight.
 * @return The calculated remaining percentage of the filament.
 */
fun calculatePercentageFromWeight(weight: Int, filamentDetail: FilamentDetail?): Int {
    if (filamentDetail == null) return 0

    val emptyWeight = filamentDetail.emptyWeight ?: 0
    val spoolWeight = filamentDetail.spoolWeight

    if (spoolWeight == 0) return 0 // Avoid division by zero

    val weightAboveEmpty = weight - emptyWeight

    if(weightAboveEmpty < 0) return 0 // If the weight is less than the empty weight, return 0.

    return ((weightAboveEmpty.toFloat() / spoolWeight.toFloat()) * 100).toInt()
}
