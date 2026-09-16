package com.example.ui.appearance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.Haptic
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.rememberHaptics

/**
 * Settings & Appearance Screen Composable.
 * Allows the user to select theme (Dark/Light/System), reduce motion, and force use of system font size.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = viewModel()
) {
    val themePreference by viewModel.themeState.collectAsState()
    val reduceMotionPreference by viewModel.reduceMotionState.collectAsState()
    val useSystemFontSizePreference by viewModel.fontSizeState.collectAsState()
    val colorScheme = MaterialTheme.colorScheme
    val haptics = rememberHaptics()

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Settings & Appearance",
                        color = colorScheme.onBackground,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        haptics(Haptic.Click)
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorScheme.background,
                    scrolledContainerColor = colorScheme.background
                ),
                modifier = Modifier.statusBarsPadding()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section 1: Brand Info Header
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Aesthetic Settings",
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Tailor the visual, motion, and spacing dimensions of Disha to your likeness.",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Section 2: Theme Selector
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Theme Selection",
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
                Text(
                    text = "Control the visual palette. Choosing 'System' will follow your device's global style.",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeOptionCard(
                        title = "Dark",
                        isSelected = themePreference == "Dark",
                        onSelect = { viewModel.setTheme("Dark") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        title = "Light",
                        isSelected = themePreference == "Light",
                        onSelect = { viewModel.setTheme("Light") },
                        modifier = Modifier.weight(1f)
                    )
                    ThemeOptionCard(
                        title = "System",
                        isSelected = themePreference == "System",
                        onSelect = { viewModel.setTheme("System") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Section 3: Premium Dynamic Toggles
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reduce Motion Toggle
                TogglePreferenceItem(
                    title = "Reduce Motion",
                    description = "Minimizes sliding and fading velocities inside local screens for eye safety.",
                    checked = reduceMotionPreference,
                    icon = Icons.Default.Settings,
                    onCheckedChange = { viewModel.setReduceMotion(it) }
                )

                // Font Size Toggle
                TogglePreferenceItem(
                    title = "Use system font size",
                    description = "Follows Android System Accessibility settings. If disabled, fonts scale at default dimensions.",
                    checked = useSystemFontSizePreference,
                    icon = Icons.Default.Info,
                    onCheckedChange = { viewModel.setUseSystemFontSize(it) }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Brand Stamp Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Disha Settings v1.0.0 • Offline-First",
                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun ThemeOptionCard(
    title: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = rememberHaptics()
    val containerColor = if (isSelected) colorScheme.surface else Color.Transparent
    val borderColor = if (isSelected) colorScheme.primary else colorScheme.outlineVariant
    val textColor = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant
    val icon = when (title) {
        "Dark" -> Icons.Default.DarkMode
        "Light" -> Icons.Default.LightMode
        else -> Icons.Default.Brightness6
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .clickable {
                if (!isSelected) haptics(Haptic.Click)
                onSelect()
            }
            .height(84.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun TogglePreferenceItem(
    title: String,
    description: String,
    checked: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCheckedChange: (Boolean) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = rememberHaptics()
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorScheme.background, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        color = colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = description,
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Switch(
                checked = checked,
                onCheckedChange = {
                    haptics(Haptic.Toggle)
                    onCheckedChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = colorScheme.onPrimary,
                    checkedTrackColor = colorScheme.primary,
                    uncheckedThumbColor = colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = colorScheme.background,
                    uncheckedBorderColor = colorScheme.outlineVariant
                )
            )
        }
    }
}
