package app.mrb.bambuspoolpal.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val GreyLightColors = lightColorScheme(
    primary = Color(0xFF1E1E1E),           // Presque noir
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3D3D3),

    secondary = Color(0xFF616161),         // Gris moyen
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEEEEEE),

    background = Color(0xFFF9F9F9),
    onBackground = Color(0xFF1E1E1E),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1E1E1E),

    error = Color(0xFFD32F2F),
    onError = Color.White
)

val GreyDarkColors = darkColorScheme(
    primary = Color(0xFF90CAF9),           // Bleu doux pour action (facultatif)
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2B2B2B),  // Gris foncé

    secondary = Color(0xFFB0BEC5),         // Gris clair bleuté
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF37474F),

    background = Color(0xFF121212),        // Fond noir/gris
    onBackground = Color(0xFFE0E0E0),      // Texte

    surface = Color(0xFF1E1E1E),           // Cartes, zones
    onSurface = Color(0xFFEEEEEE),

    error = Color(0xFFFF6E6E),
    onError = Color.Black
)
