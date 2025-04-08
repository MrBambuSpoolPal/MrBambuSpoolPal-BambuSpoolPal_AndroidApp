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

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncDisabled
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.preferences.Constants
import app.mrb.bambuspoolpal.ui.CompactRow
import app.mrb.bambuspoolpal.ui.Section
import app.mrb.bambuspoolpal.ui.StyledBox
import app.mrb.bambuspoolpal.ui.TitleBar

data class Language(val code: String, val flagResId: Int, val name: String)

@SuppressLint("InflateParams")
@Composable
fun ConfigScreen(viewModel: ConfigViewModel, navController: NavController) {

    val language: String? by viewModel.language.observeAsState()
    val languageString = remember { language ?: viewModel.getLanguage() } // Utilise getLanguage() comme fallback

    // --- Spoolman Database URL ---
    val spoolmanDatabaseUrl by viewModel.configStateFlows[Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM]!!.collectAsState()
    val spoolmanDatabaseUrlString = spoolmanDatabaseUrl.toString() // Explicit conversion to String

    // --- Spoolman Database Bambu Manufacturer ---
    val spoolmanDatabaseBambuManufacturer by viewModel.configStateFlows[Constants.MANUFACTURER_BAMBU_LAB_PARAM]!!.collectAsState()
    val spoolmanDatabaseBambuManufacturerString = spoolmanDatabaseBambuManufacturer.toString()

    // --- Proxy URL ---
    val proxyUrl by viewModel.configStateFlows[Constants.SPOOLMAN_PROXY_BASE_URL_PARAM]!!.collectAsState()
    val proxyUrlString = proxyUrl.toString()

    // --- Spoolman API Base URL ---
    val spoolmanApiBaseUrl by viewModel.configStateFlows[Constants.SPOOLMAN_BASE_API_URL_PARAM]!!.collectAsState()
    val spoolmanApiBaseUrlString = spoolmanApiBaseUrl.toString()

    // --- Spoolman API Self Signed ---
    val spoolmanApiSelfSigned by viewModel.configStateFlows[Constants.SPOOLMAN_API_SELF_SIGNED_PARAM]!!.collectAsState()
    val localApiSelfSigned = spoolmanApiSelfSigned

    val spoolmanApiAutoTriggering by viewModel.configStateFlows[Constants.SPOOLMAN_API_AUTO_TRIGGERING_PARAM]!!.collectAsState()
    val localApiAutoTriggering = spoolmanApiAutoTriggering

    val spoolShowUid by viewModel.configStateFlows[Constants.SHOW_SPOOL_UID_PARAM]!!.collectAsState()
    val localSpoolShowUid = spoolShowUid

    val spoolmanApiSelfSignedBoolean = when (localApiSelfSigned) {
        is Boolean -> localApiSelfSigned
        is String -> {
            try {
                localApiSelfSigned.toBoolean()
            } catch (e: IllegalArgumentException) {
                Log.e("ConfigScreen", "Error converting String to Boolean: ${e.message}")
                false // Default value in case of error
            }
        }
        else -> false
    }

    val spoolmanApiAutoTriggeringBoolean = when (localApiAutoTriggering) {
        is Boolean -> localApiAutoTriggering
        is String -> {
            try {
                localApiAutoTriggering.toBoolean()
            } catch (e: IllegalArgumentException) {
                Log.e("ConfigScreen", "Error converting String to Boolean: ${e.message}")
                false // Default value in case of error
            }
        }
        else -> false
    }

    val spoolShowUidBoolean = when (localSpoolShowUid) {
        is Boolean -> localSpoolShowUid
        is String -> {
            try {
                localSpoolShowUid.toBoolean()
            } catch (e: IllegalArgumentException) {
                Log.e("ConfigScreen", "Error converting String to Boolean: ${e.message}")
                false // Default value in case of error
            }
        }
        else -> false
    }

    val isSending by viewModel.isSending.observeAsState(false)

    // Collecting the count of items in the filament database for display
    val filamentDataCount by viewModel.filamentDataCount.observeAsState(0)

    val languages = listOf(
        Language("en", R.drawable.flag_us, stringResource(R.string.language_english)),
        Language("fr", R.drawable.flag_fr, stringResource(R.string.language_french)),
        Language("es", R.drawable.flag_es, stringResource(R.string.language_spanish))
    )

    // Main UI layout for the settings screen
    Column(
        modifier = Modifier
            .fillMaxSize()  // Fill the entire screen
            .padding(8.dp)  // Apply padding around the content
            .verticalScroll(rememberScrollState()),  // Enable vertical scrolling
        verticalArrangement = Arrangement.spacedBy(6.dp)  // Add spacing between sections
    ) {
        // Title bar of the screen
        TitleBar(title = stringResource(R.string.bambu_spoolman_pal_settings))

        Section(title = stringResource(R.string.language)) {

            val selectedLanguage = remember {
                mutableStateOf(languages.find { it.code == languageString } ?: languages[0])
            }

            CompactRow {

                LanguageSelector(
                    languages = languages,
                    selectedLanguage = selectedLanguage.value,
                    onLanguageChange = { newLanguage: Language ->
                        selectedLanguage.value = newLanguage
                        viewModel.setLanguage(newLanguage.code)
                    }
                )
            }
        }

        Section(title = stringResource(R.string.spool_rfid_tag)) {

            CompactRow {

                Switch(
                    checked = spoolShowUidBoolean,
                    onCheckedChange = { viewModel.updateConfig(Constants.SHOW_SPOOL_UID_PARAM, it) },
                    thumbContent = {
                        if (spoolShowUidBoolean) {
                            // Icon isn't focusable, no need for content description
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )

                        }
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.show_spool_uid))

            }
        }

        // Section for Spoolman Database configuration
        Section(title = stringResource(R.string.spoolman_database)) {

            // Nested column to display text fields for Spoolman database URL and Bambu manufacturer
            Column(modifier = Modifier.fillMaxWidth()) {

                ConfigTextField(
                    label = stringResource(R.string.full_url_of_spoolman_filament_database),  // Label for the URL text field
                    value = spoolmanDatabaseUrlString,  // Value to be displayed
                    modifier = Modifier.fillMaxWidth(),  // Fill available width
                    onValueChange = { viewModel.updateConfig(Constants.SPOOLMAN_DB_FILAMENTS_URL_PARAM, it) },  // Update the value in the ViewModel
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
                )
                ConfigTextField(
                    label = stringResource(R.string.bambu_manufacturer_manufacturer_value_in_the_database),  // Label for the manufacturer text field
                    value = spoolmanDatabaseBambuManufacturerString,  // Current value
                    modifier = Modifier.fillMaxWidth(),  // Fill available width
                    onValueChange = { viewModel.updateConfig(Constants.MANUFACTURER_BAMBU_LAB_PARAM, it) },  // Update the value in ViewModel
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Display the count of items in the filament database
                StyledBox {
                    Text(
                        stringResource(
                            R.string.count_of_currently_known_filaments,
                            filamentDataCount
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            // Button to fetch and filter filament data
            FetchFilamentDataButton(isSending = isSending, onClick = { viewModel.fetchAndFilterFilamentData(viewModel) })
        }

        // Section for Spoolman API configuration
        Section(title = stringResource(R.string.spoolman_api)) {
            // First row with one input field for API base URL
            CompactRow {
                ConfigTextField(
                    label = stringResource(R.string.spoolman_base_url_https_required),
                    value = spoolmanApiBaseUrlString,
                    modifier = Modifier.weight(1f),  // Take equal space in the row
                    onValueChange = { viewModel.updateConfig(Constants.SPOOLMAN_BASE_API_URL_PARAM, it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
                )
            }

            // Checkbox row
            CompactRow {

                Switch(
                    checked = spoolmanApiSelfSignedBoolean,
                    onCheckedChange = { viewModel.updateConfig(Constants.SPOOLMAN_API_SELF_SIGNED_PARAM, it) },
                    thumbContent = {
                        if (spoolmanApiSelfSignedBoolean) {
                            // Icon isn't focusable, no need for content description
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Block    ,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.accept_self_signed_certificates))

            }

            CompactRow {

                Switch(
                    checked = spoolmanApiAutoTriggeringBoolean,
                    onCheckedChange = { viewModel.updateConfig(Constants.SPOOLMAN_API_AUTO_TRIGGERING_PARAM, it) },
                    thumbContent = {
                        if (spoolmanApiAutoTriggeringBoolean) {
                            // Icon isn't focusable, no need for content description
                            Icon(
                                imageVector = Icons.Filled.Sync,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Filled.SyncDisabled,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize),
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(4.dp))
                Text(text = stringResource(R.string.auto_trigger_spoolman_changes))

            }

        }

        // Section for Proxy configuration
        Section(title = stringResource(R.string.bambu_spool_pal_backend_optional)) {
            // Display proxy URL and endpoint text fields in a column
            Column(modifier = Modifier.fillMaxWidth()) {
                ConfigTextField(
                    label = stringResource(R.string.backend_api_url),  // Label for the proxy URL field
                    value = proxyUrlString,  // Current value for the proxy URL
                    modifier = Modifier.fillMaxWidth(),
                    onValueChange = { viewModel.updateConfig(Constants.SPOOLMAN_PROXY_BASE_URL_PARAM, it) },  // Update the value in ViewModel
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done)
                )
            }
        }

        // Row containing Save and Cancel buttons
        CompactRow {

            // Cancel button: Resets configuration and navigates back to the previous screen
            TooltipButton(
                onClick = {
                    viewModel.resetConfig()  // Reset the configuration
                    navController.popBackStack()  // Navigate back to the previous screen
                },
                modifier = Modifier.weight(1f) , // Distribute space equally
                text = stringResource(R.string.cancel),
                icon = Icons.Filled.Cancel
            )

            // Save button: Saves the current configuration and navigates back to the previous screen
            TooltipButton(
                onClick = {
                    viewModel.saveConfig()  // Save the configuration
                    navController.popBackStack()  // Navigate back to the previous screen
                },
                modifier = Modifier.weight(1f),  // Distribute space equally
                text = stringResource(R.string.save),
                icon = Icons.Filled.Save
            )


        }
    }

}

/**
 * Button to fetch filament data from Bambu Lab's filament database.
 *
 * @param isSending Indicates if the sending process is in progress.
 * @param onClick Callback function when the button is clicked.
 */
@Composable
fun FetchFilamentDataButton(isSending: Boolean, onClick: () -> Unit) {
    TooltipButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),  // Make the button take the full width
        text = stringResource(R.string.fetch_spoolman_database),
        running = isSending,
        icon = Icons.Filled.Search
    )
}

/**
 * Configuration text field with a label and input field.
 *
 * @param label The label to be displayed for the text field.
 * @param value The current value of the text field.
 */
@Composable
fun ConfigTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardOptions:KeyboardOptions = KeyboardOptions()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) }, // Reduce label size
        textStyle = TextStyle(fontSize = 14.sp), // Set input text size
        modifier = modifier
            .fillMaxWidth()  // Fill available width
            .padding(vertical = 2.dp),  // Apply small vertical padding,
        keyboardOptions = keyboardOptions
    )
}

@Composable
fun LanguageSelector(languages: List<Language>, selectedLanguage: Language, onLanguageChange: (Language) -> Unit) {

val expanded = remember { mutableStateOf(false) }

    Text(stringResource(R.string.select_language), style = MaterialTheme.typography.bodyLarge)

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded.value = !expanded.value }) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = selectedLanguage.flagResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(selectedLanguage.name, style = MaterialTheme.typography.bodyMedium)
            }
        }

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            languages.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = language.flagResId),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(language.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    },
                    onClick = {
                        onLanguageChange(language)
                        expanded.value = false
                    }
                )
            }
        }
    }
}
