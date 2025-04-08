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

import java.time.LocalDateTime

/**
 * Data class representing a Spool, which is a roll of filament used in 3D printing.
 *
 * @property id Unique identifier for the spool.
 * @property registered Timestamp when the spool was registered.
 * @property first_used Timestamp of the first use of the spool.
 * @property last_used Timestamp of the last use of the spool.
 * @property filament Filament details associated with the spool.
 * @property price Price of the spool.
 * @property remaining_weight Remaining weight of filament on the spool.
 * @property initial_weight Initial weight of filament on the spool.
 * @property spool_weight Weight of the empty spool.
 * @property used_weight Used weight of filament on the spool.
 * @property remaining_length Remaining length of filament on the spool.
 * @property used_length Used length of filament on the spool.
 * @property location Storage location of the spool.
 * @property lot_nr Lot number of the spool.
 * @property comment Free text comment about the spool.
 * @property archived Whether the spool is archived.
 * @property extra Extra properties for the spool.
 */
data class SpoolResponse(
    val id: Int,
    val registered: String,
    val first_used: String?,
    val last_used: String?,
    val filament: FilamentResponse?,
    val price: Int?,
    val remaining_weight: Double?,
    val initial_weight: Int?,
    val spool_weight: Int?,
    val used_weight: Double?,
    val remaining_length: Double?,
    val used_length: Double?,
    val location: String?,
    val lot_nr: String?,
    val comment: String?,
    val archived: Boolean,
    val extra: Map<String, String>?,
    val fetched: String? = LocalDateTime.now().toString()
)

/**
 * Data class representing the request body for creating or updating a spool.
 *
 * @property first_used Timestamp of the first use of the spool.
 * @property last_used Timestamp of the last use of the spool.
 * @property filament_id ID of the filament associated with the spool (required).
 * @property price Price of the spool.
 * @property initial_weight Initial weight of filament on the spool.
 * @property spool_weight Weight of the empty spool.
 * @property remaining_weight Remaining weight of filament on the spool.
 * @property used_weight Used weight of filament on the spool.
 * @property location Storage location of the spool.
 * @property lot_nr Lot number of the spool.
 * @property comment Free text comment about the spool.
 * @property archived Whether the spool is archived.
 * @property extra Extra properties for the spool.
 */
data class SpoolRequestBody(
    var first_used: String? = null,
    var last_used: String? = null,
    var filament_id: Int,
    var price: Double? = null,
    var initial_weight: Double? = null,
    var spool_weight: Double? = null,
    var remaining_weight: Double? = null,
    var used_weight: Double? = null,
    var location: String? = null,
    var lot_nr: String? = null,
    var comment: String? = null,
    var archived: Boolean? = false,
    var extra: Map<String, String>? = null
)

/**
 * Data class representing query parameters for searching spools.
 *
 * @property filamentName Filter spools by filament name.
 * @property filamentId Filter spools by filament ID.
 * @property filamentMaterial Filter spools by filament material.
 * @property filamentVendorName Filter spools by filament vendor name.
 * @property filamentVendorId Filter spools by filament vendor ID.
 * @property location Filter spools by location.
 * @property lotNr Filter spools by lot number.
 * @property allowArchived Filter spools by archived status.
 * @property sort Sorting criteria.
 * @property limit Maximum number of results to return.
 * @property offset Offset for pagination.
 */
data class SpoolSearchQueryParams(
    @ApiParameter("filament.name") val filamentName: String? = null,
    @ApiParameter("filament.id") val filamentId: String? = null,
    @ApiParameter("filament.material") val filamentMaterial: String? = null,
    @ApiParameter("filament.vendor.name") val filamentVendorName: String? = null,
    @ApiParameter("filament.vendor.id") val filamentVendorId: String? = null,
    val location: String? = null,
    @ApiParameter("lot_nr") val lotNr: String? = null,
    @ApiParameter("allow_archived") val allowArchived: Boolean? = false,
    val sort: String? = null,
    val limit: Int? = null,
    val offset: Int? = 0
)
