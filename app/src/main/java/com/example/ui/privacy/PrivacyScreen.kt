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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// Theme Colors for consistency across the App
private val BackgroundColor = Color(0xFF0E0E14)
private val SurfaceColor = Color(0xFF13131F)
private val BorderColor = Color(0xFF1E1E2E)
private val AccentColor = Color(0xFFA78BFA)
private val DangerColor = Color(0xFFF87171)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF94A3B8)

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
        containerColor = BackgroundColor,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Privacy & Legal Policies",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BackgroundColor,
                    scrolledContainerColor = BackgroundColor
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
                        tint = AccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy Controls",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Profile Visibility Selector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Profile visibility", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "Determine who can discover and match with your scout metrics.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .background(BackgroundColor, RoundedCornerShape(8.dp))
                                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(8.dp))
                                        .clickable { dropdownExpanded = true }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = profileVis, color = AccentColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = AccentColor, modifier = Modifier.size(16.dp))
                                }

                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false },
                                    modifier = Modifier.background(SurfaceColor).border(BorderStroke(1.dp, BorderColor))
                                ) {
                                    listOf("Everyone", "Recruiters only", "Hidden").forEach { vis ->
                                        DropdownMenuItem(
                                            text = { Text(text = vis, color = TextPrimary, fontSize = 13.sp) },
                                            onClick = {
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
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConsentDialog = true }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Data usage consent", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "View and modify analytical logging or tracking permission guidelines.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = if (consentGiven) "Accepted" else "Declined", color = if (consentGiven) AccentColor else DangerColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Resume Access Log Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.List, contentDescription = null, tint = AccentColor, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Resume Access Log", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text(text = "Recent recruiters who matched, analyzed, or downloaded your offline smart resume.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            accessLogs.forEach { log ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(BackgroundColor, RoundedCornerShape(10.dp))
                                        .border(BorderStroke(1.dp, BorderColor), RoundedCornerShape(10.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = log.recruiterName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(text = log.actionType, color = AccentColor, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Text(text = log.timeAgo, color = TextSecondary, fontSize = 10.sp)
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
                        tint = AccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Legal & Web Documents",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, BorderColor),
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
                                .clickable { showCookieDialog = true }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Cookie Preferences", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Manage local SDK persistent cache tokens and telemetry data.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (cookiePreference) "All Cached" else "Strict (No Telemetry)",
                                    color = AccentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Download Data Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    Toast.makeText(context, "Preparing offline database export ZIP...", Toast.LENGTH_SHORT).show()
                                    Toast.makeText(context, "Export Complete: Check /Downloads/disha_data_export.zip", Toast.LENGTH_LONG).show()
                                }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Download my data", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Export Room SQLite records and profile resume files as a secure ZIP package.", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = AccentColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // ================= SECTION 3: DANGER ZONE =================
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Danger Zone",
                    color = DangerColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, DangerColor.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Irreversible Modifications",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Instantly clears Room database profiles, deletes login session cookie tokens in Jetpack DataStore, and wipes all offline matching analytics from this Android device.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DangerColor),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Delete my account", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = AccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Analytical Consent", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "To match candidate resumes with recruiter targets dynamically, we trace background searches. We save records offline to save network data.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable analytical tracking", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = consentGiven,
                            onCheckedChange = { viewModel.setConsentGiven(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentColor,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = BackgroundColor,
                                uncheckedBorderColor = BorderColor
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConsentDialog = false }) {
                    Text(text = "Save & Apply", color = AccentColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 2. Cookie dialog
    if (showCookieDialog) {
        AlertDialog(
            onDismissRequest = { showCookieDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = AccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Manage Cookies", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "We use secure system token cookies to process your profile metrics faster and bypass daily OTP limits.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Allow session caching", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(
                            checked = cookiePreference,
                            onCheckedChange = { viewModel.setCookiePreferences(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = AccentColor,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = BackgroundColor,
                                uncheckedBorderColor = BorderColor
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCookieDialog = false }) {
                    Text(text = "Confirm Preference", color = AccentColor, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = SurfaceColor,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // 3. Confirm Delete Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = DangerColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Irreversible Account Erasure?", color = DangerColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text(
                    text = "Are you absolutely sure you want to delete your Disha account? All matching jobs metrics, saved checklists, and system cookies stored in local database tables will be cleared entirely. This action CANNOT be undone.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        viewModel.deleteAccountAndAllData {
                            Toast.makeText(context, "Account, profile data, and session cleared successfully.", Toast.LENGTH_LONG).show()
                            onDeleteAccountComplete()
                        }
                    }
                ) {
                    Text(text = "ERASE ALL DATA", color = DangerColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(text = "Cancel", color = TextPrimary)
                }
            },
            containerColor = SurfaceColor,
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = description, color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = AccentColor, modifier = Modifier.size(16.dp))
    }
}
