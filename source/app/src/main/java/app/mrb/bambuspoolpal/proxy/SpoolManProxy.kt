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

package app.mrb.bambuspoolpal.proxy

import android.util.Base64
import android.util.Log
import app.mrb.bambuspoolpal.network.ApiResponse
import app.mrb.bambuspoolpal.network.handleApiError
import app.mrb.bambuspoolpal.network.handleHttpException
import app.mrb.bambuspoolpal.preferences.Constants
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Url
import java.io.IOException

// Data class representing the structure to send to the API
@JsonClass(generateAdapter = true)
data class BambuLabTagScannerData(
    val blocks: String, // Base64 encoded ByteArray representing the blocks of data
    val weight: Int     // Actual spool weight in grams
)

// API service interface defining the POST request
interface ApiService {
    @POST
    suspend fun sendBambuLabTagScannerData(@Url base_url: String, @Body data: BambuLabTagScannerData): Response<Unit>
}

// Singleton Retrofit instance for API calls
object ApiClient {
    // Moshi JSON parser configuration for parsing and serializing data
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory()) // Adds the Kotlin-specific JSON adapter
        .build()

    // OkHttpClient for making network requests
    private val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    // Retrofit instance that sets up the base URL and converter factory for API calls
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(Constants.SPOOLMAN_PROXY_BASE_URL) // Set the base URL for the API
        .addConverterFactory(MoshiConverterFactory.create(moshi)) // JSON converter
        .client(okHttpClient) // Use OkHttpClient for making requests
        .build()

    // Create the API service interface to interact with the API
    val apiService: ApiService = retrofit.create(ApiService::class.java)
}

object SpoolManProxy {
    // Upload filament data asynchronously and return the response through a callback
    suspend fun uploadFilamentData(@Url endpoint: String, byteArrayData: ByteArray, weight: Int): ApiResponse<Unit> {
        val base64Encoded = Base64.encodeToString(byteArrayData, Base64.NO_WRAP)
        val data = BambuLabTagScannerData(base64Encoded, weight)

        return try {
            // Send the data to the API asynchronously and get the response
            val response: Response<Unit> = ApiClient.apiService.sendBambuLabTagScannerData(endpoint, data)

            // Check if the response was successful
            if (response.isSuccessful) {
                ApiResponse.Success(Unit) // Use Unit for a successful response with no data
            } else {
                handleApiError(response)
            }
        } catch (e: IOException) {
            Log.e("SpoolManProxy", "Network error uploading filament data", e)
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: HttpException) {
            Log.e("SpoolManProxy", "HTTP exception uploading filament data", e)
            handleHttpException(e)
        } catch (e: Exception) {
            Log.e("SpoolManProxy", "Unknown error uploading filament data", e)
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }
}
