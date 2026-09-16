package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.widget.Toast
import kotlinx.coroutines.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.JobMatch
import com.example.ui.DishaViewModel
import com.example.ui.theme.*
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.HazeState

@Composable
fun AppProfileScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val isHighContrast by viewModel.isHighContrastMode.collectAsState()

    // Profile input states
    var editName by remember(activeSession) { mutableStateOf(activeSession?.name ?: "") }
    var editDesignation by remember(activeSession) { mutableStateOf(activeSession?.designation ?: "") }
    var editEmail by remember(activeSession) { mutableStateOf(activeSession?.email ?: "") }
    var editMobile by remember(activeSession) { mutableStateOf(activeSession?.mobile ?: "") }
    val hazeState = remember { HazeState() }
    val haptics = rememberHaptics()

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = SpaceBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = bottomNavBarClearance())
        ) {
            // Title
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "My Scout Profile",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Manage your scout identity details, custom styling themes, and privacy preferences.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Basic Information Editing Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ElectricTeal)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Edit Scout Information", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }

                        // Name
                        Text(text = "Full Name", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        CustomTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            placeholder = "Enter full name",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Designation
                        Text(text = "Designation / Role Interest", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        CustomTextField(
                            value = editDesignation,
                            onValueChange = { editDesignation = it },
                            placeholder = "e.g. Lead Designer",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Email
                        Text(text = "Email Address", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        CustomTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            placeholder = "Enter email address",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Phone
                        Text(text = "Mobile Phone", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        CustomTextField(
                            value = editMobile,
                            onValueChange = { editMobile = it },
                            placeholder = "Enter mobile number",
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                if (editName.isBlank()) {
                                    haptics(Haptic.Reject)
                                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                                } else {
                                    haptics(Haptic.Confirm)
                                    viewModel.updateProfile(editName, editDesignation, editEmail, editMobile)
                                    Toast.makeText(context, "Basic Information Updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "Save Profile Details", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // High Contrast Mode Switch Row
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ElectricTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    color = ElectricTeal,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "High Contrast Mode", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = if (isHighContrast) "Stark accessibility borders & pure solid background contrast" else "Standard comfortable translucent color styling", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        Switch(
                            checked = isHighContrast,
                            onCheckedChange = {
                                haptics(Haptic.Toggle)
                                viewModel.toggleHighContrastMode()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SeafoamMint,
                                checkedTrackColor = ElectricTeal
                            )
                        )
                    }
                }
            }

            // Listing Sources (job platform integrations)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Listing Sources", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(text = "Choose platforms to pull roles from dynamically", color = TextSecondary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(16.dp))

                        val platforms = listOf(
                            Triple("LinkedIn", activeSession?.linkedinConnected ?: true, "linkedin"),
                            Triple("Indeed", activeSession?.indeedConnected ?: false, "indeed"),
                            Triple("Naukri.com", activeSession?.naukriConnected ?: true, "naukri"),
                            Triple("Daily Digest Notification Alerts", activeSession?.dailyDigest ?: true, "daily_digest")
                        )

                        platforms.forEach { (name, connected, key) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Switch(
                                    checked = connected,
                                    onCheckedChange = {
                                        haptics(Haptic.Toggle)
                                        if (key == "daily_digest") {
                                            viewModel.updateToggleDigest(it)
                                        } else {
                                            viewModel.updatePlatformConnection(key, it)
                                        }
                                    },
                                    colors = SwitchDefaults.colors(checkedThumbColor = SeafoamMint, checkedTrackColor = ElectricTeal)
                                )
                            }
                        }
                    }
                }
            }

            // Disha Preferences & Settings Navigation Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Disha Preferences & Settings",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Manage your app appearance, referral rewards, and account security.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // 1. Appearance Screen Route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable {
                                    haptics(Haptic.Click)
                                    onNavigateToTab(DishaScreen.Appearance)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Theme & Appearance Settings", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Refer Screen Route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable {
                                    haptics(Haptic.Click)
                                    onNavigateToTab(DishaScreen.ReferEarn)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Refer & Earn Pro Benefits", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 3. Privacy Screen Route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable {
                                    haptics(Haptic.Click)
                                    onNavigateToTab(DishaScreen.Privacy)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = SeafoamMint, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(text = "Privacy & Account Deletion", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Logout
            item {
                Button(
                    onClick = {
                        haptics(Haptic.Warning)
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(30.dp))
                ) {
                    Text(text = "Log Out from Disha", color = SunsetOrange, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
    AppBottomNavigationBar(
        currentTab = currentTab,
        onSelectTab = { onNavigateToTab(it) },
        hazeState = hazeState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }
}
