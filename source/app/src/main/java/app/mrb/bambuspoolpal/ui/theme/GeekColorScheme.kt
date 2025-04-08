package app.mrb.bambuspoolpal.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val GeekLightColors = lightColorScheme(
    primary = Color(0xFF007F5F),         // Vert foncé "coder"
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB2F2BB), // Vert clair pastel

    secondary = Color(0xFF0A9396),       // Bleu canard
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFA8DADC),

    background = Color(0xFFF1F1F1),
    onBackground = Color(0xFF111111),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111111),

    error = Color(0xFFBA1A1A),
    onError = Color.White
)

val GeekDarkColors = darkColorScheme(
    primary = Color(0xFF00FF9F),         // Vert néon
    onPrimary = Color.Black,             // Texte sur vert
    primaryContainer = Color(0xFF003322), // Fond container vert

    secondary = Color(0xFF00BFFF),       // Bleu "holo"
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF002A3A),

    background = Color(0xFF0A0A0A),      // Noir profond
    onBackground = Color(0xFFE0E0E0),    // Texte général

    surface = Color(0xFF1A1A1A),         // Cartes, panneaux
    onSurface = Color(0xFFE0E0E0),

    error = Color(0xFFFF0066),           // Magenta saturé
    onError = Color.Black
)
