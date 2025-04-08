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

package app.mrb.bambuspoolpal.bambu

import app.mrb.bambuspoolpal.nfc.NfcTagData
import app.mrb.bambuspoolpal.preferences.Constants
import app.mrb.bambuspoolpal.shared.FilamentData
import app.mrb.bambuspoolpal.shared.FilamentDetail
import app.mrb.bambuspoolpal.spoolman.FilamentDatabase
import app.mrb.bambuspoolpal.utils.ColorUtils

/**
 * Object responsible for decoding and parsing NFC tag data into FilamentDetails.
 */
object TagDecoder {

    /**
     * Parses the details from the NFC tag data and matches it with available filament data.
     *
     * @param data The NFC tag data containing the byte information from the tag.
     * @param filamentData The list of available filament data to match against.
     * @return A FilamentDetail object containing the parsed details.
     * @throws IllegalArgumentException If the tag data is insufficient for processing.
     */
    fun parseTagDetails(data: NfcTagData, filamentData: List<FilamentData>): FilamentDetail {
        // Ensure there is enough data in the tag bytes to extract the necessary details.
        if (data.bytes.size < 80) {
            throw IllegalArgumentException("Insufficient data in tag dump") // Error if data is insufficient.
        }

        val trayUid = BambuTagDecodeHelpers.hexstring(data.bytes, 9, 0, 16)

        // Extract the color bytes, material type, and color hex from the tag data.
        val colorBytes = BambuTagDecodeHelpers.bytes(data.bytes, 5, 0, 4)
        val material = BambuTagDecodeHelpers.string(data.bytes, 2, 0, 16)
        val colorHex = BambuTagDecodeHelpers.hexstring(data.bytes, 5, 0, 4)
        val detailedFilamentType = BambuTagDecodeHelpers.string(data.bytes, 4, 0, 16)

        // Extract the first 6 characters of the color hex to use as the RGB value.
        val rgb = colorHex.take(6)

        // Filter filament data based on the material type and the matching RGB color.
        val filamentDataItems = filamentData.filter {
            it.color_hex == rgb
        }

        // List<Pair<FilamentData, Int>> item and score
        val possibleMatch =
            FilamentDatabase.sortByTokenSimilarity(detailedFilamentType, filamentDataItems)

        val genericColorName = ColorUtils.getColorName(colorBytes)

        // Provide a detailed description of how the color name was determined.
        val colorNameDetails = if (filamentDataItems.isNotEmpty())
            "The color name is from the database"
        else
            "The color name is approximate"

        // Create a FilamentDetail object with the parsed data.
        val filamentDetail = FilamentDetail(
            uid = data.uid,
            trayUID = trayUid,
            filamentType = material,
            detailedFilamentType = detailedFilamentType,
            colorBytes = colorBytes,
            colorName = possibleMatch.firstOrNull()?.first?.name ?: genericColorName,
            possibleMatch = possibleMatch,
            colorHexString = colorHex,
            colorNameDetail = colorNameDetails,
            spoolWeight = BambuTagDecodeHelpers.int(data.bytes, 5, 4),
            filamentDiameter = BambuTagDecodeHelpers.float(data.bytes, 5, 8, 4),
            filamentLength = BambuTagDecodeHelpers.int(data.bytes, 14, 4),
            productionDatetime = BambuTagDecodeHelpers.datetime(data.bytes, 12, 0),
            actualWeight = null,
            filamentDatabaseId = null, // Initialize as null
            emptyWeight = Constants.DEFAULT_EMPTY_SPOOL_WEIGHT,
            vendorName = Constants.MANUFACTURER_BAMBU_LAB,
            vendorId = null,
            filamentDensity = null,
            filamentId = null, // Initialize as null
            defaultFilamentDatabaseId = true
        )

        // Determine the filament ID to use. If filamentDatabaseId is null, use defaultFilamentId.
        val filamentIdToUse = filamentDetail.filamentDatabaseId ?: filamentDetail.defaultFilamentId

        // Create a new FilamentDetail object with the determined filamentId.
        return filamentDetail.copy(filamentDatabaseId = filamentIdToUse, defaultFilamentDatabaseId = true)
    }
}