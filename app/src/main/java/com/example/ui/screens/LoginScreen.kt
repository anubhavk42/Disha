package com.example.ui.screens

import androidx.activity.compose.BackHandler
import android.app.Activity
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
fun OnboardingLoginScreen(viewModel: DishaViewModel, onCodeSent: () -> Unit, onAutoVerified: () -> Unit) {
    val phone by viewModel.phoneInput.collectAsState()
    val email by viewModel.emailInput.collectAsState()
    val name by viewModel.userNameInput.collectAsState()
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authError by viewModel.authError.collectAsState()
    var isGoogleLogin by remember { mutableStateOf(false) }
    val activity = LocalContext.current as? Activity

    Scaffold(containerColor = SpaceBlack, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
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
                        .background(DeepTeal)
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
                            brush = Brush.linearGradient(listOf(ElectricTeal, SunsetOrange)),
                            style = Stroke(width = 4f)
                        )
                        drawCircle(
                            color = SeafoamMint,
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
                            .background(DeepTeal, RoundedCornerShape(16.dp))
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
                if (authError != null) {
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }

                CustomButton(
                    text = if (isAuthenticating) "Sending OTP..." else "Get OTP",
                    enabled = !isAuthenticating,
                    onClick = {
                        activity?.let { viewModel.requestOtp(it, onCodeSent = onCodeSent, onAutoVerified = onAutoVerified) }
                    },
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
                        activity?.let { viewModel.requestOtp(it, onCodeSent = onCodeSent, onAutoVerified = onAutoVerified) }
                    },
                    enabled = !isAuthenticating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .border(1.dp, BorderHighlight, RoundedCornerShape(30.dp))
                        .testTag("google_login_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_logo),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(20.dp)
                        )
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
    val isAuthenticating by viewModel.isAuthenticating.collectAsState()
    val authError by viewModel.authError.collectAsState()

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = "Step 2 of 3",
                    color = ElectricTeal,
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
                                .background(DeepTeal)
                                .border(
                                    width = if (otp.length == index) 2.dp else 1.dp,
                                    color = if (otp.length == index) ElectricTeal else BorderHighlight,
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

                if (authError != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            CustomButton(
                text = if (isAuthenticating) "Verifying..." else "Verify & Login",
                enabled = !isAuthenticating,
                onClick = { viewModel.verifyOtp(onSuccess = onVerify) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("verify_otp_button")
            )
        }
    }
}
