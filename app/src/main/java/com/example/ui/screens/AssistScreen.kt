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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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

private fun domainInsight(title: String): String {
    val t = title.lowercase()
    return when {
        "design" in t -> "Highlight a project where your design directly moved a product metric — interviewers weigh outcomes over process."
        "engineer" in t || "developer" in t -> "Be ready to walk through your most complex technical decision and the trade-offs you weighed."
        "product manager" in t || " pm" in t -> "Frame your answers around prioritization trade-offs — that's what most PM interviews probe for."
        "data" in t || "scientist" in t || "analyst" in t -> "Prepare to explain one analysis end-to-end, including how you validated the result."
        "sales" in t || "growth" in t || "marketing" in t -> "Lead with a number — a specific quota, conversion lift, or campaign result you can speak to confidently."
        "hr" in t || "people" in t -> "Have a concrete example ready of a people-process you improved, not just a policy you followed."
        "legal" in t || "counsel" in t -> "Be ready to discuss a time you balanced legal risk against business speed."
        "operations" in t || "supply chain" in t -> "Quantify an efficiency you drove — operations interviews reward specifics over generalities."
        "security" in t -> "Have an incident or vulnerability you handled ready, with what you'd do differently."
        "finance" in t || "fp&a" in t -> "Be ready to defend a forecast or model assumption under scrutiny."
        "research" in t || "clinical" in t -> "Prepare an example of how you ensured data integrity or protocol compliance."
        "consultant" in t -> "Structure your answers the way you'd structure a client deliverable — framework first, detail second."
        else -> "Tailor your examples to this company's stage and sector — generic answers stand out for the wrong reason."
    }
}

private fun domainQuestion(title: String): String {
    val t = title.lowercase()
    return when {
        "design" in t -> "Walk me through a design decision you made that a stakeholder disagreed with."
        "engineer" in t || "developer" in t -> "Tell me about a time you had to debug a production issue under pressure."
        "product manager" in t || " pm" in t -> "How do you decide what NOT to build?"
        "data" in t || "scientist" in t || "analyst" in t -> "How would you explain a complex finding to a non-technical stakeholder?"
        "sales" in t || "growth" in t || "marketing" in t -> "Describe a deal or campaign that didn't go as planned. What did you learn?"
        "hr" in t || "people" in t -> "How do you balance company policy with individual employee needs?"
        "legal" in t || "counsel" in t -> "How do you communicate legal risk to a non-legal audience?"
        "operations" in t || "supply chain" in t -> "Tell me about a time a process you owned broke down. How did you fix it?"
        "security" in t -> "Walk me through how you'd triage a suspected breach in the first hour."
        "finance" in t || "fp&a" in t -> "How do you handle disagreement over a forecast's assumptions?"
        "research" in t || "clinical" in t -> "Tell me about a time a protocol deviation required you to make a judgment call."
        "consultant" in t -> "How would you structure an approach to a problem you've never seen before?"
        else -> "Why is this role the right next step for you specifically?"
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
                .padding(24.dp),
            contentPadding = PaddingValues(bottom = bottomNavBarClearance())
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "AI Interview Assistant",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGrotesk
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
                        .clickable {
                            haptics(Haptic.Click)
                            onJobClick(job.id)
                        }
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(companyAvatarColor(job.company)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = job.company.take(2).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = job.title,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(text = "${job.company} • ${job.location}", color = TextSecondary, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(PremiumGold.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = "PRO", color = PremiumGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = SeafoamMint, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Key talking point", color = SeafoamMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = domainInsight(job.title), color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal, RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(text = "Likely interview question", color = ElectricTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = "\"${domainQuestion(job.title)}\"", color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, modifier = Modifier.padding(top = 2.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(SeafoamMint.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(text = "🔥 ${job.matchScore}% Match", color = SeafoamMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "View full prep", color = ElectricTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = ElectricTeal, modifier = Modifier.size(14.dp))
                            }
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
}
