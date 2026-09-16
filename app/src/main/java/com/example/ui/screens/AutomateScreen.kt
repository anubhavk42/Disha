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
fun AppAutomateScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit
) {
    val activeSession by viewModel.activeSession.collectAsState()
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = SpaceBlack,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .hazeSource(state = hazeState)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = bottomNavBarClearance())
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
                colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
                            colors = SwitchDefaults.colors(checkedThumbColor = SeafoamMint)
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
                                .background(CardTeal, RoundedCornerShape(10.dp))
                                .border(1.dp, BorderHighlight, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Connected", color = SeafoamMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real-Time Notification Simulator Card
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(
                colors = CardDefaults.cardColors(containerColor = CardTeal),
                border = BorderStroke(1.dp, BorderHighlight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(ElectricTeal.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = null,
                                tint = ElectricTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Job Alerts", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(text = "Never miss a new match", color = SeafoamMint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "We'll send you a notification the moment a job matching your preferences is posted, so you can be one of the first to apply.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.simulateIncomingMatch(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Send Me a Test Job Alert", color = Color.White, fontWeight = FontWeight.Bold)
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
}
