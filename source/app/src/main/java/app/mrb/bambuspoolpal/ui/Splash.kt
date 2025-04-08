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

package app.mrb.bambuspoolpal.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.mrb.bambuspoolpal.BuildConfig
import app.mrb.bambuspoolpal.R
import kotlinx.coroutines.launch

/**
 * Composable for displaying the splash screen of the application.
 *
 * This screen shows the logo, app version, and a disclaimer message.
 * Includes animated scaling and fading on start, and a button to continue.
 *
 * @param onDismiss Callback triggered when the user accepts and continues.
 */
@Composable
fun SplashScreen(onDismiss: () -> Unit) {
    // Animation for scale and alpha (fade-in)
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Launch entrance animations when the screen appears
    LaunchedEffect(Unit) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 800)
            )
        }
    }

    // Main layout of the splash screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App logo with fade-in animation
        Image(
            painter = painterResource(id = R.drawable.icon), // Replace with your logo resource
            contentDescription = "App Logo",
            modifier = Modifier
                .size(120.dp)
                .alpha(alpha.value)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Title text (localized)
        TitleBar(
            title = stringResource(R.string.welcome_to_bambuspoolpal)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Disclaimer text with app version (from BuildConfig)
        Text(
            text = stringResource(R.string.disclaimer_text),
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Accept button
        Button(onClick = onDismiss) {
            Text(stringResource(R.string.i_agree))
        }
    }
}
