package app.mrb.bambuspoolpal.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Composable function that provides the application's theme
@Composable
fun BambuspoolpalTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Default value to follow system dark mode setting
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit // Content to be styled with the theme
) {
    // Determine the color scheme based on the device's theme and dynamic color setting
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> Print3DDarkColors // Use the defined dark color scheme
        else -> Print3DLightColors // Use the defined light color scheme
    }

    // Apply the MaterialTheme with the determined color scheme, typography, and shapes
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Defined in Typography.kt
        content = content // The content to be themed
    )
}