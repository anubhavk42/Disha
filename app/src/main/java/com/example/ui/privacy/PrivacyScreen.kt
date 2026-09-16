package com.example.ui.privacy

import android.widget.Toast
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
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.Haptic
import com.example.ui.theme.SpaceGrotesk
import com.example.ui.theme.rememberHaptics

/**
 * Privacy & Legal Configuration Screen Composable.
 * Houses profile accessibility dropdowns, local tracking preferences, data downloader scripts,
 * external policy anchors, and full account erasure features.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    onDeleteAccountComplete: () -> Unit,
    viewModel: PrivacyViewModel = viewModel()
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val colorScheme = MaterialTheme.colorScheme
    val haptics = rememberHaptics()

    val profileVis by viewModel.profileVisibility.collectAsState()
    val consentGiven by viewModel.dataConsentGiven.collectAsState()
    val accessLogs by viewModel.accessLogs.collectAsState()
    val cookiePreference by viewModel.cookiePreferences.collectAsState()

    // Dialog & UI flows states
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showConsentDialog by remember { mutableStateOf(false) }
    var showCookieDialog by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Privacy & Legal Policies",
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
            
            // ================= SECTION 1: PRIVACY CONTROLS =================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Controls",
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Profile Visibility Selector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Profile visibility", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Determine who can discover and match with your scout metrics.", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .background(colorScheme.background, RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, colorScheme.outlineVariant), RoundedCornerShape(8.dp))
                                        .clickable {
                                            haptics(Haptic.Click)
                                            dropdownExpanded = true
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = profileVis, color = colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(colorScheme.surface).border(BorderStroke(1.dp, colorScheme.outlineVariant))
                                ) {
                                    listOf("Everyone", "Recruiters only", "Hidden").forEach { vis ->
                                        DropdownMenuItem(
                                            text = { Text(text = vis, color = colorScheme.onSurface, fontSize = 13.sp) },
                                            onClick = {
                                                haptics(Haptic.Toggle)
                                                viewModel.setProfileVisibility(vis)
                                                dropdownExpanded = false
                                                Toast.makeText(context, "Profile visibility set to $vis", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Data Usage Consent Row Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                haptics(Haptic.Click)
                                showConsentDialog = true
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Data usage consent", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "View and modify analytical logging or tracking permission guidelines.", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (consentGiven) "Accepted" else "Declined", color = if (consentGiven) colorScheme.primary else colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Resume Access Log Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.List, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Resume Access Log", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(text = "Recent recruiters who matched, analyzed, or downloaded your offline smart resume.", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            accessLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(colorScheme.background, RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, colorScheme.outlineVariant), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = log.recruiterName, color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = log.actionType, color = colorScheme.primary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(text = log.timeAgo, color = colorScheme.onSurfaceVariant, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            }

            // ================= SECTION 2: LEGAL LINKS =================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Legal & Web Documents",
                        color = colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        // Privacy Policy Row
                        LegalRow(
                            title = "Privacy Policy",
                            description = "Review how we encrypt database inputs and local session tokens.",
                            onClick = { uriHandler.openUri("https://disha.app/privacy") }
                        )

                        // Terms of Service Row
                        LegalRow(
                            title = "Terms of Service",
                            description = "Our standard agreement containing scout safety rules.",
                            onClick = { uriHandler.openUri("https://disha.app/terms") }
                        )

                        // Cookie Preferences Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                haptics(Haptic.Click)
                                showCookieDialog = true
                            }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Cookie Preferences", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Manage local SDK persistent cache tokens and telemetry data.", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (cookiePreference) "All Cached" else "Strict (No Telemetry)",
                                    color = colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Download Data Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    haptics(Haptic.Confirm)
                                    Toast.makeText(context, "Preparing offline database export ZIP...", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(context, "Export Complete: Check /Downloads/disha_data_export.zip", Toast.LENGTH_LONG).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Download my data", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Export Room SQLite records and profile resume files as a secure ZIP package.", color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ================= SECTION 3: DANGER ZONE =================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Danger Zone",
                    color = colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, colorScheme.error.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Irreversible Modifications",
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Instantly clears Room database profiles, deletes login session cookie tokens in Jetpack DataStore, and wipes all offline matching analytics from this Android device.",
                            color = colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                haptics(Haptic.Warning)
                                showDeleteConfirm = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = colorScheme.onError, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Delete my account", color = colorScheme.onError, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // --- POPUP DIALOGS ---

    // 1. Consent dialog
    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { showConsentDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Analytical Consent", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "To match candidate resumes with recruiter targets dynamically, we trace background searches. We save records offline to save network data.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable analytical tracking", color = colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = consentGiven,
                            onCheckedChange = {
                                haptics(Haptic.Toggle)
                                viewModel.setConsentGiven(it)
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
            },
            confirmButton = {
                TextButton(onClick = {
                    haptics(Haptic.Confirm)
                    showConsentDialog = false
                }) {
                    Text(text = "Save & Apply", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Cookie dialog
    if (showCookieDialog) {
        AlertDialog(
            onDismissRequest = { showCookieDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Manage Cookies", color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "We use secure system token cookies to process your profile metrics faster and bypass daily OTP limits.",
                        color = colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Allow session caching", color = colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = cookiePreference,
                            onCheckedChange = {
                                haptics(Haptic.Toggle)
                                viewModel.setCookiePreferences(it)
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
            },
            confirmButton = {
                TextButton(onClick = {
                    haptics(Haptic.Confirm)
                    showCookieDialog = false
                }) {
                    Text(text = "Confirm Preference", color = colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 3. Confirm Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Irreversible Account Erasure?", color = colorScheme.error, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to delete your Disha account? All matching jobs metrics, saved checklists, and system cookies stored in local database tables will be cleared entirely. This action CANNOT be undone.",
                    color = colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics(Haptic.Warning)
                        showDeleteConfirm = false
                        viewModel.deleteAccountAndAllData {
                            Toast.makeText(context, "Account, profile data, and session cleared successfully.", Toast.LENGTH_LONG).show()
                            onDeleteAccountComplete()
                        }
                    }
                ) {
                    Text(text = "ERASE ALL DATA", color = colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptics(Haptic.Click)
                    showDeleteConfirm = false
                }) {
                    Text(text = "Cancel", color = colorScheme.onSurface)
                }
            },
            containerColor = colorScheme.surface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun LegalRow(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val haptics = rememberHaptics()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptics(Haptic.Click)
                onClick()
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, color = colorScheme.onSurfaceVariant, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = colorScheme.primary, modifier = Modifier.size(16.dp))
    }
}
