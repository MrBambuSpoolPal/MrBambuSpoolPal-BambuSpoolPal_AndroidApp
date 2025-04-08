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
 * Data class representing extra properties for a Vendor.
 *
 * @property property1 An optional property.
 * @property property2 An optional property.
 */
data class VendorExtra(
    val property1: String?,
    val property2: String?
)

/**
 * Data class representing a Vendor, which is a supplier or manufacturer of filaments.
 *
 * @property id Unique identifier for the vendor.
 * @property registered Timestamp when the vendor was registered.
 * @property name Name of the vendor.
 * @property comment Free text comment about the vendor.
 * @property empty_spool_weight Weight of an empty spool from this vendor.
 * @property external_id External identifier of the vendor.
 * @property extra Extra properties for the vendor.
 */
data class Vendor(
    val id: Int,
    val registered: String,
    val name: String,
    val comment: String?,
    val empty_spool_weight: Int?,
    val external_id: String?,
    val extra: VendorExtra?
)

/**
 * Data class representing the request body for creating or updating a vendor.
 *
 * @property name Name of the vendor.
 * @property comment Free text comment about the vendor.
 * @property empty_spool_weight Weight of an empty spool from this vendor.
 * @property external_id External identifier of the vendor.
 * @property extra Extra properties for the vendor.
 */
data class VendorRequestBody(
    val name: String,
    val comment: String?,
    val empty_spool_weight: Double?,
    val external_id: String?,
    val extra: Map<String, Any>?
)
