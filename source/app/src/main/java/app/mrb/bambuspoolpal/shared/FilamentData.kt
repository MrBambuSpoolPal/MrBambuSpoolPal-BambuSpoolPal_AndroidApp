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

// Simplified data class for filtered filament records.
data class FilamentData(
    val id: String,           // Unique identifier for the filament
    val name: String,         // Filament name
    val material: String,     // Material type
    val empty_spool_weight: Int,    // Weight of the empty spool (grams)
    val color_hex: String?,   // HEX color code (optional)
    val translucent: Boolean, // Flag indicating if the filament is translucent
    val glow: Boolean,        // Flag indicating if the filament is glow-in-the-dark
    val density: Double
)
