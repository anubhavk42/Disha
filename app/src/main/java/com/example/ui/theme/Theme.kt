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
    primary = Color(0xFF5FD3B8),
    secondary = Color(0xFFF2B08C),
    tertiary = Color(0xFF8FF0C7),
    background = Color(0xFF0D1412),
    surface = Color(0xFF16211D),
    surfaceVariant = Color(0xFF1E322B),
    onPrimary = Color(0xFF00382E),
    onSecondary = Color(0xFF4A2412),
    onTertiary = Color(0xFF00391F),
    onBackground = Color(0xFFE3EDE9),
    onSurface = Color(0xFFE3EDE9),
    onSurfaceVariant = Color(0xFFB9C9C3),
    outline = Color(0xFF4F625C),
    outlineVariant = Color(0xFF24332E)
  )

private val CustomLightColorScheme =
  lightColorScheme(
    primary = Color(0xFF166B5A),
    secondary = Color(0xFFB2532F),
    tertiary = Color(0xFF0F6B45),
    background = Color(0xFFF6FAF9),
    surface = Color(0xFFEDF5F2),
    surfaceVariant = Color(0xFFDCEBE6),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF10201C),
    onSurface = Color(0xFF10201C),
    onSurfaceVariant = Color(0xFF49605A),
    outline = Color(0xFFCFE3DD),
    outlineVariant = Color(0xFFEFF7F4)
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
      // Same ~167° brand teal hue as the normal dark primary, pushed to a
      // higher saturation/lightness so it reads as ~14:1 against pure black
      // (WCAG AAA) instead of the ~9:1 the normal primary would give here.
      baseScheme.copy(
        primary = Color(0xFF2BEEC4),
        background = Color(0xFF000000),
        surface = Color(0xFF000000),
        onPrimary = Color.Black,
        onBackground = Color.White,
        onSurface = Color.White,
        outline = Color.White
      )
    } else {
      // Same brand teal hue as the normal light primary, darkened and
      // more saturated so it reads as ~7.7:1 against pure white (WCAG AAA).
      baseScheme.copy(
        primary = Color(0xFF085E4C),
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
