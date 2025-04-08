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

package app.mrb.bambuspoolpal.preferences

/**
 * Constants object to store fixed values used throughout the application.
 * This includes base URLs and predefined strings for the application.
 */
object Constants {

    // Key for accessing application configuration settings
    const val APPLICATION_PARAM = "app_config"

    // Default language settings
    const val DEFAULT_LANGUAGE = "en"
    const val DEFAULT_LANGUAGE_PARAM = "language"
    // List of available languages the application supports
    val AVAILABLE_LANGUAGES: List<String> = listOf("fr", "es")

    // The base URL for API requests to the Spoolman database
    const val SPOOLMAN_DB_FILAMENTS_URL = "https://donkie.github.io/SpoolmanDB/filaments.json"
    const val SPOOLMAN_DB_FILAMENTS_URL_PARAM = "spoolman_filament_database_url"

    // Manufacturer name for Bambu Lab
    const val MANUFACTURER_BAMBU_LAB = "Bambu Lab"
    const val MANUFACTURER_BAMBU_LAB_PARAM = "spoolman_database_bambu_manufacturer"

    // The base URL for the Spoolman Proxy API
    const val SPOOLMAN_PROXY_BASE_URL = "http://localhost:7913/api/v1/spool/bambulab"
    const val SPOOLMAN_PROXY_BASE_URL_PARAM = "proxy_url"

    // Endpoint for interacting with Spoolman Proxy's filament data
    const val SPOOLMAN_PROXY_ENDPOINT = "/api/v1/spool/bambulab"
    const val SPOOLMAN_PROXY_ENDPOINT_PARAM = "proxy_endpoint"

    // Base URL for Spoolman API (local instance)
    const val SPOOLMAN_BASE_API_URL = "https://localhost:7912"
    const val SPOOLMAN_BASE_API_URL_PARAM = "spoolman_api_base_url"

    // Whether or not Spoolman API uses a self-signed certificate
    const val SPOOLMAN_API_SELF_SIGNED = false
    const val SPOOLMAN_API_SELF_SIGNED_PARAM = "spoolman_api_self_signed"

    // Default weight values for the spool (in grams)
    const val DEFAULT_SPOOL_WEIGHT = 1000
    const val DEFAULT_EMPTY_SPOOL_WEIGHT = 250

    // Whether or not to enable automatic triggering for Spoolman API
    const val SPOOLMAN_API_AUTO_TRIGGERING = false
    const val SPOOLMAN_API_AUTO_TRIGGERING_PARAM = "spoolman_api_auto_triggering"

    // Whether or not to show the spool UID (unique identifier)
    const val SHOW_SPOOL_UID = false
    const val SHOW_SPOOL_UID_PARAM = "show_uid"

    // Whether or not to show the splash screen at the app startup
    const val SHOW_SPLASH_SCREEN = true
    const val SHOW_SPLASH_SCREEN_PARAM = "show_splash_screen"
}
