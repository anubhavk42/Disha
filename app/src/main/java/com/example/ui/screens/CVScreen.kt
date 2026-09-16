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
private fun CvSectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(imageVector = icon, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 22.sp)
    }
}

@Composable
fun AppCVScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val resumeName = activeSession?.resumeFileName ?: "Not Uploaded"
    val hasResume = activeSession?.hasUploadedResume ?: false

    // Dialog & Store Submission preference states
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Referral state
    var referralInput by remember { mutableStateOf("") }
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
                        text = "Smart CV & Documents Hub",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGrotesk,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Your profile, resume, and referrals in one place.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // ================= SECTION: MY RESUME =================
            item {
                CvSectionHeader(icon = Icons.Default.Lock, title = "My Resume")
            }

            // Uploaded Smart CV
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (hasResume) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (hasResume) SeafoamMint else SunsetOrange,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (hasResume) "Current Document: $resumeName" else "No Active Resume on Scout File",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                haptics(Haptic.Confirm)
                                viewModel.uploadResumeAndProgress("Anubhav_Kapoor_NewCV.pdf")
                                Toast.makeText(context, "Smart resume updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (hasResume) "Replace Resume Blueprint" else "Upload Scout Resume File", color = Color.White)
                        }
                    }
                }
            }

            // Resume & cover letter pricing (part of My Resume section)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Resume & Cover Letter Services", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "Professional resume writing and tailored cover letters for Indian tech hubs.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price table
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(CardTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Professional Resume PDF", color = TextPrimary, fontSize = 14.sp)
                                Text(text = "₹49", color = SeafoamMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tailored Cover Letter Prompt", color = TextPrimary, fontSize = 14.sp)
                                Text(text = "₹49", color = SeafoamMint, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Divider(color = BorderHighlight.copy(alpha = 0.5f), thickness = 1.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subscription Fees", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "₹0 (No Hidden Fees)", color = SeafoamMint, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // ================= SECTION: REFER & EARN =================
            item {
                CvSectionHeader(icon = Icons.Default.Share, title = "Refer & Earn")
            }

            // Referral Program Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Refer a friend and get 1 free professional resume. Credits are added instantly.", color = TextSecondary, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        val count = activeSession?.referralsCount ?: 0
                        Text(text = "Referrals Made: $count ($count Resumes Earned Free)", color = SeafoamMint, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CustomTextField(
                                value = referralInput,
                                onValueChange = { referralInput = it },
                                placeholder = "Friend's phone or email",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (referralInput.trim().isEmpty()) {
                                        haptics(Haptic.Reject)
                                        Toast.makeText(context, "Enter contact to refer", Toast.LENGTH_SHORT).show()
                                    } else {
                                        haptics(Haptic.Confirm)
                                        viewModel.inviteReferral(referralInput)
                                        Toast.makeText(context, "Successfully referred $referralInput! +1 Smart Resume Earned Free!", Toast.LENGTH_LONG).show()
                                        referralInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(text = "Refer & Earn", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // ================= SECTION: MY PROFILE =================
            item {
                CvSectionHeader(icon = Icons.Default.Info, title = "My Profile")
            }

            // Policies & account data
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Review our policies or manage your account data.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Policies
                        Text(text = "Our Policies", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Read how we handle your data and what you're agreeing to.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptics(Haptic.Click)
                                    showPrivacyDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardTeal),
                                border = BorderStroke(1.dp, BorderHighlight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Privacy Policy", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    haptics(Haptic.Click)
                                    showTermsDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CardTeal),
                                border = BorderStroke(1.dp, BorderHighlight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Terms of Service", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Account & Data Deletion
                        Text(text = "Delete My Account & Data", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "You can permanently delete your account and all your data at any time.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                haptics(Haptic.Warning)
                                showDeleteConfirmDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                            border = BorderStroke(1.dp, BorderHighlight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Request Account & Data Deletion", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
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

    // Material 3 Dialog Dialogs
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(text = "Privacy Policy", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Last updated: June 2026\n\n" +
                                "Disha values your privacy. Your saved job preferences, search history, and session details are stored locally on your device.\n\n" +
                                "1. Information We Collect:\n" +
                                "- Profile data (name, email, phone, bio/designation) to personalize your matches.\n" +
                                "- Your uploaded resume text, used only to help tailor your applications.\n\n" +
                                "2. Google Sheets Sync:\n" +
                                "If you turn on Google Sheets Sync, your matching jobs are written directly and securely to the spreadsheet you choose. We never sell your data or share it with unapproved third parties.\n\n" +
                                "3. Security & Safety:\n" +
                                "To keep your account safe, you can enable biometric login (fingerprint/face unlock).\n\n" +
                                "For any questions, or to delete your data, go to your Profile settings.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptics(Haptic.Click)
                    showPrivacyDialog = false
                }) {
                    Text(text = "I Understand", color = ElectricTeal, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepTeal,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = {
                Text(text = "Terms of Service", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "Last updated: June 2026\n\n" +
                                "By using Disha (\"the App\" or \"our services\"), you agree to follow these Terms:\n\n" +
                                "1. Proper Use:\n" +
                                "Our job alerts, resume tools, and referral codes are designed to help your job search. Misusing or tampering with these features is a violation of these terms.\n\n" +
                                "2. Payment and Premium Features:\n" +
                                "Individual custom resume PDFs and cover letters are billed clearly (₹49) with transparency. We adhere to standard consumer regulatory guidelines under Indian law.\n\n" +
                                "3. Compliance & Fair Play:\n" +
                                "You confirm that all profile details (such as target salary, location preferences, name) are accurate.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    haptics(Haptic.Confirm)
                    showTermsDialog = false
                }) {
                    Text(text = "Accept", color = ElectricTeal, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepTeal,
            shape = RoundedCornerShape(20.dp)
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(text = "Confirm Account & Data Deletion?", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Text(
                    text = "This will instantly delete your profile, your uploaded resumes, and all your saved job matches. This action is final and cannot be undone.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        haptics(Haptic.Warning)
                        showDeleteConfirmDialog = false
                        viewModel.logout() // Clear active session & DB
                        Toast.makeText(context, "Your account and all your data have been deleted.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(text = "ERASE ALL DATA", color = SunsetOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    haptics(Haptic.Click)
                    showDeleteConfirmDialog = false
                }) {
                    Text(text = "Cancel", color = TextPrimary)
                }
            },
            containerColor = DeepTeal,
            shape = RoundedCornerShape(20.dp)
        )
    }
}
