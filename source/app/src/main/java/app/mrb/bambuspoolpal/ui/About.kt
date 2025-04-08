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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.mrb.bambuspoolpal.BuildConfig
import app.mrb.bambuspoolpal.R
import app.mrb.bambuspoolpal.ui.HtmlText
import app.mrb.bambuspoolpal.ui.Section
import app.mrb.bambuspoolpal.ui.StyledBox
import app.mrb.bambuspoolpal.ui.TitleBar

@Composable
fun AboutScreen() {

    // Main UI layout for the about screen
    Column(
        modifier = Modifier
            .fillMaxSize()  // Fill the entire screen
            .padding(8.dp)  // Apply padding of 8 density-independent pixels around the content
            .verticalScroll(rememberScrollState()),  // Enable vertical scrolling and remember the scroll state
        verticalArrangement = Arrangement.spacedBy(6.dp)  // Arrange child elements vertically with a spacing of 6 density-independent pixels
    ) {
        // Title bar displayed at the top of the screen
        TitleBar(title = stringResource(R.string.about_screen_title))

        // Section for displaying "How to" information
        Section(title = stringResource(R.string.how_to_title)) {

            // A styled container for the "How to" content
            StyledBox {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp), // Arrange child elements vertically with a spacing of 4 density-independent pixels
                    modifier = Modifier.fillMaxWidth() // Make the content fill the maximum available width
                ) {
                    // Display the "How to" text, which might contain HTML formatting, including the app version
                    HtmlText(stringResource(R.string.how_to, BuildConfig.VERSION_NAME))
                }
            }

        }

    }

}