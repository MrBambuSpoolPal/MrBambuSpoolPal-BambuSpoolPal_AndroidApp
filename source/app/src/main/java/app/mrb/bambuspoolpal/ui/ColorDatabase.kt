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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.ScanViewModel
import app.mrb.bambuspoolpal.shared.FilamentDetail

/**
 * Composable function to display a dropdown menu for selecting a filament from the database.
 *
 * @param filament The FilamentDetail object containing possible match options.
 * @param viewModel The ScanViewModel instance.
 */
@Composable
fun CorrespondingDatabaseColorChooser(filament: FilamentDetail, viewModel: ScanViewModel) {
    val options = filament.possibleMatch?.map { (filamentData, score) ->
        Pair("${filamentData.material} ${filamentData.name} ($score %)", filamentData.id)
    } ?: emptyList()

    val defaultSelectedOption = filament.colorName ?: stringResource(R.string.unknown_color)

    var selectedOption by remember { mutableStateOf(options.firstOrNull()?.first ?: defaultSelectedOption) }
    var selectedOptionId by remember { mutableStateOf(options.firstOrNull()?.second) }
    var expanded by remember { mutableStateOf(false) }

    // New filament scan complete + no database id defined and options available
    // this is used to set the filament database id to the first one available in options
    LaunchedEffect(filament, filament.defaultFilamentDatabaseId, options) {
        if (filament.defaultFilamentDatabaseId && options.isNotEmpty()) {
            val firstOptionId = options.firstOrNull()?.second
            if (firstOptionId != null) {
                selectedOptionId = firstOptionId
                viewModel.onDatabaseItemSelection(firstOptionId)
            }
        }
    }

    // When filamentDatabaseId is set (ie check was hit), the selection dropdown needs update
    LaunchedEffect(filament, filament.filamentDatabaseId, options) {
        filament.filamentDatabaseId?.let { databaseId ->
            selectedOptionId = databaseId

            val matchingOption = options.find { (_, id) -> id == databaseId }
            selectedOption = matchingOption?.first ?: options.firstOrNull()?.first ?: defaultSelectedOption
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {

        OutlinedTextField(
            value = selectedOption,
            onValueChange = {
                val localSelectedOptionId = selectedOptionId
                if (localSelectedOptionId != null) {
                    viewModel.onDatabaseItemSelection(localSelectedOptionId)
                }
            },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            label = { Text(stringResource(R.string.choose_database_item)) },
            trailingIcon = {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = stringResource(R.string.show_options)
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (displayOption, id) ->
                DropdownMenuItem(
                    text = { Text(displayOption, style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        selectedOption = displayOption
                        selectedOptionId = id
                        expanded = false
                        viewModel.onDatabaseItemSelection(id)
                    }
                )
            }
        }
    }
}
