package app.mrb.bambuspoolpal.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color


val SciFiDarkColors = darkColorScheme(
    primary = Color(0xFF00FFF7),           // Cyan néon
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003B4E),  // Bleu profond

    secondary = Color(0xFF9D4EDD),         // Violet galactique
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2C004D),

    background = Color(0xFF0B0F1A),        // Noir bleuté futuriste
    onBackground = Color(0xFFE3E8FF),      // Texte clair

    surface = Color(0xFF161B29),           // Cartes et panneaux
    onSurface = Color(0xFFE3E8FF),

    error = Color(0xFFFF003C),             // Rouge alerte
    onError = Color.Black
)

val SciFiLightColors = lightColorScheme(
    primary = Color(0xFF007B9E),           // Bleu techno
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC0F7FF),

    secondary = Color(0xFF9D4EDD),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF0DFFF),

    background = Color(0xFFF5F9FF),
    onBackground = Color(0xFF111827),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),

    error = Color(0xFFD00036),
    onError = Color.White
)
