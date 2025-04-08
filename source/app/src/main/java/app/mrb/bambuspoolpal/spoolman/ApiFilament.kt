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

package app.mrb.bambuspoolpal.spoolman

/**
 * Data class representing a Filament, the material used for 3D printing.
 *
 * @property id Unique identifier for the filament.
 * @property registered Timestamp when the filament was registered.
 * @property name Name of the filament.
 * @property vendor Vendor of the filament.
 * @property material Material type of the filament (e.g., PLA, ABS).
 * @property price Price of the filament.
 * @property density Density of the filament.
 * @property diameter Diameter of the filament.
 * @property weight Weight of the filament.
 * @property spool_weight Weight of the empty spool.
 * @property article_number Article number of the filament.
 * @property comment Free text comment about the filament.
 * @property settings_extruder_temp Extruder temperature settings.
 * @property settings_bed_temp Bed temperature settings.
 * @property color_hex Hexadecimal color code of the filament.
 * @property multi_color_hexes Comma-separated list of hexadecimal color codes for multi-color filaments.
 * @property multi_color_direction Direction of the multi-color filament.
 * @property external_id External identifier of the filament.
 * @property extra Extra properties for the filament.
 */
data class FilamentResponse(
    val id: Int,
    val registered: String,
    val name: String?,
    val vendor: Vendor?,
    val material: String?,
    val price: Int?,
    val density: Double,
    val diameter: Double,
    val weight: Int?,
    val spool_weight: Int?,
    val article_number: String?,
    val comment: String?,
    val settings_extruder_temp: Int?,
    val settings_bed_temp: Int?,
    val color_hex: String?,
    val multi_color_hexes: String?,
    val multi_color_direction: String?,
    val external_id: String?,
    val extra: Map<String, String>?
)

/**
 * Data class representing query parameters for fetching filaments.
 *
 * @property vendor_name Filter filaments by vendor name.
 * @property vendor_id Filter filaments by vendor ID.
 * @property name Filter filaments by name.
 * @property material Filter filaments by material.
 * @property article_number Filter filaments by article number.
 * @property color_hex Filter filaments by color hexadecimal code.
 * @property color_similarity_threshold Threshold for color similarity search.
 * @property external_id Filter filaments by external ID.
 * @property sort Sorting criteria.
 * @property limit Maximum number of results to return.
 * @property offset Offset for pagination.
 */
data class FilamentQueryParams(
    val vendor_name: String? = null,
    val vendor_id: String? = null,
    val name: String? = null,
    val material: String? = null,
    val article_number: String? = null,
    val color_hex: String? = null,
    val color_similarity_threshold: Double? = null,
    val external_id: String? = null,
    val sort: String? = null,
    val limit: Int? = null,
    val offset: Int? = null
)

/**
 * Data class representing the request body for creating or updating a filament.
 *
 * @property name Name of the filament.
 * @property vendor_id ID of the vendor.
 * @property material Material type of the filament.
 * @property price Price of the filament.
 * @property density Density of the filament.
 * @property diameter Diameter of the filament.
 * @property weight Weight of the filament.
 * @property spool_weight Weight of the empty spool.
 * @property article_number Article number of the filament.
 * @property comment Free text comment about the filament.
 * @property settings_extruder_temp Extruder temperature settings.
 * @property settings_bed_temp Bed temperature settings.
 * @property color_hex Hexadecimal color code of the filament.
 * @property multi_color_hexes Comma-separated list of hexadecimal color codes for multi-color filaments.
 * @property multi_color_direction Direction of the multi-color filament.
 * @property external_id External identifier of the filament.
 * @property extra Extra properties for the filament.
 */
data class FilamentRequestBody(
    val name: String?,
    val vendor_id: Int?,
    val material: String?,
    val price: Double?,
    val density: Double,
    val diameter: Double,
    val weight: Double?,
    val spool_weight: Double?,
    val article_number: String?,
    val comment: String?,
    val settings_extruder_temp: Int?,
    val settings_bed_temp: Int?,
    val color_hex: String?,
    val multi_color_hexes: String?,
    val multi_color_direction: String?,
    val external_id: String?,
    val extra: Map<String, Any>?
)

/**
 * Data class representing the request body for updating filament usage.
 *
 * @property use_length Length of filament to reduce by, in mm.
 * @property use_weight Filament weight to reduce by, in g.
 */
data class UseFilamentRequest(
    val use_length: Double? = null,
    val use_weight: Double? = null
)
