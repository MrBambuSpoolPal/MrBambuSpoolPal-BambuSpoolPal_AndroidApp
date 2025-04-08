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

import android.annotation.SuppressLint
import android.util.Log
import app.mrb.bambuspoolpal.network.ApiResponse
import app.mrb.bambuspoolpal.network.ValidationErrorDetail
import app.mrb.bambuspoolpal.preferences.ConfigViewModel
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Path
import java.io.IOException
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

interface SpoolmanRepository {
    suspend fun getVendorsByName(@Path("name") name: String): ApiResponse<List<Vendor>>
    suspend fun addVendor(vendor: VendorRequestBody): ApiResponse<Vendor>
    suspend fun getFilaments(request: FilamentQueryParams): ApiResponse<List<FilamentResponse>>
    suspend fun addFilament(filament: FilamentRequestBody): ApiResponse<FilamentResponse>
    suspend fun getSpools(request: SpoolSearchQueryParams): ApiResponse<List<SpoolResponse>>
    suspend fun addSpool(spool: SpoolRequestBody): ApiResponse<SpoolResponse>
    suspend fun updateSpool(id: Int, spool: SpoolRequestBody): ApiResponse<SpoolResponse>
    suspend fun updateSpoolUsage(id: Int, usage: UseFilamentRequest): ApiResponse<SpoolResponse>
}

object SpoolmanRepositoryImpl : SpoolmanRepository {

    private fun <T : Any> dataClassToQueryMap(dataClass: T): Map<String, String> {
        return dataClass.javaClass.declaredFields.associate { field ->
            field.isAccessible = true
            val propertyNameAnnotation = field.getAnnotation(ApiParameter::class.java)
            val queryName = propertyNameAnnotation?.value ?: field.name
            queryName to field.get(dataClass)?.toString()
        }.filterValues { it != null }
            .mapValues { it.value!! }
    }

    private fun <T> Response<T>.toApiResponse(): ApiResponse<T> {
        return if (this.isSuccessful) {
            body()?.let { ApiResponse.Success(it) } ?: ApiResponse.UnknownError("Response body is null")
        } else {
            when (this.code()) {
                422 -> {
                    val errorBody: ResponseBody? = this.errorBody()
                    if (errorBody != null) {
                        try {
                            val moshi: Moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                            val adapter: JsonAdapter<ValidationErrorDetail> = moshi.adapter(ValidationErrorDetail::class.java)
                            val error: ValidationErrorDetail? = adapter.fromJson(errorBody.string())
                            ApiResponse.ValidationError(error)
                        } catch (e: Exception) {
                            Log.e("ApiResponse", "Error parsing validation error", e)
                            ApiResponse.UnknownError("Error parsing validation error: ${e.message}")
                        }
                    } else {
                        ApiResponse.UnknownError("Validation error body is null")
                    }
                }
                in 400..499 -> {
                    Log.e("ApiResponse", "Client error: ${this.code()} - ${this.message()}")
                    ApiResponse.ClientError(this.message())
                }
                in 500..599 -> {
                    Log.e("ApiResponse", "Server error: ${this.code()} - ${this.message()}")
                    ApiResponse.ServerError(this.message())
                }
                else -> {
                    Log.e("ApiResponse", "Unknown error: ${this.code()} - ${this.message()}")
                    ApiResponse.UnknownError(this.message())
                }
            }
        }
    }

    override suspend fun addVendor(vendor: VendorRequestBody): ApiResponse<Vendor> {
        return try {
            SpoolmanApiClient.apiService.addVendor(vendor).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun getVendorsByName(name: String): ApiResponse<List<Vendor>> {
        return try {
            SpoolmanApiClient.apiService.getVendorsByName(name).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun getFilaments(request: FilamentQueryParams): ApiResponse<List<FilamentResponse>> {
        return try {
            val queryMap = dataClassToQueryMap(request)
            SpoolmanApiClient.apiService.getFilaments(queryMap).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun addFilament(filament: FilamentRequestBody): ApiResponse<FilamentResponse> {
        return try {
            SpoolmanApiClient.apiService.addFilament(filament).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun getSpools(request: SpoolSearchQueryParams): ApiResponse<List<SpoolResponse>> {
        return try {
            val queryMap = dataClassToQueryMap(request)
            SpoolmanApiClient.apiService.getSpools(queryMap).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun addSpool(spool: SpoolRequestBody): ApiResponse<SpoolResponse> {
        return try {
            SpoolmanApiClient.apiService.addSpool(spool).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun updateSpool(id: Int, spool: SpoolRequestBody): ApiResponse<SpoolResponse> {
        return try {
            SpoolmanApiClient.apiService.updateSpool(id, spool).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

    override suspend fun updateSpoolUsage(id: Int, usage: UseFilamentRequest): ApiResponse<SpoolResponse> {
        return try {
            SpoolmanApiClient.apiService.updateSpoolUsage(id, usage).toApiResponse()
        } catch (e: IOException) {
            ApiResponse.NetworkError("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResponse.UnknownError(e.message ?: "Unknown exception")
        }
    }

}

// Singleton Retrofit instance for API calls.
object SpoolmanApiClient {

    private lateinit var viewModel: ConfigViewModel // ViewModel reference for dynamic baseUrl.

    fun initialize(viewModel: ConfigViewModel) {
        this.viewModel = viewModel
    }

    // Moshi JSON parser configuration for parsing and serializing data.
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()


    private fun httpClient(): OkHttpClient {
        try {
            // Créer un TrustManager qui n'effectue aucune vérification de certificat
            val trustAllCerts = arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })

            // Installer le TrustManager personnalisé
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Créer une SocketFactory SSL à partir du SSLContext
            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { hostname, session -> true }

            return builder.build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    // Creates a Retrofit instance with the provided baseUrl.
    private fun createApiService(baseUrl: String): SpoolmanApiService {

        val getSpoolmanApiSelfSigned = viewModel.getSpoolmanApiSelfSigned()

        val okHttpClient: OkHttpClient = if (getSpoolmanApiSelfSigned) {
            httpClient() // Appeler votre fonction personnalisée httpClient()
        } else {
            OkHttpClient() // Créer une instance OkHttpClient standard
        }

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .client(okHttpClient)
            .build()
        return retrofit.create(SpoolmanApiService::class.java)
    }

    // Provides a dynamically updated SpoolmanApiService instance.
    val apiService: SpoolmanApiService
        get() {
            val baseUrl = viewModel.getSpoolmanApiBaseUrl()
            return createApiService(baseUrl)
        }
}
