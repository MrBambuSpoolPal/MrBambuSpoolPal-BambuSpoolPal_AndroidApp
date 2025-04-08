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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.mrb.bambuspoolpal.BuildConfig
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.ScanViewModel
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.shared.FilamentDetail
import kotlin.random.Random


/**
 * Display the details of the filament.
 *
 * @param filament The filament details to display.
 * @param viewModel The ViewModel to handle color changes.
 */
@Composable
fun FilamentDetails(filament: FilamentDetail, viewModel: ScanViewModel, configViewModel: ConfigViewModel) {
    Section(title = stringResource(R.string.rfid_spool_details)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Styled box displaying UID, filament type, and production date.
            StyledBox {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (configViewModel.showSpoolUid()) {
                        Text(
                            stringResource(
                                R.string.uid,
                                filament.trayUID ?: stringResource(R.string.n_a)
                            ), style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(
                        stringResource(R.string.type, filament.detailedFilamentType?: stringResource(R.string.n_a)),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    // DEBUG mode only, for demonstration purposes
                    if (BuildConfig.IS_DEBUG) {
                        val prodTime = filament.productionDatetime?.minusDays(
                            Random.nextInt(1, 10000).toLong()
                        )?.plusMinutes(Random.nextInt(100, 10000).toLong())

                        DisplayLocalizedDateTime(prodTime)
                    } else {
                        DisplayLocalizedDateTime(filament.productionDatetime)
                    }
                }
            }
            // Styled box displaying filament color and hex value.
            StyledBox {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MainRow {
                        FilamentColorBox(filament.colorBytes)
                        Text(stringResource(R.string.hex_value, filament.colorHexString ?: stringResource(R.string.n_a)), style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Color chooser for filament based on database color.
            CorrespondingDatabaseColorChooser(filament = filament, viewModel = viewModel)

            // Styled box displaying filament weight, spool weight, diameter, length, and database ID.
            StyledBox {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.filament_weight_g, filament.spoolWeight),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            stringResource(R.string.spool_weight_g, filament.emptyWeight ?: "N/A"),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.diameter_mm, filament.filamentDiameter?: stringResource(R.string.n_a)), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.length_m, filament.filamentLength?: stringResource(R.string.n_a)), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    Text(
                        stringResource(R.string.db_id, filament.filamentDatabaseId ?: stringResource(R.string.n_a)),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        stringResource(
                            R.string.manufacturer_id_filament_id_spool_id,
                            filament.vendorId ?: stringResource(R.string.n_a),
                            filament.filamentId ?: stringResource(R.string.n_a),
                            filament.spoolId ?: stringResource(R.string.n_a)
                        ),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
