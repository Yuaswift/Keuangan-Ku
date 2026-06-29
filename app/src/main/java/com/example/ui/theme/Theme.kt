package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryMinimal,
    secondary = SecondaryMinimal,
    background = BackgroundMinimal,
    onBackground = OnBackgroundMinimal,
    surface = SolidWhite,
    onSurface = OnBackgroundMinimal,
    outline = OutlineMinimal,
    primaryContainer = PrimaryMinimal,
    onPrimaryContainer = SolidWhite,
    surfaceVariant = AccentLightBlue,
    onSurfaceVariant = SecondaryMinimal
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Set dynamicColor to false by default to ensure the Clean Minimalism design is displayed.
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
