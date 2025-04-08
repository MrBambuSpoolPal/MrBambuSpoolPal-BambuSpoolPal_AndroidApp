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

import app.mrb.bambuspoolpal.preferences.Constants
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.pow

/**
 * Data class representing the details of a filament including information like color,
 * weight, filament type, and more.
 */
data class FilamentDetail(
    var uid: String? = "unknown", // Unique identifier of the spool, default message if not scanned.
    var trayUID: String? = "unknown",
    val vendorName: String? = Constants.MANUFACTURER_BAMBU_LAB, // Name of the vendor, default is Bambu Lab.
    val vendorId: Int? = null, // Vendor ID, if available.
    val detailedFilamentType: String? = "unknown", // Detailed filament type, default is "unknown".
    val filamentType: String? = "unknown", // General filament type, default is "unknown".
    val colorBytes: ByteArray? = null, // Raw color bytes from the tag.
    val colorHexString: String? = "unknown", // Color in hexadecimal string format.
    val colorName: String? = null, // Name of the color.
    val colorNameDetail: String? = null, // Details about how the color name was determined.
    val spoolWeight: Int = Constants.DEFAULT_SPOOL_WEIGHT, // Total weight of the spool with filament.
    val actualWeight: Int? = 0, // Current actual weight of the spool.
    val inputWeight: LocalDateTime? = null,
    val emptyWeight: Int? = Constants.DEFAULT_EMPTY_SPOOL_WEIGHT, // Weight of the empty spool.
    val filamentDiameter: Float? = 0.0F, // Diameter of the filament in millimeters.
    val filamentLength: Int? = 0, // Total length of the filament on the spool.
    val productionDatetime: LocalDateTime? = null, // Date and time when the filament was produced.
    val possibleMatch: List<Pair<FilamentData, Int>>? = null, // List of possible matches from the database.
    val filamentDatabaseId: String? = null, // ID of the filament in the database.
    val defaultFilamentDatabaseId: Boolean = true,
    val filamentDensity: Double? = null, // Density of the filament material.
    val filamentId: Int? = null, // ID of the filament
    var spoolId: Int? = null
) {
    /**
     * Calculates the used weight of the filament.
     */
    val usedWeight: Int
        get() {
            return actualWeight?.let { weight ->
                spoolWeight.let { spool ->
                    emptyWeight?.let { empty ->
                        spool + empty - weight
                    }
                }
            } ?: 0
        }

    /**
     * Calculates the percentage of filament remaining on the spool.
     */
    val percentRemaining: Int
        get() {
            return emptyWeight?.let { empty ->
                spoolWeight.let { spool ->
                    actualWeight?.let { actual ->
                        ((actual - empty) * 100) / spool
                    }
                }
            } ?: 100
        }

    /**
     * Calculates the remaining length of the filament on the spool.
     */
    val remainingFilamentLength: Int?
        get() {
            return filamentLength?.let { length ->
                percentRemaining * length / 100
            }
        }

    /**
     * Calculates the computed filament density based on the filament length and weight.
     */
    val computedFilamentDensity: Double?
        get() {
            return filamentLength?.let { length ->
                spoolWeight.let {
                    filamentDiameter?.let { diameter ->
                        spoolWeight / (length * 100 * PI * ((diameter / 10 / 2)).pow(2))
                    }
                }
            }
        }

    /**
     * Generates a default filament ID based on available properties.
     */
    val defaultFilamentId: String

        // TODO: vendorName n'est pas valorisé
        get() {
            val parts = listOfNotNull(
                vendorName,
                detailedFilamentType,
                colorName,
                spoolWeight.toString(),
                filamentDiameter?.toString()
            )

            // Function to clean a string by removing non-alphanumeric characters.
            fun cleanString(input: String?): String {
                return input?.replace(Regex("[^a-zA-Z0-9]"), "") ?: ""
            }

            // Clean each part of the string.
            val cleanedParts = parts.map { cleanString(it) }

            return cleanedParts.filter { it.isNotEmpty() }.joinToString("_").lowercase()
        }
}
