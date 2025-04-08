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

import app.mrb.bambuspoolpal.network.ApiResponse
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import app.mrb.bambuspoolpal.shared.FilamentData
import kotlinx.coroutines.runBlocking
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

// Data class representing a filament record from the Spoolman database.
data class SpoolmanFilamentData(
    val id: String,                  // Unique identifier for the filament
    val manufacturer: String,        // Name of the filament spoolman_database_bambu_manufacturer
    val name: String,                // Filament name
    val material: String,            // Material type (e.g., PLA, ABS)
    val density: Double,             // Filament density (g/cm³)
    val weight: Double,              // Total filament weight (grams)
    val spool_weight: Float,         // Weight of the empty spool (grams)
    val diameter: Double,            // Filament diameter (e.g., 1.75mm)
    val color_hex: String?,          // Primary color in HEX format (optional)
    val color_hexes: List<String>?,  // List of possible color variations (optional)
    val extruder_temp: Int,          // Recommended extruder temperature (°C)
    val bed_temp: Int,               // Recommended bed temperature (°C)
    val translucent: Boolean,        // Whether the filament is translucent
    val glow: Boolean                // Whether the filament is glow-in-the-dark
)

// Retrofit API service interface for fetching filament data.
interface FilamentApiService {
    @GET // API endpoint for retrieving filament data
    suspend fun getFilamentData(@Url endpoint: String): Response<List<SpoolmanFilamentData>>  // Get filament data from the API
}

// Singleton object for Retrofit instance configuration.
private object RetrofitInstance {

    // Function to get the latest base URL from ConfigViewModel
    private fun getBaseUrl(configViewModel: ConfigViewModel): String {
        return runBlocking { configViewModel.getSpoolmanBaseUrl() }
    }

    // Function to create a Retrofit instance dynamically
    fun createApiService(configViewModel: ConfigViewModel): FilamentApiService {
        val baseUrl = getBaseUrl(configViewModel)

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl) // Use dynamically extracted base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(FilamentApiService::class.java)
    }
}

private fun <T> Response<T>.toApiResponse(): ApiResponse<T> {
    return if (this.isSuccessful) {
        body()?.let { ApiResponse.Success(it) } ?: ApiResponse.UnknownError("Response body is null")
    } else {
        when (this.code()) {
            in 400..499 -> ApiResponse.ClientError(this.message())
            in 500..599 -> ApiResponse.ServerError(this.message())
            else -> ApiResponse.UnknownError(this.message())
        }
    }
}
// Object for filtering and processing filament data.
object FilamentDatabase {

    // Function to get filament data from the API and filter it.
    suspend fun getFilamentData(configViewModel: ConfigViewModel): ApiResponse<List<FilamentData>> {

        val apiService = RetrofitInstance.createApiService(configViewModel)
        val endpoint = runBlocking { configViewModel.getSpoolmanEndpoint() }

        // Make the Retrofit API call via the service
        val response = apiService.getFilamentData(endpoint).toApiResponse()

        val manufacturer = configViewModel.getManufacturer()

        // Convert the response to ApiResponse type
        // Check if the API response is successful and apply filtering if it is
        return when (response) {
            is ApiResponse.Success -> {
                // Apply filtering logic to the successful response data
                val filteredData = filter(response.data, manufacturer)
                ApiResponse.Success(filteredData)  // Return the filtered data in the Success response
            }
            else -> {
                ApiResponse.UnknownError("Unexpected error")
            }

        }
    }

    // Function to filter the filament data based on specific criteria.
    private fun filter(filamentDataList: List<SpoolmanFilamentData>, manufacturer: String): List<FilamentData> {
        val filteredList = mutableListOf<FilamentData>()

        // Loop through each filament record and apply filter criteria
        for (filament in filamentDataList) {
            // Only include filaments from Bambu Lab with a non-null color
            if (filament.manufacturer == manufacturer && filament.color_hex != null) {

                val correctedColorHex = if (filament.color_hex == "FFFFFF" && filament.translucent) {
                    "000000"
                } else {
                    filament.color_hex
                }

                filteredList.add(
                    FilamentData(
                        id = filament.id,
                        name = filament.name,
                        material = filament.material,
                        color_hex = correctedColorHex,
                        translucent = filament.translucent,
                        glow = filament.glow,
                        empty_spool_weight = filament.spool_weight.toInt(),
                        density = filament.density
                    )
                )
            }
        }

        // Return the filtered list of filament data
        return filteredList
    }

    // Function to compute the Jaccard similarity coefficient between two strings.
    private fun jaccardSimilarity(s1: String, s2: String): Double {
        val set1 = s1.lowercase().split(" ").toSet()
        val set2 = s2.lowercase().split(" ").toSet()

        val intersection = set1.intersect(set2).size.toDouble()
        val union = set1.union(set2).size.toDouble()

        return if (union == 0.0) 0.0 else intersection / union
    }

    // Function to sort filament data by similarity to a given target string.
    fun sortByTokenSimilarity(target: String, filaments: List<FilamentData>): List<Pair<FilamentData, Int>> {
        return filaments
            .map { it to (jaccardSimilarity(target, "${it.material} ${it.name}${if (it.translucent) " translucent" else ""}") * 100).toInt() }
            .filter { it.second > 0 }  // Filtrer les scores égaux à 0
            .sortedByDescending { it.second }
    }

}
