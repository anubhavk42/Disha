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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.JobMatch
import com.example.ui.DishaViewModel
import com.example.ui.theme.*
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

private val responsibilitiesAdapter: JsonAdapter<List<String>> by lazy {
    val moshi = Moshi.Builder().build()
    val type = Types.newParameterizedType(List::class.java, String::class.java)
    moshi.adapter(type)
}

/** Parses [JobMatch.responsibilitiesJson]; falls back to a single generic line if the field is empty/unset or malformed. */
private fun parseResponsibilities(json: String): List<String> {
    val parsed = try {
        responsibilitiesAdapter.fromJson(json)
    } catch (e: Exception) {
        null
    }
    return parsed.takeUnless { it.isNullOrEmpty() } ?: listOf(
        "Collaborate cross-functionally to deliver on the core objectives of this role."
    )
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
    val haptics = rememberHaptics()

    Scaffold(
        containerColor = SpaceBlack,
        topBar = {
            Row(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    haptics(Haptic.Click)
                    onBack()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(text = "Detail View", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        haptics(Haptic.Click)
                        onToggleBookmark(job.id, !job.isBookmarked)
                    }) {
                        Icon(
                            imageVector = if (job.isBookmarked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Bookmark",
                            tint = if (job.isBookmarked) SunsetOrange else TextPrimary
                        )
                    }
                    IconButton(onClick = {
                        haptics(Haptic.Click)
                        Toast.makeText(context, "Shared link to job match!", Toast.LENGTH_SHORT).show()
                    }) {
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
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 12.dp)
                            ) {
                                Text(
                                    text = job.title,
                                    color = TextPrimary,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = SpaceGrotesk,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(text = job.company, color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                            }

                            // Company avatar with a deterministic per-company color
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(companyAvatarColor(job.company)),
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
                            BadgeCap(text = "🔥 ${job.matchScore}% Match", color = SeafoamMint)
                            BadgeCap(text = "Full-time", color = ElectricTeal)
                            BadgeCap(text = "Mid-Senior", color = SunsetOrange)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(text = "Location: ${job.location}", color = TextSecondary)
                        Text(text = "Salary: ${formatSalaryDisplay(job.salaryRange)}", color = TextSecondary)

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

            // Application Progress Tracker Card on Job Detail View
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricTeal.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = "Application Progress", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(
                                    text = if (job.applicationStatus != null) "Current status: ${job.applicationStatus}" else "Not applied yet (Saved)",
                                    color = SeafoamMint,
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
                                        "Applied" -> ElectricTeal
                                        "Interviewing" -> SunsetOrange
                                        "Offer" -> SeafoamMint
                                        else -> ElectricTeal
                                    }
                                } else if (isPassed) {
                                    ElectricTeal
                                } else {
                                    TextSecondary.copy(alpha = 0.3f)
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            haptics(Haptic.Toggle)
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
                                                if (isPathPassed) ElectricTeal else TextSecondary.copy(alpha = 0.2f)
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
                    colors = CardDefaults.cardColors(containerColor = CardTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, Brush.horizontalGradient(listOf(ElectricTeal, PremiumGold))),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = PremiumGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Magical Cover Letter", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.background(PremiumGold, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
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
                            onClick = {
                                haptics(Haptic.Confirm)
                                Toast.makeText(context, "Generated premium draft!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "Generate for ₹49 ✨")
                        }
                    }
                }
            }

            // Job Responsibilities list
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderHighlight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "Job Responsibilities", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        val responsibilities = remember(job.responsibilitiesJson) { parseResponsibilities(job.responsibilitiesJson) }

                        responsibilities.forEach { resp ->
                            Row(modifier = Modifier.padding(vertical = 6.dp), verticalAlignment = Alignment.Top) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SeafoamMint, modifier = Modifier.size(20.dp).padding(top = 2.dp))
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
                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
                                val recruiterInitials = job.recruiterName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").uppercase()
                                Text(text = recruiterInitials, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                                onClick = {
                                    haptics(Haptic.Click)
                                    Toast.makeText(context, "Opening LinkedIn recruiter profile...", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).height(48.dp)
                            ) {
                                Text(text = "View LinkedIn", color = TextPrimary)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    haptics(Haptic.Click)
                                    onSendNoteClick()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
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

private fun noteSkillsPhrase(jobTitle: String): String {
    val t = jobTitle.lowercase()
    return when {
        "design" in t -> "user-centered design and end-to-end product thinking"
        "engineer" in t || "developer" in t -> "scalable engineering and clean system design"
        "product manager" in t || " pm" in t -> "cross-functional product strategy and execution"
        "data" in t || "scientist" in t || "analyst" in t -> "data-driven analysis and clear communication of insights"
        "sales" in t -> "consultative selling and relationship-building"
        "marketing" in t || "growth" in t -> "growth strategy and performance marketing"
        "hr" in t || "people" in t -> "people operations and organizational development"
        "legal" in t || "counsel" in t -> "sound legal judgment and contract negotiation"
        "operations" in t || "supply chain" in t -> "process optimization and operational execution"
        "security" in t -> "security engineering and risk mitigation"
        "finance" in t || "fp&a" in t -> "financial modeling and analytical rigor"
        "research" in t || "clinical" in t -> "rigorous research methodology and attention to detail"
        "consultant" in t -> "structured problem-solving and client management"
        else -> "adaptability and a strong track record of delivering results"
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
            "Dear $recruiterName,\n\nI hope this message finds you well. I am highly interested in the $jobTitle opportunity at $companyName that I discovered through Disha. I believe my skills in ${noteSkillsPhrase(jobTitle)} would be a valuable asset to your team.\n\nI look forward to the possibility of discussing how I can contribute.\n\nBest regards,\nAnubhav Kapoor"
        )
    }
    val context = LocalContext.current
    val haptics = rememberHaptics()

    Scaffold(
        containerColor = SpaceBlack,
        contentWindowInsets = WindowInsets.safeDrawing.exclude(WindowInsets.ime)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .imePadding()
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
                    modifier = Modifier.clickable {
                        haptics(Haptic.Click)
                        onCancel()
                    }
                )
                Text(text = "Personal Note", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.width(56.dp))
            }

            // Textarea Editor card
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
