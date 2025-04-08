package app.mrb.bambuspoolpal.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Define the light color scheme with pastel and grey tones for a 3D printing app
 val Print3DLightColors = lightColorScheme(
   primary = Color(0xFF546E7A),           // Bleu-gris plus neutre
   onPrimary = Color.White,
   primaryContainer = Color(0xFFB0BEC5),

   secondary = Color(0xFFF48FB1),         // Corail un peu plus intense (peut être ajusté)
   onSecondary = Color.White,
   secondaryContainer = Color(0xFFF8BBD0),

   tertiary = Color(0xFF64B5F6),          // Bleu un peu plus soutenu (peut être ajusté)
   onTertiary = Color.White,
   tertiaryContainer = Color(0xFF90CAF9),

   background = Color(0xFFF5F5F5),        // Gris clair
   onBackground = Color(0xFF212121),

   surface = Color(0xFFFFFFFF),
   onSurface = Color(0xFF212121),

   error = Color(0xFFE57373),
   onError = Color.White
)

// Define the dark color scheme with darker and more contrasted tones for a 3D printing app
 val Print3DDarkColors = darkColorScheme(
   primary = Color(0xFF607D8B),           // Bleu-gris foncé
   onPrimary = Color.White,
   primaryContainer = Color(0xFF37474F),

   secondary = Color(0xFFEC407A),         // Corail foncé plus intense (peut être ajusté)
   onSecondary = Color.White,
   secondaryContainer = Color(0xFF880E4F),

   tertiary = Color(0xFF1E88E5),          // Bleu foncé plus soutenu (peut être ajusté)
   onTertiary = Color.White,
   tertiaryContainer = Color(0xFF0D47A1),

   background = Color(0xFF212121),        // Gris foncé
   onBackground = Color(0xFFE0E0E0),

   surface = Color(0xFF303030),
   onSurface = Color(0xFFE0E0E0),

   error = Color(0xFFF44336),
   onError = Color.Black
)

