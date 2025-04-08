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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.mrb.bambuspoolpal.utils.ColorUtils


/**
 * A composable to display the color of the filament based on the byte data provided.
 *
 * @param colorBytes The byte array that represents the color.
 */
@Composable
fun FilamentColorBox(colorBytes: ByteArray?) {
    val color = colorBytes?.let { ColorUtils.byteArrayToColor(it) }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(color ?: Color.Transparent, shape = RoundedCornerShape(8.dp))
            .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
    ) {
        if (color == null) {
            // Draw a dashed pattern inside the box when color is null.
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeWidth = 3.dp.toPx()
                val spaceBetween = 6.dp.toPx()

                // Draw diagonal dashes inside the box.
                for (i in 0..(size.width / spaceBetween).toInt()) {
                    val offset = i * spaceBetween

                    // Draw dashes from top-left to bottom-right.
                    drawLine(
                        color = Color.Gray,
                        start = Offset(offset, 0f),
                        end = Offset(size.width, size.width - offset),
                        strokeWidth = strokeWidth
                    )
                    drawLine(
                        color = Color.Gray,
                        start = Offset(0f, offset),
                        end = Offset(size.width - offset, size.height),
                        strokeWidth = strokeWidth
                    )
                }
            }
        }
    }
}
