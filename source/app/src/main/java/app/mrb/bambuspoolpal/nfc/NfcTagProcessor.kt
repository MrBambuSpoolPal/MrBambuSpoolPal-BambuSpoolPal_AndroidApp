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

package app.mrb.bambuspoolpal.nfc

import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.util.Log
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.generators.HKDFBytesGenerator
import org.bouncycastle.crypto.params.HKDFParameters

/**
 * Data class representing the NFC tag data including its UID and byte array.
 */
data class NfcTagData(
    var uid: String,
    var bytes: ByteArray
)

/**
 * Class to process NFC tags, authenticate them, and retrieve data from Mifare Classic tags.
 */
class NfcTagProcessor {

    /**
     * Processes an NFC tag, retrieves and decodes its data.
     *
     * @param tag The NFC tag to be processed.
     * @return An NfcTagData object containing the UID and data bytes of the tag.
     */
    fun processTag(tag: Tag): NfcTagData {
        // Retrieve the UID of the tag
        val uid = tag.id ?: throw IllegalArgumentException("Tag UID is null")
        val uidHex = uid.joinToString("") { "%02X".format(it) }
        Log.d("TagProcessor", "Processing tag UID: $uidHex")

        // Get the MifareClassic instance from the tag
        val mifare = MifareClassic.get(tag)
        val sectorCount = mifare.sectorCount
        val keys = deriveKeys(uid, sectorCount)
        val tagData = ByteArray(mifare.size)

        mifare.use {

            try {
                it.connect()
            } catch (e: Exception) {
                Log.e("TagProcessor", "Error connecting to tag: ${e.message}")
                throw e
            }

            // Loop through each sector of the Mifare Classic tag
            for (sector in 0 until it.sectorCount) {
                try {
                    // Authenticate with Key A for each sector
                    if (!it.authenticateSectorWithKeyA(sector, keys[sector])) {
                        throw AuthenticationException("Authentication failed for sector $sector")
                    }

                    // Read all blocks in the sector
                    for (block in 0 until it.getBlockCountInSector(sector)) {
                        val absoluteBlockIndex = it.sectorToBlock(sector) + block
                        val blockData = it.readBlock(absoluteBlockIndex)
                        System.arraycopy(blockData, 0, tagData, absoluteBlockIndex * 16, 16)
                    }
                } catch (e: Exception) {
                    Log.e("TagProcessor", "Error processing sector $sector: ${e.message}")
                    throw e // Re-throw to indicate failure to process the tag
                }
            }
        }
        // Return the processed tag data
        return NfcTagData(uidHex, tagData)
    }

    /**
     * Derives keys for each sector of the Mifare Classic tag using HKDF and a master key.
     *
     * @param uid The UID of the NFC tag.
     * @param sectorCount The number of sectors on the tag.
     * @return A list of 6-byte keys derived for each sector.
     */
    private fun deriveKeys(uid: ByteArray, sectorCount: Int): List<ByteArray> {
        // The master key used for HKDF derivation
        val masterKey = byteArrayOf(
            0x9a.toByte(), 0x75.toByte(), 0x9c.toByte(), 0xf2.toByte(),
            0xc4.toByte(), 0xf7.toByte(), 0xca.toByte(), 0xff.toByte(),
            0x22.toByte(), 0x2c.toByte(), 0xb9.toByte(), 0x76.toByte(),
            0x9b.toByte(), 0x41.toByte(), 0xbc.toByte(), 0x96.toByte()
        )

        // Fixed context string for HKDF derivation
        val context = "RFID-A\u0000".toByteArray(Charsets.UTF_8)

        // Initialize the HKDFBytesGenerator with a SHA-256 digest
        val hkdf = HKDFBytesGenerator(SHA256Digest())
        val keys = mutableListOf<ByteArray>() // List to store the derived keys

        // Calculate the total key length required (6 bytes per sector)
        val totalKeyLength = sectorCount * 6
        val derivedBuffer = ByteArray(totalKeyLength) // Buffer to hold the derived keys

        // Initialize the HKDF with the UID (salt), master key, and context
        hkdf.init(HKDFParameters(uid, masterKey, context))

        // Generate the derived keys and fill the buffer
        hkdf.generateBytes(derivedBuffer, 0, totalKeyLength)

        // Split the derived buffer into individual 6-byte keys for each sector
        for (i in 0 until sectorCount) {
            val start = i * 6
            val key = derivedBuffer.copyOfRange(start, start + 6) // Extract a 6-byte key
            keys.add(key) // Add the key to the list
            Log.d("MainActivity", "Derived Key A for sector $i: ${key.joinToString("") { "%02X".format(it) }}}") // Log the key
        }

        // Return the list of derived keys
        return keys
    }
}

/**
 * Custom exception class to indicate errors during NFC tag authentication.
 */
class AuthenticationException(message: String) : Exception(message)
