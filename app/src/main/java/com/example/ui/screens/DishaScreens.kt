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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.JobMatch
import com.example.ui.DishaViewModel
import com.example.ui.theme.*

// Root navigation levels
enum class DishaScreen {
    Welcome,
    Consent,
    Login,
    Otp,
    ProfileSetup,
    RefinePreferences,
    AlertsSetup,
    Success,
    Dashboard,
    CV,
    Automate,
    Assist,
    Profile,
    JobDetail,
    SendNote,
    Appearance,
    ReferEarn,
    Privacy
}

@Composable
fun MainNavigationContainer(
    viewModel: DishaViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(DishaScreen.Welcome) }
    var selectedJobId by remember { mutableStateOf<String?>(null) }
    val activeSession by viewModel.activeSession.collectAsState()

    // Screen history backstack
    val screenHistory = remember { mutableStateListOf<DishaScreen>() }

    val navigateTo: (DishaScreen) -> Unit = { targetScreen ->
        if (currentScreen != targetScreen) {
            screenHistory.add(currentScreen)
            currentScreen = targetScreen
        }
    }

    val navigateToTab: (DishaScreen) -> Unit = { targetTab ->
        if (currentScreen != targetTab) {
            screenHistory.clear()
            currentScreen = targetTab
        }
    }

    val navigateAndClearHistory: (DishaScreen) -> Unit = { targetScreen ->
        screenHistory.clear()
        currentScreen = targetScreen
    }

    val handleBackPress: () -> Unit = {
        if (screenHistory.isNotEmpty()) {
            val previousScreen = screenHistory.removeAt(screenHistory.lastIndex)
            currentScreen = previousScreen
        } else {
            // Fallback screen-specific back navigation if screen history is empty
            when (currentScreen) {
                DishaScreen.Consent -> currentScreen = DishaScreen.Welcome
                DishaScreen.Login -> currentScreen = DishaScreen.Consent
                DishaScreen.Otp -> currentScreen = DishaScreen.Login
                DishaScreen.ProfileSetup -> currentScreen = DishaScreen.Otp
                DishaScreen.RefinePreferences -> currentScreen = DishaScreen.ProfileSetup
                DishaScreen.AlertsSetup -> currentScreen = DishaScreen.RefinePreferences
                DishaScreen.CV, DishaScreen.Automate, DishaScreen.Assist, DishaScreen.Profile -> {
                    currentScreen = DishaScreen.Dashboard
                }
                DishaScreen.JobDetail -> currentScreen = DishaScreen.Dashboard
                DishaScreen.SendNote -> currentScreen = DishaScreen.JobDetail
                DishaScreen.Appearance, DishaScreen.ReferEarn, DishaScreen.Privacy -> {
                    currentScreen = DishaScreen.Profile
                }
                else -> {
                    // Let the system handle (exit app / close)
                }
            }
        }
    }

    // Capture system back press which is triggered from physical/system navigation keys
    val isBackHandlerEnabled = currentScreen != DishaScreen.Welcome && 
            (currentScreen != DishaScreen.Dashboard || screenHistory.isNotEmpty())
    BackHandler(enabled = isBackHandlerEnabled) {
        handleBackPress()
    }

    val dlJobId by viewModel.deepLinkJobId.collectAsState()
    val dlScreen by viewModel.deepLinkScreen.collectAsState()

    LaunchedEffect(dlJobId, dlScreen) {
        val screen = dlScreen
        if (screen != null) {
            navigateAndClearHistory(screen)
            if (dlJobId != null) {
                selectedJobId = dlJobId
            }
            viewModel.clearDeepLink()
        }
    }

    // Auto navigate to dashboard if setup is complete
    LaunchedEffect(activeSession) {
        if (activeSession?.isSetupComplete == true && currentScreen in listOf(DishaScreen.Welcome, DishaScreen.Consent, DishaScreen.Login)) {
            navigateAndClearHistory(DishaScreen.Dashboard)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBlack)
    ) {
        // Crossfade for smooth animations between pages
        Crossfade(
            targetState = currentScreen,
            animationSpec = tween(350),
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                DishaScreen.Welcome -> OnboardingWelcomeScreen(
                    onGetStarted = { navigateTo(DishaScreen.Consent) }
                )
                DishaScreen.Consent -> OnboardingConsentScreen(
                    onConsent = { navigateTo(DishaScreen.Login) },
                    onCancel = { handleBackPress() }
                )
                DishaScreen.Login -> OnboardingLoginScreen(
                    viewModel = viewModel,
                    onGetOtp = {
                        viewModel.requestOtp()
                        navigateTo(DishaScreen.Otp)
                    }
                )
                DishaScreen.Otp -> OnboardingOtpScreen(
                    viewModel = viewModel,
                    onVerify = {
                        viewModel.performFirebaseAndLocalLogin()
                        navigateTo(DishaScreen.ProfileSetup)
                    },
                    onBack = { handleBackPress() }
                )
                DishaScreen.ProfileSetup -> OnboardingProfileSetupScreen(
                    viewModel = viewModel,
                    onContinue = { navigateTo(DishaScreen.RefinePreferences) },
                    onBack = { handleBackPress() }
                )
                DishaScreen.RefinePreferences -> OnboardingRefineScreen(
                    viewModel = viewModel,
                    onDone = { navigateTo(DishaScreen.AlertsSetup) },
                    onCancel = { handleBackPress() }
                )
                DishaScreen.AlertsSetup -> OnboardingAlertsScreen(
                    viewModel = viewModel,
                    onComplete = {
                        viewModel.completeOnboardingAndSave()
                        navigateAndClearHistory(DishaScreen.Success)
                    },
                    onSkip = {
                        viewModel.completeOnboardingAndSave()
                        navigateAndClearHistory(DishaScreen.Success)
                    }
                )
                DishaScreen.Success -> OnboardingSuccessScreen(
                    onGoDashboard = { navigateAndClearHistory(DishaScreen.Dashboard) }
                )
                DishaScreen.Dashboard -> AppDashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { tab ->
                        navigateToTab(tab)
                    },
                    onJobClick = { jobId ->
                        selectedJobId = jobId
                        navigateTo(DishaScreen.JobDetail)
                    }
                )
                DishaScreen.CV -> AppCVScreen(
                    viewModel = viewModel,
                    currentTab = DishaScreen.CV,
                    onNavigateToTab = { tab ->
                        navigateToTab(tab)
                    }
                )
                DishaScreen.Automate -> AppAutomateScreen(
                    viewModel = viewModel,
                    currentTab = DishaScreen.Automate,
                    onNavigateToTab = { tab ->
                        navigateToTab(tab)
                    }
                )
                DishaScreen.Assist -> AppAssistScreen(
                    viewModel = viewModel,
                    currentTab = DishaScreen.Assist,
                    onNavigateToTab = { tab ->
                        navigateToTab(tab)
                    },
                    onJobClick = { jobId ->
                        selectedJobId = jobId
                        navigateTo(DishaScreen.JobDetail)
                    }
                )
                DishaScreen.Profile -> AppProfileScreen(
                    viewModel = viewModel,
                    currentTab = DishaScreen.Profile,
                    onNavigateToTab = { tab ->
                        navigateToTab(tab)
                    }
                )
                DishaScreen.JobDetail -> {
                    val jobId = selectedJobId ?: "job_1"
                    val jobsList by viewModel.jobMatches.collectAsState()
                    val job = jobsList.find { it.id == jobId } ?: jobsList.firstOrNull()
                    if (job != null) {
                        JobDetailScreen(
                            job = job,
                            viewModel = viewModel,
                            onBack = { handleBackPress() },
                            onToggleBookmark = { id, saved -> viewModel.toggleJobBookmark(id, saved) },
                            onUpdateStatus = { id, status -> viewModel.updateJobApplicationStatus(id, status) },
                            onSendNoteClick = { navigateTo(DishaScreen.SendNote) }
                        )
                    } else {
                        navigateAndClearHistory(DishaScreen.Dashboard)
                    }
                }
                DishaScreen.SendNote -> {
                    val jobId = selectedJobId ?: "job_1"
                    val job = viewModel.jobMatches.value.find { it.id == jobId }
                        ?: viewModel.jobMatches.value.firstOrNull()
                    SendNoteScreen(
                        jobTitle = job?.title ?: "Position",
                        companyName = job?.company ?: "Company",
                        recruiterName = job?.recruiterName ?: "Hiring Team",
                        onCancel = { handleBackPress() },
                        onSent = { handleBackPress() }
                    )
                }
                DishaScreen.Appearance -> {
                    com.example.ui.appearance.AppearanceScreen(
                        onBack = { handleBackPress() }
                    )
                }
                DishaScreen.ReferEarn -> {
                    com.example.ui.refer.ReferEarnScreen(
                        onBack = { handleBackPress() }
                    )
                }
                DishaScreen.Privacy -> {
                    com.example.ui.privacy.PrivacyScreen(
                        onBack = { handleBackPress() },
                        onDeleteAccountComplete = {
                            viewModel.logout()
                            navigateAndClearHistory(DishaScreen.Welcome)
                        }
                    )
                }
            }
        }
    }
}

// ======================= SCREEN DEFINITIONS =======================

@Composable
fun OnboardingWelcomeScreen(onGetStarted: () -> Unit) {
    Scaffold(
        containerColor = SpaceBlack,
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
                            colors = listOf(ElectricViolet.copy(alpha = 0.15f), Color.Transparent),
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
                        .background(DeepViolet)
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
                            brush = Brush.linearGradient(listOf(ElectricViolet, SunsetOrange)),
                            style = Stroke(width = 8f)
                        )
                        drawCircle(
                            color = MintyTeal,
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
    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                                    tint = MintyTeal,
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
fun OnboardingLoginScreen(viewModel: DishaViewModel, onGetOtp: () -> Unit) {
    val phone by viewModel.phoneInput.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val name by viewModel.userNameInput.collectAsState()
    var isGoogleLogin by remember { mutableStateOf(false) }

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Logo Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(DeepViolet)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(36.dp)) {
                        val path = Path().apply {
                            moveTo(size.width * 0.25f, size.height * 0.15f)
                            lineTo(size.width * 0.75f, size.height * 0.25f)
                            lineTo(size.width * 0.65f, size.height * 0.75f)
                            lineTo(size.width * 0.45f, size.height * 0.55f)
                            close()
                        }
                        drawPath(
                            path = path,
                            brush = Brush.linearGradient(listOf(ElectricViolet, SunsetOrange)),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = MintyTeal,
                            radius = 3f,
                            center = Offset(size.width * 0.45f, size.height * 0.55f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Disha",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Input Fields Form Block
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Full Name",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                CustomTextField(
                    value = name,
                    onValueChange = { viewModel.onUserNameChange(it) },
                    placeholder = "e.g., Anubhav Kapoor",
                    modifier = Modifier.testTag("name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Email Address",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                CustomTextField(
                    value = email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    placeholder = "e.g., anubhavk42@gmail.com",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.testTag("email_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Mobile Number",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Flag Sim
                    Box(
                        modifier = Modifier
                            .height(56.dp)
                            .background(DeepViolet, RoundedCornerShape(16.dp))
                            .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp))
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🇮🇳", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "+91", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    CustomTextField(
                        value = phone,
                        onValueChange = { viewModel.onPhoneChange(it) },
                        placeholder = "Enter mobile number",
                        keyboardType = KeyboardType.Phone,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("mobile_input")
                    )
                }
            }

            // Login Trigger Controls
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                CustomButton(
                    text = "Get OTP",
                    onClick = onGetOtp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("get_otp_button")
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.onUserNameChange("Anubhav Kapoor")
                        viewModel.onEmailChange("anubhavk42@gmail.com")
                        viewModel.onPhoneChange("9988776655")
                        isGoogleLogin = true
                        onGetOtp()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(30.dp))
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Continue with Google", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer
                Text(
                    text = "Terms of Service  •  Privacy Policy",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingOtpScreen(viewModel: DishaViewModel, onVerify: () -> Unit, onBack: () -> Unit) {
    val otp by viewModel.otpInput.collectAsState()
    val phone by viewModel.phoneInput.collectAsState()

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Step 2 of 3",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Titles
                Text(
                    text = "Secure Login OTP",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter the 6-digit code sent to\n+91 $phone",
                    color = TextSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                // OTP Box Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(6) { index ->
                        val char = if (otp.length > index) otp[index].toString() else ""
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DeepViolet)
                                .border(
                                    width = if (otp.length == index) 2.dp else 1.dp,
                                    color = if (otp.length == index) ElectricViolet else BorderHighlight,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = char,
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Simple hidden Textfield to receive characters
                Box(modifier = Modifier.height(0.dp)) {
                    BasicTextField(
                        value = otp,
                        onValueChange = { viewModel.onOtpChange(it) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Resend code in 24s",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }

            CustomButton(
                text = "Verify & Login",
                onClick = onVerify,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("verify_otp_button")
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

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                    color = ElectricViolet,
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
                        .background(ElectricViolet),
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
                    .background(DeepViolet)
                    .border(
                        BorderStroke(
                            width = 2.dp,
                            color = if (hasUploaded) MintyTeal else ElectricViolet
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
                        tint = if (hasUploaded) MintyTeal else ElectricViolet,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (hasUploaded) "Resume_Uploaded.pdf" else "Upload Resume",
                        color = if (hasUploaded) MintyTeal else TextPrimary,
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
fun OnboardingRefineScreen(viewModel: DishaViewModel, onDone: () -> Unit, onCancel: () -> Unit) {
    val rolesFlow by viewModel.preferenceRoles.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()
    var newRoleName by remember { mutableStateOf("") }
    var minVal by remember { mutableStateOf(12f) }
    var maxVal by remember { mutableStateOf(45f) }

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                    modifier = Modifier.clickable { onCancel() }
                )
                Text(
                    text = "Refine Your Match Preferences",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Done",
                    color = ElectricViolet,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onDone() }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                item {
                    Text(
                        text = "Desired Roles",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )

                    // Tags/Chips presentation Grid layout
                    FlowRowSim(
                        items = rolesFlow.map { it.roleName },
                        onRemove = { viewModel.removePreferenceRole(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextField(
                            value = newRoleName,
                            onValueChange = { newRoleName = it },
                            placeholder = "Enter new role title",
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newRoleName.trim().isNotEmpty()) {
                                    viewModel.addPreferenceRole(newRoleName.trim())
                                    newRoleName = ""
                                }
                            },
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ElectricViolet)
                                .testTag("add_role_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Location",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    CustomTextField(
                        value = location,
                        onValueChange = { viewModel.selectedLocation.value = it },
                        placeholder = "Enter city or region"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Salary Range (Annual)",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepViolet),
                        border = BorderStroke(1.dp, BorderHighlight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "₹${minVal.toInt()} LPA", color = TextPrimary, fontWeight = FontWeight.Bold)
                                Text(text = "₹${maxVal.toInt()} LPA+", color = TextPrimary, fontWeight = FontWeight.Bold)
                            }
                            RangeSlider(
                                value = minVal..maxVal,
                                onValueChange = { range ->
                                    minVal = range.start
                                    maxVal = range.endInclusive
                                },
                                valueRange = 5f..100f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = ElectricViolet,
                                    thumbColor = SunsetOrange
                                )
                            )
                        }
                    }
                }
            }

            CustomButton(
                text = "Save Preferences",
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_preferences_button")
            )
        }
    }
}

@Composable
fun OnboardingAlertsScreen(viewModel: DishaViewModel, onComplete: () -> Unit, onSkip: () -> Unit) {
    val activeSession by viewModel.activeSession.collectAsState()
    val dailyDigest = activeSession?.dailyDigest ?: true
    val secureBiometrics = activeSession?.secureBiometrics ?: false

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                            colors = SwitchDefaults.colors(checkedThumbColor = MintyTeal, checkedTrackColor = ElectricViolet)
                        )
                    }
                }

                // Biometrics Security Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                            colors = SwitchDefaults.colors(checkedThumbColor = MintyTeal, checkedTrackColor = ElectricViolet)
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
    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                        .background(CardViolet)
                        .border(1.dp, BorderHighlight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MintyTeal,
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

fun getSalaryLpa(salaryRange: String): Pair<Int, Int> {
    val cleaned = salaryRange.replace(",", "").replace(" ", "").uppercase()
    return try {
        if (cleaned.contains("LPA")) {
            val parts = cleaned.replace("LPA", "").split("-")
            val min = parts.getOrNull(0)?.toIntOrNull() ?: 5
            val max = parts.getOrNull(1)?.toIntOrNull() ?: 100
            Pair(min, max)
        } else if (cleaned.contains("L") && !cleaned.contains("LPA") && cleaned.contains("-")) {
            val parts = cleaned.replace("₹", "").replace("L", "").split("-")
            val min = parts.getOrNull(0)?.toIntOrNull() ?: 5
            val max = parts.getOrNull(1)?.toIntOrNull() ?: 100
            Pair(min, max)
        } else if (cleaned.contains("00000")) {
            val parts = cleaned.replace("₹", "").replace("/YR", "").split("-")
            val m1 = parts.getOrNull(0)?.toDoubleOrNull() ?: 500000.0
            val m2 = parts.getOrNull(1)?.toDoubleOrNull() ?: 10000000.0
            Pair((m1 / 100000.0).toInt(), (m2 / 100000.0).toInt())
        } else {
            Pair(10, 30)
        }
    } catch (e: Exception) {
        Pair(10, 30)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppDashboardScreen(
    viewModel: DishaViewModel,
    onNavigateToTab: (DishaScreen) -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs by viewModel.jobMatches.collectAsState()
    val activeSession by viewModel.activeSession.collectAsState()
    val searchVal by viewModel.searchQuery.collectAsState()

    // Collect sidebar filters from state flow
    val selLoc by viewModel.filterLocation.collectAsState()
    val selInd by viewModel.filterIndustry.collectAsState()
    val selSalMin by viewModel.filterSalaryMin.collectAsState()
    val selSalMax by viewModel.filterSalaryMax.collectAsState()
    val sortOpt by viewModel.sortOption.collectAsState()

    var isFilterSidebarOpen by remember { mutableStateOf(false) }
    var sidebarTab by remember { mutableStateOf("Filters") }

    val bookmarkedJobs = remember(jobs) { jobs.filter { it.isBookmarked } }

    val filterKey = remember(searchVal, selLoc, selInd, selSalMin, selSalMax, sortOpt) {
        "$searchVal-$selLoc-$selInd-$selSalMin-$selSalMax-$sortOpt"
    }

    val filteredJobs = remember(searchVal, jobs, selLoc, selInd, selSalMin, selSalMax, sortOpt) {
        var list = jobs

        // 1. Search Query
        if (searchVal.trim().isNotEmpty()) {
            list = list.filter {
                it.title.contains(searchVal, ignoreCase = true) ||
                        it.company.contains(searchVal, ignoreCase = true) ||
                        it.location.contains(searchVal, ignoreCase = true)
            }
        }

        // 2. Location filter
        if (selLoc != "All") {
            list = list.filter {
                if (selLoc == "Remote") {
                    it.isRemote || it.location.contains("Remote", ignoreCase = true)
                } else if (selLoc == "Delhi NCR") {
                    it.location.contains("Noida", ignoreCase = true) ||
                            it.location.contains("Gurugram", ignoreCase = true) ||
                            it.location.contains("Gurgaon", ignoreCase = true)
                } else {
                    it.location.contains(selLoc, ignoreCase = true)
                }
            }
        }

        // 3. Industry Title filter
        if (selInd != "All") {
            list = list.filter {
                if (selInd == "Designer") {
                    it.title.contains("Designer", ignoreCase = true) || it.title.contains("UX", ignoreCase = true)
                } else if (selInd == "Product Manager") {
                    it.title.contains("Product Manager", ignoreCase = true) || it.title.contains("PM", ignoreCase = true)
                } else if (selInd == "Engineer") {
                    it.title.contains("Engineer", ignoreCase = true) || it.title.contains("Architect", ignoreCase = true)
                } else {
                    it.title.contains(selInd, ignoreCase = true)
                }
            }
        }

        // 4. INR Salary filter in LPA
        list = list.filter {
            val (minLpa, maxLpa) = getSalaryLpa(it.salaryRange)
            minLpa >= selSalMin && minLpa <= selSalMax
        }

        // 5. Sort Order
        when (sortOpt) {
            "Salary High to Low" -> list.sortedByDescending { getSalaryLpa(it.salaryRange).second }
            "Salary Low to High" -> list.sortedBy { getSalaryLpa(it.salaryRange).first }
            else -> list.sortedByDescending { it.matchScore } // Default Sort by Match Score
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = SpaceBlack,
            topBar = {
                Column(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = activeSession?.name?.take(2)?.uppercase() ?: "AK", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Disha",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 24.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Dark Mode quick indicator toggle
                            IconButton(
                                onClick = { viewModel.toggleDarkMode() },
                                modifier = Modifier
                                    .background(CardViolet, CircleShape)
                                    .size(40.dp)
                            ) {
                                val isDark by viewModel.isDarkMode.collectAsState()
                                Icon(
                                    imageVector = if (isDark) Icons.Default.Lock else Icons.Default.Info,
                                    contentDescription = "Toggle Quick DarkMode",
                                    tint = ElectricViolet
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Settings navigation trigger
                            IconButton(
                                onClick = { onNavigateToTab(DishaScreen.RefinePreferences) },
                                modifier = Modifier
                                    .background(CardViolet, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Refine Filter", tint = TextPrimary)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                AppBottomNavigationBar(DishaScreen.Dashboard, onSelectTab = { onNavigateToTab(it) })
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Greetings Greeting Header
                item {
                    Column {
                        Text(
                            text = "Ready for your next\nleap, ${activeSession?.name?.split(" ")?.firstOrNull() ?: "Anubhav"}?",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 38.sp,
                            letterSpacing = (-1).sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "We've scanned top Indian tech hubs. Your profile is currently trending for Lead UI/UX roles. Let's find your perfect match.",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            lineHeight = 22.sp
                        )
                    }
                }

                // Search Bar with Sidebar Filter Trigger button
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CustomTextField(
                            value = searchVal,
                            onValueChange = { viewModel.onSearchQueryChange(it) },
                            placeholder = "Search roles, companies...",
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { isFilterSidebarOpen = true },
                            modifier = Modifier
                                .background(CardViolet, RoundedCornerShape(12.dp))
                                .size(52.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Sidebar Filter", tint = ElectricViolet)
                        }
                    }
                }

                // Horizontal active matches
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Active Matches", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text(text = "View all", color = ElectricViolet, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val activeMatchesList = filteredJobs.filter { it.matchScore >= 90 }
                        if (activeMatchesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepViolet),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "No matches over 90% score", color = TextSecondary)
                            }
                        } else {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(activeMatchesList) { index, match ->
                                    FramerEntrance(index = index, filterKey = filterKey) {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = DeepViolet),
                                            modifier = Modifier
                                                .width(280.dp)
                                                .clickable { onJobClick(match.id) },
                                            shape = RoundedCornerShape(20.dp),
                                            border = BorderStroke(1.dp, BorderHighlight)
                                        ) {
                                            Column(modifier = Modifier.padding(18.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .clip(CircleShape)
                                                                .background(SunsetOrange.copy(alpha = 0.2f)),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(text = match.company.take(2), color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                        }
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = match.company,
                                                            color = TextSecondary,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        IconButton(
                                                            onClick = { viewModel.toggleJobBookmark(match.id, !match.isBookmarked) },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = if (match.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                                contentDescription = "Bookmark",
                                                                tint = if (match.isBookmarked) SunsetOrange else TextSecondary,
                                                                modifier = Modifier.size(20.dp)
                                                                )
                                                        }
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        // Rating capsule
                                                        Box(
                                                            modifier = Modifier
                                                                .background(MintyTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(text = "🔥 ${match.matchScore}%", color = MintyTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(text = match.title, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = match.location, color = TextSecondary, fontSize = 13.sp)
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(text = match.salaryRange, color = TextSecondary, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Connection Dashboard
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepViolet),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, BorderHighlight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(text = "Listing Sources", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Choose platforms to pull roles from dynamically", color = TextSecondary, fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            val platforms = listOf(
                                Triple("LinkedIn Integration", activeSession?.linkedinConnected ?: true, "linkedin"),
                                Triple("Indeed Integration", activeSession?.indeedConnected ?: false, "indeed"),
                                Triple("Naukri.com Integration", activeSession?.naukriConnected ?: true, "naukri"),
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
                                            if (key == "daily_digest") {
                                                viewModel.updateToggleDigest(it)
                                            } else {
                                                viewModel.updatePlatformConnection(key, it)
                                            }
                                        },
                                        colors = SwitchDefaults.colors(checkedThumbColor = MintyTeal, checkedTrackColor = ElectricViolet)
                                    )
                                }
                            }
                        }
                    }
                }

                // Latest Aggregated Roles
                item {
                    Column {
                        Text(text = "Latest Aggregated Roles", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))

                        filteredJobs.filter { it.matchScore < 90 }.forEachIndexed { index, cardJob ->
                            FramerEntrance(
                                index = index,
                                filterKey = filterKey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderHighlight),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onJobClick(cardJob.id) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(44.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(ElectricViolet.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = cardJob.company.take(2), color = ElectricViolet, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(text = cardJob.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(text = "${cardJob.company} • ${cardJob.location}", color = TextSecondary, fontSize = 13.sp)

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(CardViolet, RoundedCornerShape(6.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = cardJob.sourcePlatform, color = SunsetOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Text(text = cardJob.salaryRange, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = { viewModel.toggleJobBookmark(cardJob.id, !cardJob.isBookmarked) },
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (cardJob.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Bookmark",
                                                    tint = if (cardJob.isBookmarked) SunsetOrange else TextSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sliding Sidebar Overlay Panel
        if (isFilterSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isFilterSidebarOpen = false }
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isFilterSidebarOpen = false }
                )

                Column(
                    modifier = Modifier
                        .width(310.dp)
                        .fillMaxHeight()
                        .background(DeepViolet)
                        .border(BorderStroke(1.dp, BorderHighlight), shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                        .padding(20.dp)
                        .clickable(enabled = false) {},
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .then(
                                if (sidebarTab == "Filters") Modifier.verticalScroll(androidx.compose.foundation.rememberScrollState())
                                else Modifier
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "Sidebar Menu", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            IconButton(onClick = { isFilterSidebarOpen = false }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        // Tab switcher inside Sidebar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardViolet, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Filters", "Saved Jobs").forEach { tab ->
                                val active = sidebarTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) ElectricViolet else Color.Transparent)
                                        .clickable { sidebarTab = tab }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (tab == "Saved Jobs") "Saved (${bookmarkedJobs.size})" else tab,
                                        color = if (active) Color.Black else TextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        if (sidebarTab == "Filters") {
                            // 1. Location filter selection (chips)
                            Text(text = "Job Location", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val locs = listOf("All", "Remote", "Bangalore", "Delhi NCR", "Stockholm")
                                locs.forEach { loc ->
                                    val active = selLoc == loc
                                    Box(
                                        modifier = Modifier
                                            .background(if (active) ElectricViolet else CardViolet, RoundedCornerShape(12.dp))
                                            .border(1.dp, if (active) ElectricViolet else BorderHighlight, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setFilterLocation(loc) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = loc, color = if (active) Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 2. Industry Role Filter Selection
                            Text(text = "Industry / Job Title", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            val industries = listOf("All", "Designer", "Product Manager", "Engineer")
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                industries.forEach { ind ->
                                    val active = selInd == ind
                                    Box(
                                        modifier = Modifier
                                            .background(if (active) ElectricViolet else CardViolet, RoundedCornerShape(12.dp))
                                            .border(1.dp, if (active) ElectricViolet else BorderHighlight, RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setFilterIndustry(ind) }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(text = ind, color = if (active) Color.Black else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // 3. INR Salary range (Annual in LPA)
                            Text(text = "INR Salary (Annual, in LPA)", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "₹${selSalMin} LPA", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "₹${selSalMax} LPA", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            RangeSlider(
                                value = selSalMin.toFloat()..selSalMax.toFloat(),
                                onValueChange = { range ->
                                    viewModel.setFilterSalaryRange(range.start.toInt(), range.endInclusive.toInt())
                                },
                                valueRange = 5f..100f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = ElectricViolet,
                                    thumbColor = SunsetOrange
                                )
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // 4. Sort Order selection
                            Text(text = "Sort Order", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            listOf("Match Score", "Salary High to Low", "Salary Low to High").forEach { sortVal ->
                                val active = sortOpt == sortVal
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setSortOption(sortVal) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = active,
                                        onClick = { viewModel.setSortOption(sortVal) },
                                        colors = RadioButtonDefaults.colors(selectedColor = ElectricViolet)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = sortVal, color = TextPrimary, fontSize = 14.sp)
                                }
                            }
                        } else {
                            // Saved Jobs Section
                            if (bookmarkedJobs.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = TextSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No saved jobs yet",
                                        color = TextPrimary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Tap the heart icon on any job to bookmark it here.",
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(bookmarkedJobs) { bmJob ->
                                        val isHighContrast by viewModel.isHighContrastMode.collectAsState()
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CardViolet),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    isFilterSidebarOpen = false
                                                    onJobClick(bmJob.id)
                                                },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, BorderHighlight)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.Top
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = bmJob.title,
                                                            color = TextPrimary,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 14.sp,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                        Text(
                                                            text = bmJob.company,
                                                            color = TextSecondary,
                                                            fontSize = 12.sp,
                                                            maxLines = 1,
                                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = { viewModel.toggleJobBookmark(bmJob.id, false) },
                                                        modifier = Modifier.size(28.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Favorite,
                                                            contentDescription = "Unsave",
                                                            tint = SunsetOrange,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(text = bmJob.salaryRange, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                                                    Box(
                                                        modifier = Modifier
                                                            .background(MintyTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(text = "🔥 ${bmJob.matchScore}%", color = MintyTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Pipeline Progress visual bar/gauge
                                                val progressPercent = when (bmJob.applicationStatus) {
                                                    "Applied" -> 0.33f
                                                    "Interviewing" -> 0.66f
                                                    "Offer Received" -> 1.0f
                                                    else -> 0f
                                                }
                                                val statusLabel = bmJob.applicationStatus ?: "Saved"

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "Status: $statusLabel",
                                                        color = TextSecondary,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    if (progressPercent > 0f) {
                                                        Text(
                                                            text = "${(progressPercent * 100).toInt()}%",
                                                            color = if (statusLabel == "Offer Received") MintyTeal else ElectricViolet,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Custom visual track line
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp))
                                                        .background(BorderHighlight.copy(alpha = 0.2f))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(progressPercent)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(
                                                                if (statusLabel == "Offer Received") MintyTeal
                                                                else if (statusLabel == "Interviewing") SunsetOrange
                                                                else ElectricViolet
                                                            )
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(10.dp))

                                                // Quick status selection tags
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(SoftGray, RoundedCornerShape(8.dp))
                                                        .padding(2.dp),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    val statuses = listOf("Applied", "Interviewing", "Offer")
                                                    statuses.forEach { statusText ->
                                                        val dbStatus = when (statusText) {
                                                            "Applied" -> "Applied"
                                                            "Interviewing" -> "Interviewing"
                                                            "Offer" -> "Offer Received"
                                                            else -> null
                                                        }
                                                        val isActive = bmJob.applicationStatus == dbStatus
                                                        val activeColor = when (statusText) {
                                                            "Applied" -> ElectricViolet
                                                            "Interviewing" -> SunsetOrange
                                                            "Offer" -> MintyTeal
                                                            else -> ElectricViolet
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isActive) activeColor else Color.Transparent)
                                                                .clickable {
                                                                    val newStatus = if (isActive) null else dbStatus
                                                                    viewModel.updateJobApplicationStatus(bmJob.id, newStatus)
                                                                }
                                                                .padding(vertical = 6.dp),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = statusText,
                                                                color = if (isActive) {
                                                                    if (isHighContrast) Color.Black else Color.White
                                                                } else TextSecondary,
                                                                fontSize = 9.sp,
                                                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (sidebarTab == "Filters") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.resetFilters() },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .border(1.dp, BorderHighlight, RoundedCornerShape(12.dp))
                            ) {
                                Text(text = "Reset", color = SunsetOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { isFilterSidebarOpen = false },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text(text = "Apply", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = { isFilterSidebarOpen = false },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "Close", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            AppBottomNavigationBar(currentTab, onSelectTab = { onNavigateToTab(it) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Title
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Smart CV & Documents Hub",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Manage your local scout resumes, cover letters, and app store deployment guides.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Uploaded Smart CV
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                            tint = if (hasResume) MintyTeal else SunsetOrange,
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
                                viewModel.uploadResumeAndProgress("Anubhav_Kapoor_NewCV.pdf")
                                Toast.makeText(context, "Smart resume updated successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = if (hasResume) "Replace Resume Blueprint" else "Upload Scout Resume File", color = Color.White)
                        }
                    }
                }
            }

            // Referral Program Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = SunsetOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Disha Service Desk & Referrals", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text(
                            text = "Professional resume crafting and tailored cover letter engines for Indian tech hubs.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Price table
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .background(CardViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Professional Resume PDF", color = TextPrimary, fontSize = 14.sp)
                                Text(text = "₹49", color = MintyTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Tailored Cover Letter Prompt", color = TextPrimary, fontSize = 14.sp)
                                Text(text = "₹49", color = MintyTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Divider(color = BorderHighlight.copy(alpha = 0.5f), thickness = 1.dp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Subscription Fees", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "₹0 (No Hidden Fees)", color = SunsetOrange, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Referral Section
                        Text(text = "Referral Bonus Program", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Per referral is 1 FREE smart resume blueprint. Accumulate free tokens instantly.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        val count = activeSession?.referralsCount ?: 0
                        Text(text = "Referrals Made: $count ($count Resumes Earned Free)", color = MintyTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)

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
                                        Toast.makeText(context, "Enter contact to refer", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.inviteReferral(referralInput)
                                        Toast.makeText(context, "Successfully referred $referralInput! +1 Smart Resume Earned Free!", Toast.LENGTH_LONG).show()
                                        referralInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SunsetOrange),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text(text = "Refer & Earn", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Play Store & App Store Submission Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ElectricViolet)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "App & Play Store Submission", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text(
                            text = "Required diagnostic metrics and legal guidelines needed to upload, publish, and maintain Disha on Google Play Store & iOS App Store.",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // App Store Specs List
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Package ID", color = TextSecondary, fontSize = 13.sp)
                                Text(text = context.packageName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Target API SDK", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "API Level 34 (Android 14)", color = MintyTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Bundle Version", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "v1.0.0 (Build 12)", color = TextPrimary, fontSize = 13.sp)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Signing Config", color = TextSecondary, fontSize = 13.sp)
                                Text(text = "SHA-256 Release Ready", color = MintyTeal, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Required store policies
                        Text(text = "Legally Required Policies", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "These draft documents are standard prerequisites for automated store content reviews.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showPrivacyDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CardViolet),
                                border = BorderStroke(1.dp, BorderHighlight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Privacy Policy", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { showTermsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = CardViolet),
                                border = BorderStroke(1.dp, BorderHighlight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Terms of Service", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Play Account & Data Deletion self-service guideline
                        Text(text = "User Data Disposal (Play Store Requirement)", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(text = "Google Play requires all apps with user registrations to declare and provide an online and in-app self-service way to delete accounts and all related offline data.", color = TextSecondary, fontSize = 12.sp)

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                            border = BorderStroke(1.dp, BorderHighlight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Request Account & Data Deletion", color = SunsetOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress checklists
                        Text(text = "Play Store Console Readiness Checklist", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf(
                            "Adheres to Google Play Target API 34+ guidelines",
                            "Dynamic adaptive launcher logo equipped",
                            "Edge-to-edge layout & status bar padding checked",
                            "Room database encrypted locally",
                            "Contains in-app account & data deletion self-service option"
                        ).forEach { checkText ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MintyTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = checkText, color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
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
                                "Disha values your privacy. We store all saved job matching criteria, search keywords, and user session details locally on your device in an offline-first SQLite database (powered by Room).\n\n" +
                                "1. Information We Collect:\n" +
                                "- Profile data (name, email, phone, bio/designation) for scout personalization.\n" +
                                "- Uploaded CV blueprint text used solely for local application analysis.\n\n" +
                                "2. Integration with Sync Features:\n" +
                                "If you enable Google Sheets Sync, matching jobs are written directly to your declared spreadsheet via secure APIs. Data is never sold or shared with unapproved third parties.\n\n" +
                                "3. Security & Safety:\n" +
                                "To prevent unauthorized access, we provide integrated Biometrics authentication (FaceID/TouchID settings).\n\n" +
                                "For any inquiries or to execute a full GDPR/Data deletion request, please initiate data deletion directly inside your Profile settings.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(text = "I Understand", color = ElectricViolet, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepViolet,
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
                                "1. Proper Use of Scouting Tools:\n" +
                                "Our real-time FCM notification simulator, CV parser, and professional referral codes are designed for professional development. Abuse or reverse engineering of sync properties is terms violation.\n\n" +
                                "2. Payment and Premium Blueprints:\n" +
                                "Individual custom resume PDFs and tailoring scripts are billed clearly (₹49) with transparency. We adhere to Standard Consumer regulatory guidelines under Indian Law.\n\n" +
                                "3. Compliance & Fair-Play:\n" +
                                "You declare that all profile metrics (such as target salary, location preferences, name) are true representations.",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text(text = "Accept", color = ElectricViolet, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = DeepViolet,
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
                    text = "WARNING: This actions complies with Google Play policy mandates. This will instantly delete your active session profile, your uploaded CV blueprints, and clear all local Job Match database items from Room. This action is final and irreversible.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.logout() // Clear active session & DB
                        Toast.makeText(context, "Account, profile data, and database successfully erased.", Toast.LENGTH_LONG).show()
                    }
                ) {
                    Text(text = "ERASE ALL DATA", color = SunsetOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = "Cancel", color = TextPrimary)
                }
            },
            containerColor = DeepViolet,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
fun AppProfileScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit
) {
    val context = LocalContext.current
    val activeSession by viewModel.activeSession.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val isHighContrast by viewModel.isHighContrastMode.collectAsState()

    // Profile input states
    var editName by remember(activeSession) { mutableStateOf(activeSession?.name ?: "") }
    var editDesignation by remember(activeSession) { mutableStateOf(activeSession?.designation ?: "") }
    var editEmail by remember(activeSession) { mutableStateOf(activeSession?.email ?: "") }
    var editMobile by remember(activeSession) { mutableStateOf(activeSession?.mobile ?: "") }

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            AppBottomNavigationBar(currentTab, onSelectTab = { onNavigateToTab(it) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            // Title
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "My Scout Profile",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
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
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = ElectricViolet)
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
                                    Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.updateProfile(editName, editDesignation, editEmail, editMobile)
                                    Toast.makeText(context, "Basic Information Updated!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text(text = "Save Profile Details", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Dark Mode Switch Row
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.Lock else Icons.Default.Info,
                                contentDescription = "Theme Icon",
                                tint = ElectricViolet,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Dark Mode Active", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = if (isDark) "Ambient cosmic glow activated" else "Classic comfortable light mode active", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { viewModel.toggleDarkMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintyTeal,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }
                }
            }

            // High Contrast Mode Switch Row
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                                    .background(ElectricViolet.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "A",
                                    color = ElectricViolet,
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
                            onCheckedChange = { viewModel.toggleHighContrastMode() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MintyTeal,
                                checkedTrackColor = ElectricViolet
                            )
                        )
                    }
                }
            }

            // Disha Preferences & Settings Navigation Panel
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
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
                            text = "Manage your global app appearance, referrals metrics, and account security policies.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // 1. Appearance Screen Route
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable { onNavigateToTab(DishaScreen.Appearance) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = ElectricViolet, modifier = Modifier.size(20.dp))
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
                                .background(CardViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable { onNavigateToTab(DishaScreen.ReferEarn) }
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
                                .background(CardViolet.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .border(BorderStroke(1.dp, BorderHighlight.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                                .clickable { onNavigateToTab(DishaScreen.Privacy) }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = MintyTeal, modifier = Modifier.size(20.dp))
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
                    onClick = { viewModel.logout() },
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
}

@Composable
fun AppAutomateScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            AppBottomNavigationBar(currentTab, onSelectTab = { onNavigateToTab(it) })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Automate Scouting",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Let AI scout and prepare applications on your behalf",
                color = TextSecondary,
                fontSize = 15.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = DeepViolet),
                border = BorderStroke(1.dp, BorderHighlight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(text = "Global Preference Rules", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto-Apply Alerts", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Notify me when the AI scout identifies high-potential matches.", color = TextSecondary, fontSize = 13.sp)
                        }
                        val autoApply = activeSession?.autoApplyAlerts ?: false
                        Switch(
                            checked = autoApply,
                            onCheckedChange = { viewModel.updateToggleAutoApply(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MintyTeal)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Google Sheets Sync", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Automatically log matches to a custom Sheet.", color = TextSecondary, fontSize = 13.sp)
                        }
                        Box(
                            modifier = Modifier
                                .background(CardViolet, RoundedCornerShape(10.dp))
                                .border(1.dp, BorderHighlight, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Connected", color = MintyTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Notification Simulator Card
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(
                colors = CardDefaults.cardColors(containerColor = CardViolet),
                border = BorderStroke(1.dp, BorderHighlight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectricViolet.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = ElectricViolet,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "FCM Notification Center", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Real-time preference alerts", color = MintyTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Simulates an incoming Firebase Cloud Message payload containing a newly posted job. The app's receiver checks if the job match score is high or corresponds to your preference roles before posting a push notification.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.simulateIncomingMatch(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Simulate Matching Jobs Posting FCM", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AppAssistScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs by viewModel.jobMatches.collectAsState()

    Scaffold(
        containerColor = SpaceBlack,
        bottomBar = {
            AppBottomNavigationBar(currentTab, onSelectTab = { onNavigateToTab(it) })
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .statusBarsPadding()
                .fillMaxSize()
                .padding(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "AI Interview Assistant",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Review cover notes and key application insights",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            items(jobs) { job ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    border = BorderStroke(1.dp, BorderHighlight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable { onJobClick(job.id) }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = job.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Box(
                                modifier = Modifier
                                    .background(SunsetOrange.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "PRO Application Note", color = SunsetOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Matched at ${job.company} (${job.location})", color = TextSecondary, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun JobDetailScreen(
    job: JobMatch,
    viewModel: DishaViewModel,
    onBack: () -> Unit,
    onToggleBookmark: (String, Boolean) -> Unit,
    onUpdateStatus: (String, String?) -> Unit,
    onSendNoteClick: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        containerColor = SpaceBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(text = "Detail View", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        onToggleBookmark(job.id, !job.isBookmarked)
                    }) {
                        Icon(
                            imageVector = if (job.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (job.isBookmarked) SunsetOrange else TextPrimary
                        )
                    }
                    IconButton(onClick = { Toast.makeText(context, "Shared link to job match!", Toast.LENGTH_SHORT).show() }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = TextPrimary)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(text = job.title, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                Text(text = job.company, color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }

                            // Spotify Logo simulator
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1DB954)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = job.company.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            BadgeCap(text = "🔥 ${job.matchScore}% Match", color = MintyTeal)
                            BadgeCap(text = "Full-time", color = ElectricViolet)
                            BadgeCap(text = "Mid-Senior", color = SunsetOrange)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Location: ${job.location}", color = TextSecondary)
                        Text(text = "Salary: ${job.salaryRange}", color = TextSecondary)

                        Spacer(modifier = Modifier.height(20.dp))

                        CustomButton(
                            text = "Review & Apply",
                            onClick = { Toast.makeText(context, "Redirecting to application portal...", Toast.LENGTH_SHORT).show() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("review_apply_button")
                        )
                    }
                }
            }

            // 🌐 Live Deep Web Scraping Integration Card
            item {
                val activeSession by viewModel.activeSession.collectAsState(initial = null)
                var isScrapeEnabled by remember { mutableStateOf(true) }
                var scrapingState by remember { mutableStateOf("IDLE") } // "IDLE", "SCANNING", "COMPLETED"
                var logMsg by remember { mutableStateOf("") }
                val coroutineScope = rememberCoroutineScope()

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isScrapeEnabled) CardViolet else DeepViolet.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isScrapeEnabled) {
                            if (scrapingState == "COMPLETED") MintyTeal else ElectricViolet
                        } else BorderHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Header block
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (isScrapeEnabled) MintyTeal.copy(alpha = 0.15f) else SoftGray,
                                        RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    tint = if (isScrapeEnabled) MintyTeal else TextSecondary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Deep Web Scraping Engine",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Live Scraper Integration (LinkedIn/Indeed/Naukri)",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            // Toggle Switch for Scraping
                            Switch(
                                checked = isScrapeEnabled,
                                onCheckedChange = { isScrapeEnabled = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MintyTeal,
                                    checkedTrackColor = ElectricViolet
                                ),
                                modifier = Modifier.testTag("scrape_detail_toggle")
                            )
                        }

                        if (isScrapeEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Platform Integration Status Indicator Grid
                            Text(
                                text = "Platform Microservice Integration Status:",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val liConnected = activeSession?.linkedinConnected ?: true
                                val indConnected = activeSession?.indeedConnected ?: false
                                val nkrConnected = activeSession?.naukriConnected ?: true

                                // Chip 1: LinkedIn
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (liConnected) ElectricViolet.copy(alpha = 0.15f) else SoftGray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (liConnected) ElectricViolet.copy(alpha = 0.5f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("LinkedIn", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (liConnected) "🟢 Connected" else "🔴 Disabled",
                                            color = if (liConnected) MintyTeal else TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                // Chip 2: Indeed
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (indConnected) ElectricViolet.copy(alpha = 0.15f) else SoftGray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (indConnected) ElectricViolet.copy(alpha = 0.5f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Indeed", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (indConnected) "🟢 Connected" else "🔴 Disabled",
                                            color = if (indConnected) MintyTeal else TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }

                                // Chip 3: Naukri.com
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (nkrConnected) ElectricViolet.copy(alpha = 0.15f) else SoftGray.copy(alpha = 0.1f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (nkrConnected) ElectricViolet.copy(alpha = 0.5f) else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Naukri.com", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (nkrConnected) "🟢 Connected" else "🔴 Disabled",
                                            color = if (nkrConnected) MintyTeal else TextSecondary,
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Execute deep scrape button
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        scrapingState = "SCANNING"
                                        logMsg = "Initializing Selenium/Puppeteer emulator bridge..."
                                        kotlinx.coroutines.delay(800)
                                        logMsg = "Formulating secure API crawler path for ${job.sourcePlatform}..."
                                        kotlinx.coroutines.delay(800)
                                        logMsg = "Navigating to: https://${job.sourcePlatform.lowercase()}.com/jobs/view..."
                                        kotlinx.coroutines.delay(800)
                                        logMsg = "Bypassing anti-scrape layers & matching DOM node elements..."
                                        kotlinx.coroutines.delay(800)
                                        logMsg = "Web scraping success! Formatted schema properties synced."
                                        kotlinx.coroutines.delay(600)
                                        scrapingState = "COMPLETED"
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SpaceBlack),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderHighlight),
                                modifier = Modifier.fillMaxWidth().testTag("trigger_scrape_button"),
                                enabled = scrapingState != "SCANNING"
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = MintyTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (scrapingState == "SCANNING") "Scraping Live Web portal..." else "Force Real-time Scrape Sync",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (scrapingState == "SCANNING") {
                                Spacer(modifier = Modifier.height(12.dp))
                                LinearProgressIndicator(
                                    trackColor = BorderHighlight.copy(alpha = 0.2f),
                                    color = MintyTeal,
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SoftGray, RoundedCornerShape(8.dp))
                                        .border(1.dp, BorderHighlight, RoundedCornerShape(8.dp))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "> $logMsg",
                                        color = MintyTeal,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }

                            // Show the beautiful Scraped Information if Completed/Idle
                            if (scrapingState != "SCANNING") {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Scraped Details & Platform Insights:",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(SpaceBlack.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Source Platform:", color = TextSecondary, fontSize = 11.sp)
                                        Text(job.sourcePlatform, color = MintyTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Candidate Volumes:", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            text = when (job.sourcePlatform.lowercase()) {
                                                "linkedin" -> "142 Applicants submitted"
                                                "indeed" -> "24 Resumes scanned (Moderate competition)"
                                                "naukri" -> "72 Applications in past 24 hours"
                                                else -> "Direct pipeline integration active"
                                            },
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Scraped Link Validity:", color = TextSecondary, fontSize = 11.sp)
                                        Text("Verified Secure HTTP Link 🛡️", color = MintyTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Integrity Match Confidence:", color = TextSecondary, fontSize = 11.sp)
                                        Text("99.4% (Extremely High Consistency)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Auto-Scrape Engine Status:", color = TextSecondary, fontSize = 11.sp)
                                        Text(
                                            text = if (scrapingState == "COMPLETED") "Force Scraped Just Now" else "Periodic Auto-Sync Active",
                                            color = SunsetOrange,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Live web scraping is currently toggled OFF. Enable to pull active candidate volume metrics, live direct apply URLs, and compensation indicators.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            // Application Progress Tracker Card on Job Detail View
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricViolet.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Application Progress", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = if (job.applicationStatus != null) "Current status: ${job.applicationStatus}" else "Not applied yet (Saved)",
                                    color = MintyTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress path with lines & nodes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val steps = listOf("Saved", "Applied", "Interviewing", "Offer")
                            val currentStatus = job.applicationStatus ?: "Saved"
                            
                            val stepWeight = when (currentStatus) {
                                "Saved" -> 0
                                "Applied" -> 1
                                "Interviewing" -> 2
                                "Offer Received" -> 3
                                else -> 0
                            }

                            steps.forEachIndexed { sIndex, stepLabel ->
                                val isActive = when (stepLabel) {
                                    "Saved" -> currentStatus == "Saved"
                                    "Applied" -> currentStatus == "Applied"
                                    "Interviewing" -> currentStatus == "Interviewing"
                                    "Offer" -> currentStatus == "Offer Received"
                                    else -> false
                                }
                                val isPassed = when (stepLabel) {
                                    "Saved" -> true
                                    "Applied" -> stepWeight >= 1
                                    "Interviewing" -> stepWeight >= 2
                                    "Offer" -> stepWeight >= 3
                                    else -> false
                                }

                                val stepColor = if (isActive) {
                                    when (stepLabel) {
                                        "Applied" -> ElectricViolet
                                        "Interviewing" -> SunsetOrange
                                        "Offer" -> MintyTeal
                                        else -> ElectricViolet
                                    }
                                } else if (isPassed) {
                                    ElectricViolet
                                } else {
                                    TextSecondary.copy(alpha = 0.3f)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            val newStatus = when (stepLabel) {
                                                "Saved" -> null
                                                "Applied" -> "Applied"
                                                "Interviewing" -> "Interviewing"
                                                "Offer" -> "Offer Received"
                                                else -> null
                                            }
                                            onUpdateStatus(job.id, newStatus)
                                            // Auto-save the job (bookmark) if status is moved from null to something else!
                                            if (newStatus != null && !job.isBookmarked) {
                                                onToggleBookmark(job.id, true)
                                            }
                                        }
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(stepColor.copy(alpha = 0.15f), CircleShape)
                                            .border(2.dp, stepColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(stepColor, CircleShape)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = stepLabel,
                                        color = if (isActive) TextPrimary else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1
                                    )
                                }

                                if (sIndex < 3) {
                                    val isPathPassed = stepWeight > sIndex
                                    Box(
                                        modifier = Modifier
                                            .weight(0.4f)
                                            .height(3.dp)
                                            .background(
                                                if (isPathPassed) ElectricViolet else TextSecondary.copy(alpha = 0.2f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Magical Cover Letter card banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(ElectricViolet, SunsetOrange))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SunsetOrange, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Magical Cover Letter", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.background(SunsetOrange, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                Text(text = "PRO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Stop struggling with a blank page. Let our AI craft a highly personalized cover letter highlighting your ${job.matchScore}% match for this specific role at ${job.company}.",
                            color = TextSecondary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { Toast.makeText(context, "Generated premium draft!", Toast.LENGTH_SHORT).show() },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceBlack),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Generate for ₹49 ✨", color = Color.White)
                        }
                    }
                }
            }

            // Job Responsibilities list
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Job Responsibilities", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val responsibilities = listOf(
                            "Lead end-to-end design for core listening experiences, ensuring a seamless user journey across mobile and desktop platforms.",
                            "Collaborate tightly with cross-functional teams including PMs, Engineering, and Data Science to define product vision.",
                            "Mentor junior designers and contribute to the evolution of the Encore design system."
                        )

                        responsibilities.forEach { resp ->
                            Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = MintyTeal, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = resp, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }

            // Hiring Team card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepViolet),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Meet the hiring team", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(SunsetOrange),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "PS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(text = job.recruiterName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = job.recruiterRole, color = TextSecondary, fontSize = 14.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { Toast.makeText(context, "Opening LinkedIn recruiter profile...", Toast.LENGTH_SHORT).show() },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(text = "View LinkedIn", color = TextPrimary)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = onSendNoteClick,
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(text = "Send a note", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SendNoteScreen(
    jobTitle: String,
    companyName: String,
    recruiterName: String,
    onCancel: () -> Unit,
    onSent: () -> Unit
) {
    var letterBody by remember {
        mutableStateOf(
            "Dear $recruiterName,\n\nI hope this message finds you well. I am highly interested in the $jobTitle opportunity at $companyName that I discovered through Disha. I believe my skills in UX Design and product flow strategies would be a valuable asset to your design team.\n\nI look forward to the possibility of discussing how I can contribute.\n\nBest regards,\nAnubhav Kapoor"
        )
    }
    val context = LocalContext.current

    Scaffold(containerColor = SpaceBlack) { innerPadding ->
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
                    color = SunsetOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable { onCancel() }
                )
                Text(text = "Personal Note", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    text = "Send",
                    color = MintyTeal,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.clickable {
                        Toast.makeText(context, "Note sent to recruiter!", Toast.LENGTH_SHORT).show()
                        onSent()
                    }
                )
            }

            // Textarea Editor card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepViolet),
                border = BorderStroke(1.dp, BorderHighlight),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 24.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    BasicTextField(
                        value = letterBody,
                        onValueChange = { letterBody = it },
                        textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp, lineHeight = 22.sp),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            CustomButton(
                text = "Send Note",
                onClick = {
                    Toast.makeText(context, "Note submitted successfully!", Toast.LENGTH_SHORT).show()
                    onSent()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("send_note_button")
            )
        }
    }
}

// ======================= REUSABLE COMPOSABLE LAYOUT CLIENTS =======================

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
        shape = RoundedCornerShape(30.dp),
        modifier = modifier
    ) {
        Text(text = text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(DeepViolet, RoundedCornerShape(16.dp))
            .border(1.dp, BorderHighlight, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (value.isEmpty()) {
            Text(text = placeholder, color = TextSecondary, fontSize = 15.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(color = TextPrimary, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BadgeCap(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlowRowSim(
    items: List<String>,
    onRemove: (String) -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .background(CardViolet, RoundedCornerShape(20.dp))
                    .border(1.dp, BorderHighlight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item, color = TextPrimary, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = SunsetOrange,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onRemove(item) }
                )
            }
        }
    }
}

@Composable
fun AppBottomNavigationBar(
    currentTab: DishaScreen,
    onSelectTab: (DishaScreen) -> Unit
) {
    val items = listOf(
        Triple("Dashboard", DishaScreen.Dashboard, Icons.Default.Home),
        Triple("CV", DishaScreen.CV, Icons.Default.Edit),
        Triple("Automate", DishaScreen.Automate, Icons.Default.Settings),
        Triple("Assist", DishaScreen.Assist, Icons.Default.Star),
        Triple("Profile", DishaScreen.Profile, Icons.Default.Person)
    )

    NavigationBar(
        containerColor = DeepViolet,
        tonalElevation = 8.dp
    ) {
        items.forEach { (label, route, icon) ->
            NavigationBarItem(
                selected = currentTab == route,
                onClick = { onSelectTab(route) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(text = label, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = ElectricViolet,
                    indicatorColor = ElectricViolet,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )
            )
        }
    }
}

@Composable
fun FramerEntrance(
    index: Int,
    filterKey: Any,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var animateTrigger by remember(filterKey) { mutableStateOf(false) }

    LaunchedEffect(filterKey) {
        // Dynamic staggered delay based on the card index (capped to avoid long initial layouts)
        val staggerDelay = (index * 40L).coerceAtMost(240L)
        kotlinx.coroutines.delay(staggerDelay)
        animateTrigger = true
    }

    val transition = updateTransition(targetState = animateTrigger, label = "FramerEntrance")

    val scale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = 0.72f, // Subtle spring recoil like Framer Motion's default bounce
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "scale"
    ) { state ->
        if (state) 1f else 0.94f
    }

    val offsetY by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = 0.72f,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "offsetY"
    ) { state ->
        if (state) 0f else 30f
    }

    val alpha by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "alpha"
    ) { state ->
        if (state) 1f else 0f
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                this.alpha = alpha
                this.scaleX = scale
                this.scaleY = scale
                this.translationY = offsetY * density
            }
    ) {
        content()
    }
}
