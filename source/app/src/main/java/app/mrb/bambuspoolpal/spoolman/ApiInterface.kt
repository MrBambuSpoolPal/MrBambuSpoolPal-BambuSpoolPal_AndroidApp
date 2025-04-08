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

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * Custom annotation to specify property names for API request parameters.
 *
 * @property value The name of the property as expected by the API.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class ApiParameter(val value: String)

/**
 * Retrofit API service interface for interacting with the Spoolman API.
 * This interface defines the endpoints and methods for fetching and updating data.
 */
interface SpoolmanApiService {

    companion object {
        const val SPOOL_API_PREFIX = "api/v1"
    }

    /**
     * Retrieves a list of all vendors.
     *
     * @return A list of Vendor objects.
     */
    @GET("${SPOOL_API_PREFIX}/vendors/")
    suspend fun getVendors(): List<Vendor>

    /**
     * Retrieves a specific vendor by its ID.
     *
     * @param id The ID of the vendor.
     * @return The Vendor object.
     */
    @GET("${SPOOL_API_PREFIX}/vendors/{id}/")
    suspend fun getVendorById(@Path("id") id: Int): Vendor

    /**
     * Retrieves a list of vendors by name.
     *
     * @param name The name of the vendor.
     * @return A Response object containing a list of Vendor objects.
     */
    @GET("${SPOOL_API_PREFIX}/vendor")
    suspend fun getVendorsByName(@Query("name") name: String): Response<List<Vendor>>

    /**
     * Adds a new vendor.
     *
     * @param vendor The VendorRequestBody containing vendor data.
     * @return A Response object containing the created Vendor object.
     */
    @POST("${SPOOL_API_PREFIX}/vendor")
    suspend fun addVendor(@Body vendor: VendorRequestBody): Response<Vendor>

    /**
     * Retrieves a list of filaments based on the provided query parameters.
     *
     * @param params A map of query parameters.
     * @return A Response object containing a list of FilamentResponse objects.
     */
    @GET("${SPOOL_API_PREFIX}/filament")
    suspend fun getFilaments(@QueryMap params: Map<String, String>): Response<List<FilamentResponse>>

    /**
     * Adds a new filament.
     *
     * @param filament The FilamentRequestBody containing filament data.
     * @return A Response object containing the created FilamentResponse object.
     */
    @POST("${SPOOL_API_PREFIX}/filament")
    suspend fun addFilament(@Body filament: FilamentRequestBody): Response<FilamentResponse>

    /**
     * Retrieves a list of spools based on the provided query parameters.
     *
     * @param params A map of query parameters.
     * @return A Response object containing a list of SpoolResponse objects.
     */
    @GET("${SPOOL_API_PREFIX}/spool")
    suspend fun getSpools(@QueryMap params: Map<String, String>): Response<List<SpoolResponse>>

    /**
     * Adds a new spool.
     *
     * @param spool The SpoolRequestBody containing spool data.
     * @return A Response object containing the created SpoolResponse object.
     */
    @POST("${SPOOL_API_PREFIX}/spool")
    suspend fun addSpool(@Body spool: SpoolRequestBody): Response<SpoolResponse>

    /**
     * Updates a spool with the given ID.
     *
     * @param spoolId The ID of the spool to update.
     * @param spool The SpoolRequestBody containing the updated spool data.
     * @return A Response object containing the updated SpoolResponse object.
     */
    @PATCH("${SPOOL_API_PREFIX}/spool/{spool_id}")
    suspend fun updateSpool(
        @Path("spool_id") spoolId: Int,
        @Body spool: SpoolRequestBody
    ): Response<SpoolResponse>

    /**
     * Updates the usage of a spool with the given ID.
     *
     * @param spoolId The ID of the spool to update.
     * @param spool The UseFilamentRequest containing the usage data.
     * @return A Response object containing the updated SpoolResponse object.
     */
    @PUT("${SPOOL_API_PREFIX}/spool/{spool_id}/use")
    suspend fun updateSpoolUsage(
        @Path("spool_id") spoolId: Int,
        @Body spool: UseFilamentRequest
    ): Response<SpoolResponse>
}
