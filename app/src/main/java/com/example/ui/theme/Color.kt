package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Global theme state for dynamic color resolution
var isDarkThemeGlobal: Boolean = true
var isHighContrastThemeGlobal: Boolean = false

// Clean Minimalism Styled Palette (dynamic getters for Dark and Light mode)
val SpaceBlack: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFFFFFFF)
    } else {
        if (isDarkThemeGlobal) Color(0xFF1C1B1F) else Color(0xFFFAF9FD) // Dark Slate vs Warm Soft White
    }

val DeepViolet: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFFFFFFF)
    } else {
        if (isDarkThemeGlobal) Color(0xFF2B2930) else Color(0xFFF3EDF7) // Dark gray/violet vs light purple-gray surface
    }

val CardViolet: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF121212) else Color(0xFFF5F5F5)
    } else {
        if (isDarkThemeGlobal) Color(0xFF332D41) else Color(0xFFE8DEF8) // Dark accent container vs elegant light accent container
    }

val ElectricViolet: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF00FFFF) else Color(0xFF0000FF) // Neon Cyan on dark vs Deep Blue on light
    } else {
        if (isDarkThemeGlobal) Color(0xFFD0BCFF) else Color(0xFF6750A4) // Light soft lavender vs striking deep purple
    }

val SunsetOrange: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFF5252) else Color(0xFFD50000) // Bright red vs deep crimson
    } else {
        if (isDarkThemeGlobal) Color(0xFFEFB8C8) else Color(0xFFB3261E) // Soft pink/coral vs vibrant cherry accent
    }

val MintyTeal: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF00FF00) else Color(0xFF007A00) // Neon green vs dark green
    } else {
        if (isDarkThemeGlobal) Color(0xFF80F5D2) else Color(0xFF006B54) // Soft minty vs rich emerald teal
    }

val TextPrimary: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF000000)
    } else {
        if (isDarkThemeGlobal) Color(0xFFE6E1E5) else Color(0xFF1C1B1F) // Refined off-white vs deep slate gray
    }

val TextSecondary: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFEEEEEE) else Color(0xFF111111)
    } else {
        if (isDarkThemeGlobal) Color(0xFFCAC4D0) else Color(0xFF49454F) // Muted off-white/gray vs soft charcoal gray
    }

val BorderHighlight: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF000000) // stark black and white outlines
    } else {
        if (isDarkThemeGlobal) Color(0xFF49454F) else Color(0xFFE1DDF5) // Muted edge line vs light subtle edge line
    }

val ShadowTint: Color
    get() = if (isDarkThemeGlobal) Color(0xFFD0BCFF) else Color(0xFF6750A4)

val GlassWhite: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0x33FFFFFF) else Color(0x33000000)
    } else {
        if (isDarkThemeGlobal) Color(0x0CFFFFFF) else Color(0x0C000000)
    }

val SoftGray: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF1E1E1E) else Color(0xFFE0E0E0)
    } else {
        if (isDarkThemeGlobal) Color(0xFF25232A) else Color(0xFFF5F2F8)
    }
