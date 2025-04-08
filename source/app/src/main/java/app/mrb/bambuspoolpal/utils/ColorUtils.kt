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

package app.mrb.bambuspoolpal.utils

import android.util.Log
import androidx.compose.ui.graphics.Color
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This object contains utility functions for color handling,
 * such as converting byte arrays to colors and determining the closest
 * color name from predefined color values.
 */
object ColorUtils {

    /**
     * Converts a byte array representing a color (RGB or RGBA) to a [Color] object.
     * If the byte array size is not valid, it defaults to black.
     *
     * @param byteArray A byte array representing a color in RGB or RGBA format.
     * @return A [Color] object representing the color.
     */
    fun byteArrayToColor(byteArray: ByteArray): Color {
        return if (byteArray.size >= 3) { // Ensure at least RGB values are present
            val r = byteArray[0].toUByte().toInt()
            val g = byteArray[1].toUByte().toInt()
            val b = byteArray[2].toUByte().toInt()
            val a = if (byteArray.size > 3) byteArray[3].toUByte().toInt() else 255 // Default alpha to 255
            Color(r, g, b, a)
        } else {
            Color.Black // Fallback color if the byte array size is incorrect
        }
    }

    /**
     * Attempts to get the name of a color based on the RGB bytes provided.
     * If the byte array is not a valid RGB or RGBA representation, it returns "Invalid color string".
     * If an error occurs during processing, it returns "Unknown".
     *
     * @param colorBytes A byte array representing a color in RGB or RGBA format.
     * @return A string representing the color name.
     */
    fun getColorName(colorBytes: ByteArray): String {
        if (colorBytes.size !in 3..4) {
            Log.d("getColorName", "${colorBytes.size} is not correct")
            return "Invalid color string"
        }

        try {
            val hex = StringBuilder("#")
            for (byte in colorBytes) {
                // Convert each byte to a two-digit hexadecimal string
                hex.append(String.format("%02X", byte))
            }

            return getColorNameSimple(colorBytes)

        } catch (e: Exception) {
            Log.d("getColorName", "Error: ${e.message}")
            return "Unknown"
        }
    }

    /**
     * This function matches the provided RGB bytes to predefined color names.
     * It calculates the Euclidean distance between the given color and each
     * predefined color to find the closest match.
     *
     * @param colorBytes A byte array representing a color in RGB format.
     * @return A string representing the closest color name.
     */
    private fun getColorNameSimple(colorBytes: ByteArray): String {
        // Extract RGB components from the color byte array
        val red = colorBytes[0].toInt() and 0xFF
        val green = colorBytes[1].toInt() and 0xFF
        val blue = colorBytes[2].toInt() and 0xFF

        // Map of predefined colors and their RGB values
        val predefinedColors = mapOf(
            "Jade White" to android.graphics.Color.rgb(255, 255, 255),
            "Beige" to android.graphics.Color.rgb(247, 230, 222),
            "Gold" to android.graphics.Color.rgb(228, 189, 104),
            "Silver" to android.graphics.Color.rgb(166, 169, 170),
            "Gray" to android.graphics.Color.rgb(142, 144, 137),
            "Bronze" to android.graphics.Color.rgb(132, 125, 72),
            "Brown" to android.graphics.Color.rgb(157, 67, 44),
            "Red" to android.graphics.Color.rgb(193, 46, 31),
            "Magenta" to android.graphics.Color.rgb(236, 0, 140),
            "Pink" to android.graphics.Color.rgb(245, 90, 116),
            "Orange" to android.graphics.Color.rgb(255, 106, 19),
            "Yellow" to android.graphics.Color.rgb(244, 238, 42),
            "Bambu Green" to android.graphics.Color.rgb(0, 174, 66),
            "Mistletoe Green" to android.graphics.Color.rgb(63, 142, 67),
            "Cyan" to android.graphics.Color.rgb(0, 134, 214),
            "Blue" to android.graphics.Color.rgb(10, 41, 137),
            "Purple" to android.graphics.Color.rgb(94, 67, 183),
            "Blue Gray" to android.graphics.Color.rgb(91, 101, 121),
            "Light Gray" to android.graphics.Color.rgb(209, 211, 213),
            "Dark Gray" to android.graphics.Color.rgb(84, 84, 84),
            "Black" to android.graphics.Color.rgb(0, 0, 0)
        )

        // Find the predefined color with the smallest Euclidean distance to the input color
        return predefinedColors.minByOrNull { (_, predefinedColor) ->
            val predefinedRed = android.graphics.Color.red(predefinedColor)
            val predefinedGreen = android.graphics.Color.green(predefinedColor)
            val predefinedBlue = android.graphics.Color.blue(predefinedColor)

            // Calculate Euclidean distance in RGB color space
            sqrt(
                (predefinedRed - red).toDouble().pow(2) +
                        (predefinedGreen - green).toDouble().pow(2) +
                        (predefinedBlue - blue).toDouble().pow(2)
            )
        }?.key ?: "Unknown" // Return the name of the closest color, or "Unknown" if no match is found
    }
}
