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

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow

/**
 * This object contains helper functions for decoding various data types from byte arrays.
 * It supports extracting bytes, strings, integers, floats, and dates, as well as formatting them.
 */
object BambuTagDecodeHelpers {

    /**
     * Extracts a sub-array of bytes from the provided data using a block number, offset, and length.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to extract.
     * @return The extracted byte array.
     */
    fun bytes(data: ByteArray, blockNumber: Int, offset: Int, len: Int): ByteArray {
        return data.copyOfRange(blockNumber * 16 + offset, blockNumber * 16 + offset + len)
    }

    /**
     * Converts a specific section of bytes from the data into a hexadecimal string.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to convert.
     * @return The hexadecimal string representation of the byte array.
     */
    fun hexstring(data: ByteArray, blockNumber: Int, offset: Int, len: Int): String {
        return bytes(data, blockNumber, offset, len).joinToString("") { String.format("%02X", it) }
    }

    /**
     * Decodes a specific section of bytes into a string, interpreting the bytes as UTF-8 and removing any null characters.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to decode.
     * @return The decoded string.
     */
    fun string(data: ByteArray, blockNumber: Int, offset: Int, len: Int): String {
        return bytes(data, blockNumber, offset, len).toString(Charsets.UTF_8).replace("\u0000", "")
    }

    /**
     * Extracts an integer from the data, with support for both 2-byte and 4-byte lengths.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to extract (default is 2).
     * @return The extracted integer.
     */
    fun int(data: ByteArray, blockNumber: Int, offset: Int, len: Int = 2): Int {
        val byteArray = bytes(data, blockNumber, offset, len)
        return when (len) {
            4 -> ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).int.toUInt().toInt()  // Convert to unsigned Int (UInt)
            2 -> ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).short.toUShort().toInt()  // Convert to unsigned Short (UShort) and then to Int
            else -> throw IllegalArgumentException("Unsupported length for unsigned number extraction")
        }
    }

    /**
     * Decodes a specific section of bytes into a LocalDateTime object using a predefined format.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to extract (default is 16).
     * @return The decoded LocalDateTime object.
     */
    fun datetime(data: ByteArray, blockNumber: Int, offset: Int, len: Int = 16): LocalDateTime {
        val dateString = string(data, blockNumber, offset, len)
        val formatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm")
        return LocalDateTime.parse(dateString, formatter)
    }

    /**
     * Extracts a float value from the data, supporting both 4-byte and 8-byte lengths.
     *
     * @param data The byte array containing the data.
     * @param blockNumber The block number, used to calculate the starting position.
     * @param offset The offset within the block where the extraction starts.
     * @param len The length of the byte array to extract (default is 8).
     * @param fracRound The number of decimal places to round the float to (optional).
     * @return The decoded and optionally rounded float value.
     */
    fun float(data: ByteArray, blockNumber: Int, offset: Int, len: Int = 8, fracRound: Int? = null): Float? {
        val byteArray = bytes(data, blockNumber, offset, len)
        val value = when (len) {
            8 -> ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).double.toFloat()
            4 -> ByteBuffer.wrap(byteArray).order(ByteOrder.LITTLE_ENDIAN).float
            else -> null
        }
        return value?.let { roundFloat(it.toDouble(), fracRound) }
    }

    /**
     * Rounds a float value to the specified number of decimal places.
     * If the fracRound is null, the value is returned without rounding.
     *
     * @param value The float value to round.
     * @param fracRound The number of decimal places to round to (optional).
     * @return The rounded float value.
     */
    private fun roundFloat(value: Double, fracRound: Int? = 2): Float {
        // If fracRound is null, return the value as is (without rounding)
        if (fracRound == null) {
            return value.toFloat()
        }
        return (Math.round(value * 10.0.pow(fracRound.toDouble())) / 10.0.pow(fracRound.toDouble())).toFloat()
    }
}
