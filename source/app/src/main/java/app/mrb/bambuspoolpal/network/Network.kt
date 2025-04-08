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

package app.mrb.bambuspoolpal.network

import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.Url
import java.net.InetAddress

/**
 * Checks if the given IP address is a private IP address.
 *
 * @param ip The IP address as a string.
 * @return True if the IP is private, false otherwise.
 */
fun isPrivateIP(ip: String): Boolean {
    return ip.startsWith("10.") || ip.startsWith("192.168.") ||
            (ip.startsWith("172.") && ip.split(".")[1].toInt() in 16..31) ||
            ip == "127.0.0.1"
}

/**
 * Extracts the IP address from a URL.
 *
 * @param url The URL as a string.
 * @return The IP address as a string, or null if the URL is invalid or the IP cannot be resolved.
 */
fun getIpFromUrl(url: String): String? {
    val uri = Uri.parse(url)
    val domain = uri.host ?: return null // Get the domain
    return InetAddress.getByName(domain).hostAddress // Resolve the IP
}

/**
 * Creates an OkHttpClient with custom settings based on the URL.
 * If the URL's IP address is private, HTTPS verification is disabled.
 *
 * @param url The URL as a string.
 * @return An OkHttpClient instance.
 */
fun httpClient(@Url url: String): OkHttpClient {
    val ip = getIpFromUrl(url)

    return OkHttpClient.Builder().apply {
        if (ip != null && isPrivateIP(ip)) { // Check if IP is not null and private
            this.hostnameVerifier { _, _ -> true } // Disable HTTPS verification (dev only)
        }
    }.build()
}

/**
 * Handles API errors from a Retrofit Response.
 *
 * @param response The Retrofit Response object.
 * @return An ApiResponse representing the error.
 */
fun handleApiError(response: Response<Unit>): ApiResponse<Unit> {
    return when (response.code()) {
        422 -> {
            // Handle validation errors (if applicable)
            ApiResponse.ValidationError(null) // You may need to parse the error body here
        }
        in 400..499 -> {
            Log.e("SpoolManProxy", "Client error: ${response.code()} - ${response.message()}")
            ApiResponse.ClientError(response.message())
        }
        in 500..599 -> {
            Log.e("SpoolManProxy", "Server error: ${response.code()} - ${response.message()}")
            ApiResponse.ServerError(response.message())
        }
        else -> {
            Log.e("SpoolManProxy", "Unknown API error: ${response.code()} - ${response.message()}")
            ApiResponse.UnknownError(response.message())
        }
    }
}

/**
 * Handles HTTP exceptions from Retrofit.
 *
 * @param e The HttpException object.
 * @return An ApiResponse representing the error.
 */
fun handleHttpException(e: HttpException): ApiResponse<Unit> {
    return when (e.code()) {
        422 -> {
            // Handle validation errors (if applicable)
            ApiResponse.ValidationError(null) // You may need to parse the error body here
        }
        in 400..499 -> {
            Log.e("SpoolManProxy", "Client error: ${e.code()} - ${e.message()}")
            ApiResponse.ClientError(e.message())
        }
        in 500..599 -> {
            Log.e("SpoolManProxy", "Server error: ${e.code()} - ${e.message()}")
            ApiResponse.ServerError(e.message())
        }
        else -> {
            Log.e("SpoolManProxy", "Unknown HTTP error: ${e.code()} - ${e.message()}")
            ApiResponse.UnknownError(e.message())
        }
    }
}

/**
 * Sealed class to represent possible API responses.
 *
 * @param T The type of data in a successful response.
 */
sealed class ApiResponse<out T> {
    data class Success<out T>(val data: T) : ApiResponse<T>()
    data class NetworkError(val message: String?) : ApiResponse<Nothing>()
    data class ServerError(val message: String?) : ApiResponse<Nothing>()
    data class ClientError(val message: String?) : ApiResponse<Nothing>()
    data class ValidationError(val validationError: ValidationErrorDetail?) : ApiResponse<Nothing>()
    data class UnknownError(val message: String?) : ApiResponse<Nothing>()
}

/**
 * Data class to represent validation errors from the API.
 *
 * @param detail List of Detail objects containing validation error details.
 */
data class ValidationErrorDetail(
    val detail: List<Detail>
)

data class Detail(
    val loc: List<Any>, // Peut contenir des Strings ou des Ints
    val msg: String,
    val type: String
)

/**
 * Performs an action on the Spoolman API.
 *
 * @param action A suspend function that performs an action and returns an ApiResponse.
 * @return The data object from the API response.
 * @throws Exception if an API error occurs.
 */
suspend fun <T> performAction(action: suspend () -> ApiResponse<T>): T {
    return when (val result = withContext(Dispatchers.IO) { action() }) {
        is ApiResponse.Success -> result.data
        is ApiResponse.ValidationError -> throw Exception("Validation Error: ${result.validationError?.detail?.joinToString()}")
        is ApiResponse.NetworkError -> throw Exception("Network Error: ${result.message}")
        is ApiResponse.ServerError -> throw Exception("Server Error: ${result.message}")
        is ApiResponse.ClientError -> throw Exception("Client Error: ${result.message}")
        is ApiResponse.UnknownError -> throw Exception("Unknown Error: ${result.message}")
    }
}
