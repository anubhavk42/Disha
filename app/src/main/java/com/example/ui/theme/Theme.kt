package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val CustomDarkColorScheme =
  darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFEFB8C8),
    tertiary = Color(0xFF80F5D2),
    background = Color(0xFF1C1B1F),
    surface = Color(0xFF2B2930),
    surfaceVariant = Color(0xFF332D41),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFE6E1E5),
    onSurface = Color(0xFFE6E1E5),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F),
    outlineVariant = Color(0xFF25232A)
  )

private val CustomLightColorScheme =
  lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFFB3261E),
    tertiary = Color(0xFF006B54),
    background = Color(0xFFFAF9FD),
    surface = Color(0xFFF3EDF7),
    surfaceVariant = Color(0xFFE8DEF8),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFFE1DDF5),
    outlineVariant = Color(0xFFF5F2F8)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  highContrast: Boolean = false,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  isDarkThemeGlobal = darkTheme
  isHighContrastThemeGlobal = highContrast
  
  val baseScheme = if (darkTheme) CustomDarkColorScheme else CustomLightColorScheme
  val colorScheme = if (highContrast) {
    if (darkTheme) {
      baseScheme.copy(
        primary = Color(0xFF00FFFF),
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        outline = Color.White
      )
    } else {
      baseScheme.copy(
        primary = Color(0xFF0000FF),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        onPrimary = Color.White,
        onBackground = Color.Black,
        onSurface = Color.Black,
        outline = Color.Black
      )
    }
  } else {
    baseScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = Color.Transparent.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
      }
    }
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
