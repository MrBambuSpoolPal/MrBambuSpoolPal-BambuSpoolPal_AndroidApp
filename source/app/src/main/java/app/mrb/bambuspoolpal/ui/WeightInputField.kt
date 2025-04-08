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

import android.content.Intent
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LastPage
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FirstPage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.ScanViewModel
import app.mrb.bambuspoolpal.camera.NumberRecognitionActivity

/**
 * Weight input field with validation and buttons to set weight limits.
 *
 * @param viewModel The ViewModel to update the weight.
 * @param minWeight The minimum allowed weight value in grams.
 * @param initialWeight The initial weight value in grams. Can be null.
 * @param maxWeight The maximum allowed weight value in grams.
 */
@Composable
fun WeightInputField(
    viewModel: ScanViewModel,
    minWeight: Int,
    initialWeight: Int?,
    maxWeight: Int,
    numberRecognitionLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {

    /**
     * Helper function to validate the weight input against the minimum and maximum allowed values.
     * If the input is outside the valid range, it returns the fallback value or the minimum weight.
     *
     * @param input The weight input value to validate.
     * @param minWeight The minimum allowed weight.
     * @param maxWeight The maximum allowed weight.
     * @param fallback The fallback weight value to use if the input is invalid.
     * @return The validated weight value.
     */
    fun validateWeight(input: Int?, minWeight: Int, maxWeight: Int, fallback: Int?): Int {
        return input?.takeIf { it in minWeight..maxWeight } ?: fallback ?: minWeight
    }

    // Label for the weight input field.
    val label = stringResource(R.string.actual_g)
    // Constant text for the button to set the minimum weight.
    val constant1 = "$minWeight g"
    // Constant text for the button to set the maximum weight.
    val constant2 = "$maxWeight g"

    // State variable to hold the temporary weight input value.
    var tempWeight by remember { mutableStateOf(initialWeight) }
    // State variable to store the previous valid weight value.
    var previousWeight by remember { mutableStateOf(initialWeight) }

    // LaunchedEffect to update tempWeight and previousWeight when initialWeight changes.
    LaunchedEffect(initialWeight) {
        tempWeight = initialWeight
        previousWeight = initialWeight
    }

    // Controller for managing the software keyboard.
    val keyboardController = LocalSoftwareKeyboardController.current
    // State variable to track if the input field is currently focused.
    var isFocused by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    fun sendWeight(tempWeight: Int?) {
        val weight = tempWeight
        if (weight != null) {
            Log.d("auto", "WeightInputField sendWeight has changed weight (input)")
            viewModel.onWeightChanged(weight, true)
        }
    }

    // Detect keyboard visibility to handle weight validation when the keyboard is closed.
    KeyboardVisibilityDetector { isOpen ->
        isFocused = isOpen
        if (!isOpen) {
            // Validate and update the temporary weight when the keyboard is closed.
            tempWeight = validateWeight(tempWeight, minWeight, maxWeight, previousWeight)
            previousWeight = tempWeight

            focusManager.clearFocus() // Clear focus when keyboard is closed
        }
    }

    // Layout for the weight input row, aligning items vertically in the center.
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly // Align items to the start
    ) {
        // Button to set the weight to the minimum allowed value.
        WeightButton(
            text = constant1,
            icon = Icons.Filled.FirstPage,
            contentDescription = constant1,
            onClick = {
                tempWeight = minWeight
                sendWeight(tempWeight)
            },
            modifier = Modifier.weight(1f)
        )

        // Input field for the actual weight.
        val focusRequester = remember { FocusRequester() }
        OutlinedTextField(
            value = if (isFocused) tempWeight?.toString() ?: "" else "${tempWeight ?: ""} g", // Display tempWeight or "0 g" when not focused. Show empty string when focused for easier input.
            onValueChange = {
                tempWeight = it.toIntOrNull()
                //sendWeight(tempWeight)
            }, // Update tempWeight when the input value changes.
            label = { Text(label, fontSize = 12.sp) }, // Label for the input field.
            textStyle = TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center), // Style for the input text.
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done), // Configure keyboard for number input and Done action.
            keyboardActions = KeyboardActions(onDone = {
                // Validate and update weight when the Done button on the keyboard is pressed.
                tempWeight = validateWeight(tempWeight, minWeight, maxWeight, previousWeight)
                tempWeight?.let {
                    sendWeight(tempWeight)
                    //viewModel.onWeightChanged(it, true)
                }
                keyboardController?.hide() // Hide the software keyboard.
            }),
            modifier = Modifier
                .weight(1f) // Take up the remaining width
                .height(56.dp) // Match the height of the buttons
                .padding(horizontal = 4.dp)
                .focusRequester(focusRequester) // Attach the FocusRequester to the input field.
                .onFocusChanged { focusState ->
                    // Reset to null (blank) when the input field gains focus.
                    tempWeight = if (focusState.isFocused) {
                        null // Set to null to blank the field
                    } else {
                        // Validate the weight when the input field loses focus.
                        validateWeight(tempWeight, minWeight, maxWeight, previousWeight)
                    }
                }
        )

        val context = LocalContext.current

        // Camera Icon Button
        Button(
            onClick = {
                val intent = Intent(context, NumberRecognitionActivity::class.java)
                numberRecognitionLauncher.launch(intent)
            },
            modifier = Modifier
                .height(56.dp) // Match the height of the other elements
                .weight(1f / 3f),
            contentPadding = PaddingValues(all = 0.dp), // Zero padding
            colors = ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp)
        ) {
            Icon(
                Icons.Filled.Camera,
                contentDescription = stringResource(R.string.open_the_camera),
                modifier = Modifier
                    .size(24.dp)
                    .weight(1f)
            )
        }

        // Button to set the weight to the maximum allowed value.
        WeightButton(
            text = constant2,
            icon = Icons.AutoMirrored.Filled.LastPage,
            contentDescription = constant2,
            onClick = {
                tempWeight = maxWeight
                sendWeight(tempWeight)
            },
            modifier = Modifier.weight(1f)
        )

    }
}

@Composable
fun WeightButton(
    text: String,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(40.dp),
        contentPadding = PaddingValues(all = 2.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 2.dp)
        )
    }
}
