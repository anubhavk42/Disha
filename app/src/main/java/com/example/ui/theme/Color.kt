package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Global theme state for dynamic color resolution
var isDarkThemeGlobal: Boolean = true
var isHighContrastThemeGlobal: Boolean = false

// Disha Brand Palette - Deep Teal (dynamic getters for Dark and Light mode)
val SpaceBlack: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFFFFFFF)
    } else {
        if (isDarkThemeGlobal) Color(0xFF0D1412) else Color(0xFFF6FAF9) // Cool near-black vs cool near-white
    }

val DeepTeal: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF000000) else Color(0xFFFFFFFF)
    } else {
        if (isDarkThemeGlobal) Color(0xFF16211D) else Color(0xFFEDF5F2) // Dark teal-black vs pale teal surface
    }

val CardTeal: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF121212) else Color(0xFFF5F5F5)
    } else {
        if (isDarkThemeGlobal) Color(0xFF1E322B) else Color(0xFFDCEBE6) // Dark teal container vs light teal container
    }

val ElectricTeal: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF2BEEC4) else Color(0xFF085E4C) // Same brand teal hue, higher saturation for WCAG AA/AAA contrast
    } else {
        if (isDarkThemeGlobal) Color(0xFF5FD3B8) else Color(0xFF166B5A) // Bright mint-teal vs deep confident teal
    }

val SunsetOrange: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFF5252) else Color(0xFFD50000) // Bright red vs deep crimson
    } else {
        if (isDarkThemeGlobal) Color(0xFFF2B08C) else Color(0xFFB2532F) // Soft coral vs deep terracotta accent
    }

val SeafoamMint: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF00FF00) else Color(0xFF007A00) // Neon green vs dark green
    } else {
        if (isDarkThemeGlobal) Color(0xFF8FF0C7) else Color(0xFF0F6B45) // Bright mint vs deep forest-mint tertiary
    }

val TextPrimary: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF000000)
    } else {
        if (isDarkThemeGlobal) Color(0xFFE3EDE9) else Color(0xFF10201C) // Cool off-white vs deep cool slate
    }

val TextSecondary: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFEEEEEE) else Color(0xFF111111)
    } else {
        if (isDarkThemeGlobal) Color(0xFFB9C9C3) else Color(0xFF49605A) // Muted cool gray-green vs soft teal-charcoal
    }

val BorderHighlight: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFFFFFFF) else Color(0xFF000000) // stark black and white outlines
    } else {
        if (isDarkThemeGlobal) Color(0xFF4F625C) else Color(0xFFCFE3DD) // Muted teal edge line vs light teal edge line
    }

val ShadowTint: Color
    get() = if (isDarkThemeGlobal) Color(0xFF5FD3B8) else Color(0xFF166B5A)

val GlassWhite: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0x33FFFFFF) else Color(0x33000000)
    } else {
        if (isDarkThemeGlobal) Color(0x0CFFFFFF) else Color(0x0C000000)
    }

val PremiumGold: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFFE6A800) else Color(0xFF6B4E00) // Vivid amber vs deep bronze for strong contrast
    } else {
        if (isDarkThemeGlobal) Color(0xFFD9A404) else Color(0xFF8B6508) // Rich gold vs deep bronze-gold accent
    }

val SoftGray: Color
    get() = if (isHighContrastThemeGlobal) {
        if (isDarkThemeGlobal) Color(0xFF1E1E1E) else Color(0xFFE0E0E0)
    } else {
        if (isDarkThemeGlobal) Color(0xFF24332E) else Color(0xFFEFF7F4)
    }
