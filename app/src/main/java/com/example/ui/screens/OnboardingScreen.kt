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

@Composable
fun OnboardingWelcomeScreen(onGetStarted: () -> Unit) {
    Scaffold(
        containerColor = SpaceBlack,
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .drawBehind {
                    // Draw beautiful radiant gradient spotlights on our dark canvas
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ElectricTeal.copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(size.width * 0.2f, size.height * 0.2f),
                            radius = size.width
                        )
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(SunsetOrange.copy(alpha = 0.1f), Color.Transparent),
                            center = Offset(size.width * 0.8f, size.height * 0.8f),
                            radius = size.width
                        )
                    )
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Specialized geometric Compass Logo "D"
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(DeepTeal)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(70.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.25f, size.height * 0.15f)
                            lineTo(size.width * 0.75f, size.height * 0.25f)
                            lineTo(size.width * 0.65f, size.height * 0.75f)
                            lineTo(size.width * 0.45f, size.height * 0.55f)
                            close()
                        }
                        // Beautiful glowing overlapping triangle path for compass arrowhead D symbol
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(listOf(ElectricTeal, SunsetOrange)),
                            style = Stroke(width = 8f)
                        )
                        drawCircle(
                            color = SeafoamMint,
                            radius = 6f,
                            center = Offset(size.width * 0.45f, size.height * 0.55f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Disha",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = (-1).sp
                )
            }

            // Central Slogans
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Your AI scout\nfor better roles",
                    color = TextPrimary,
                    fontSize = 42.sp,
                    lineHeight = 48.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    letterSpacing = (-1).sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Trust your career to AI.\nPrivate and secure.",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )
            }

            // Gradient pill action triggers
            CustomButton(
                text = "Get Started",
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_started_button")
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OnboardingConsentScreen(onConsent: () -> Unit, onCancel: () -> Unit) {
    Scaffold(containerColor = SpaceBlack, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(8.dp),
                    fontSize = 16.sp
                )
                Text(
                    text = "Privacy & Consent",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(48.dp)) // Equal spacing balance
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = "How we handle your data:",
                            color = TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        val items = listOf(
                            "Your CV data is secure and only shared with companies you approve.",
                            "Our AI matches you to jobs, but you are always in full control of applications.",
                            "We prioritize your privacy and will never sell your data.",
                            "You can manage your data and permissions at any time."
                        )

                        items.forEach { bullet ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Check",
                                    tint = SeafoamMint,
                                    modifier = Modifier
                                        .padding(top = 2.dp, end = 12.dp)
                                        .size(20.dp)
                                )
                                Text(
                                    text = bullet,
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            CustomButton(
                text = "I Consent",
                onClick = onConsent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("i_consent_button")
            )
        }
    }
}

@Composable
fun OnboardingProfileSetupScreen(viewModel: DishaViewModel, onContinue: () -> Unit, onBack: () -> Unit) {
    val activeSession by viewModel.activeSession.collectAsState()
    val userName = activeSession?.name ?: "Anubhav Kapoor"
    val designation = activeSession?.designation ?: "Product · Growth"
    val hasUploaded = activeSession?.hasUploadedResume ?: false

    Scaffold(containerColor = SpaceBlack, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Step 3 of 3",
                    color = ElectricTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            // User Identity Presentation Banner
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(ElectricTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userName,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = designation,
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }

            // Upload Box Area (Dotted borders simulation)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(DeepTeal)
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = if (hasUploaded) SeafoamMint else ElectricTeal
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .clickable { viewModel.uploadResumeAndProgress("Anubhav_Kapoor_Resume.pdf") }
                    .testTag("upload_resume_box"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = if (hasUploaded) Icons.Default.CheckCircle else Icons.Default.Lock,
                        contentDescription = "Upload",
                        tint = if (hasUploaded) SeafoamMint else ElectricTeal,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (hasUploaded) "Resume_Uploaded.pdf" else "Upload Resume",
                        color = if (hasUploaded) SeafoamMint else TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (hasUploaded) "Tap to re-upload files or documents" else "Drag and drop or browse files\n(PDF, DOCX, TXT, RTF)",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Your data is private and secure.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.W300
                    )
                }
            }

            CustomButton(
                text = "Save & Continue",
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_continue_button")
            )
        }
    }
}

@Composable
fun OnboardingAlertsScreen(viewModel: DishaViewModel, onComplete: () -> Unit, onSkip: () -> Unit) {
    val activeSession by viewModel.activeSession.collectAsState()
    val dailyDigest = activeSession?.dailyDigest ?: true
    val secureBiometrics = activeSession?.secureBiometrics ?: false

    Scaffold(containerColor = SpaceBlack, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    modifier = Modifier.clickable { onSkip() }
                )
                Text(
                    text = "Alerts & Security Setup",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp),
                verticalArrangement = Arrangement.Top
            ) {
                Text(
                    text = "Configure matching notifications and biometrics security options.",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Daily Digest Toggle Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Daily Digest", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Get a daily summary of relevant job matches delivered to your inbox.", color = TextSecondary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = dailyDigest,
                            onCheckedChange = { viewModel.updateToggleDigest(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SeafoamMint, checkedTrackColor = ElectricTeal)
                        )
                    }
                }

                // Biometrics Security Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Secure with Biometrics", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Enable FaceID or TouchID for quick and secure access to your account.", color = TextSecondary, fontSize = 14.sp)
                        }
                        Switch(
                            checked = secureBiometrics,
                            onCheckedChange = { viewModel.updateToggleBiometrics(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = SeafoamMint, checkedTrackColor = ElectricTeal)
                        )
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                CustomButton(
                    text = "Complete Config",
                    onClick = onComplete,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("complete_setup_button")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onSkip,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text(text = "Skip for now", color = TextSecondary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun OnboardingSuccessScreen(onGoDashboard: () -> Unit) {
    Scaffold(containerColor = SpaceBlack, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Giant Animated Success Checkmark
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(CardTeal)
                        .border(1.dp, BorderHighlight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = SeafoamMint,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = "Ready to Scout!",
                    color = TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your profile is all set. Let Disha find your perfect job matches.",
                    color = TextSecondary,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            CustomButton(
                text = "Go to Dashboard",
                onClick = onGoDashboard,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("submit_button")
            )
        }
    }
}
