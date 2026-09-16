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
fun AppAssistScreen(
    viewModel: DishaViewModel,
    currentTab: DishaScreen,
    onNavigateToTab: (DishaScreen) -> Unit,
    onJobClick: (String) -> Unit
) {
    val jobs by viewModel.jobMatches.collectAsState()
    val hazeState = remember { HazeState() }

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
                .padding(24.dp),
            contentPadding = PaddingValues(bottom = bottomNavBarClearance())
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
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
    AppBottomNavigationBar(
        currentTab = currentTab,
        onSelectTab = { onNavigateToTab(it) },
        hazeState = hazeState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    }
}
