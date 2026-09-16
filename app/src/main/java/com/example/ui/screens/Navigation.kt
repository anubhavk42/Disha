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
        // Symmetric case: session was cleared (e.g. "Log Out from Disha") while sitting on an
        // authenticated screen - navigate back out to Welcome instead of leaving the logged-out
        // user stranded on a screen that expects an active session.
        val authenticatedScreens = listOf(
            DishaScreen.Dashboard, DishaScreen.CV, DishaScreen.Automate, DishaScreen.Assist,
            DishaScreen.Profile, DishaScreen.JobDetail, DishaScreen.SendNote,
            DishaScreen.Appearance, DishaScreen.ReferEarn, DishaScreen.Privacy
        )
        if (activeSession == null && currentScreen in authenticatedScreens) {
            navigateAndClearHistory(DishaScreen.Welcome)
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
                    onCodeSent = { navigateTo(DishaScreen.Otp) },
                    onAutoVerified = { navigateTo(DishaScreen.ProfileSetup) }
                )
                DishaScreen.Otp -> OnboardingOtpScreen(
                    viewModel = viewModel,
                    onVerify = { navigateTo(DishaScreen.ProfileSetup) },
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
