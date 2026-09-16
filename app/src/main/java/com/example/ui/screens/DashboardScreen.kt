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
    val hazeState = remember { HazeState() }
    val haptics = rememberHaptics()

    val bookmarkedJobs = remember(jobs) { jobs.filter { it.isBookmarked } }

    val filterKey = remember(searchVal, selLoc, selInd, selSalMin, selSalMax, sortOpt) {
        "$searchVal-$selLoc-$selInd-$selSalMin-$selSalMax-$sortOpt"
    }

    val filteredJobs = remember(searchVal, jobs, selLoc, selInd, selSalMin, selSalMax, sortOpt, activeSession) {
        var list = jobs

        // 0. Only show jobs from source platforms the user has connected
        list = list.filter {
            when (it.sourcePlatform) {
                "LinkedIn" -> activeSession?.linkedinConnected ?: true
                "Indeed" -> activeSession?.indeedConnected ?: false
                "Naukri" -> activeSession?.naukriConnected ?: true
                else -> true
            }
        }

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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            topBar = {
                Column(
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
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
                                    .background(ElectricTeal),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = activeSession?.name?.take(2)?.uppercase() ?: "AK", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Disha",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = SpaceGrotesk,
                                fontSize = 24.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Settings navigation trigger
                            IconButton(
                                onClick = {
                                    haptics(Haptic.Click)
                                    onNavigateToTab(DishaScreen.RefinePreferences)
                                },
                                modifier = Modifier
                                    .background(CardTeal, CircleShape)
                                    .size(40.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Settings, contentDescription = "Refine Filter", tint = TextPrimary)
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = bottomNavBarClearance())
            ) {
                // Greetings Greeting Header
                item {
                    Column {
                        Text(
                            text = "Ready for your next\nleap, ${activeSession?.name?.split(" ")?.firstOrNull() ?: "Anubhav"}?",
                            color = TextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGrotesk,
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
                            onClick = {
                                haptics(Haptic.Click)
                                isFilterSidebarOpen = true
                            },
                            modifier = Modifier
                                .background(CardTeal, RoundedCornerShape(12.dp))
                                .size(52.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Menu, contentDescription = "Open Sidebar Filter", tint = ElectricTeal)
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
                            Text(text = "View all", color = ElectricTeal, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        val activeMatchesList = filteredJobs.filter { it.matchScore >= 90 }
                        if (activeMatchesList.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(DeepTeal),
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
                                            colors = CardDefaults.cardColors(containerColor = DeepTeal),
                                            modifier = Modifier
                                                .fillParentMaxWidth(0.82f)
                                                .clickable {
                                                    haptics(Haptic.Click)
                                                    onJobClick(match.id)
                                                },
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
                                                            onClick = {
                                                                haptics(Haptic.Click)
                                                                viewModel.toggleJobBookmark(match.id, !match.isBookmarked)
                                                            },
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
                                                                .background(SeafoamMint.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Text(text = "🔥 ${match.matchScore}%", color = SeafoamMint, fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                                                    Text(text = formatSalaryDisplay(match.salaryRange), color = TextSecondary, fontSize = 13.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Latest Aggregated Roles
                item {
                    Column {
                        Text(text = "Latest Job Matches", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(bottom = 12.dp))

                        filteredJobs.filter { it.matchScore < 90 }.forEachIndexed { index, cardJob ->
                            FramerEntrance(
                                index = index,
                                filterKey = filterKey,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = DeepTeal),
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, BorderHighlight),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            haptics(Haptic.Click)
                                            onJobClick(cardJob.id)
                                        }
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
                                                    .background(ElectricTeal.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(text = cardJob.company.take(2), color = ElectricTeal, fontWeight = FontWeight.Bold)
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column {
                                                Text(text = cardJob.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                                Text(text = "${cardJob.company} • ${cardJob.location}", color = TextSecondary, fontSize = 13.sp)

                                                Spacer(modifier = Modifier.height(4.dp))

                                                // Badge
                                                Box(
                                                    modifier = Modifier
                                                        .background(CardTeal, RoundedCornerShape(6.dp))
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
                                            Text(text = formatSalaryDisplay(cardJob.salaryRange), color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(
                                                onClick = {
                                                    haptics(Haptic.Click)
                                                    viewModel.toggleJobBookmark(cardJob.id, !cardJob.isBookmarked)
                                                },
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

        AppBottomNavigationBar(
            currentTab = DishaScreen.Dashboard,
            onSelectTab = { onNavigateToTab(it) },
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Sliding Sidebar Overlay Panel
        if (isFilterSidebarOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptics(Haptic.Click)
                        isFilterSidebarOpen = false
                    }
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
                        ) {
                            haptics(Haptic.Click)
                            isFilterSidebarOpen = false
                        }
                )

                Column(
                    modifier = Modifier
                        .width(310.dp)
                        .fillMaxHeight()
                        .background(DeepTeal)
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
                            IconButton(onClick = {
                                haptics(Haptic.Click)
                                isFilterSidebarOpen = false
                            }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextPrimary)
                            }
                        }

                        // Tab switcher inside Sidebar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CardTeal, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            listOf("Filters", "Saved Jobs").forEach { tab ->
                                val active = sidebarTab == tab
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) ElectricTeal else Color.Transparent)
                                        .clickable {
                                            if (!active) haptics(Haptic.Click)
                                            sidebarTab = tab
                                        }
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
                                            .background(if (active) ElectricTeal else CardTeal, RoundedCornerShape(12.dp))
                                            .border(1.dp, if (active) ElectricTeal else BorderHighlight, RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (!active) haptics(Haptic.Click)
                                                viewModel.setFilterLocation(loc)
                                            }
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
                                            .background(if (active) ElectricTeal else CardTeal, RoundedCornerShape(12.dp))
                                            .border(1.dp, if (active) ElectricTeal else BorderHighlight, RoundedCornerShape(12.dp))
                                            .clickable {
                                                if (!active) haptics(Haptic.Click)
                                                viewModel.setFilterIndustry(ind)
                                            }
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
                                onValueChangeFinished = { haptics(Haptic.Toggle) },
                                valueRange = 5f..100f,
                                colors = SliderDefaults.colors(
                                    activeTrackColor = ElectricTeal,
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
                                        .clickable {
                                            if (!active) haptics(Haptic.Click)
                                            viewModel.setSortOption(sortVal)
                                        }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = active,
                                        onClick = {
                                            if (!active) haptics(Haptic.Click)
                                            viewModel.setSortOption(sortVal)
                                        },
                                        colors = RadioButtonDefaults.colors(selectedColor = ElectricTeal)
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
                                            colors = CardDefaults.cardColors(containerColor = CardTeal),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    haptics(Haptic.Click)
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
                                                        onClick = {
                                                            haptics(Haptic.Click)
                                                            viewModel.toggleJobBookmark(bmJob.id, false)
                                                        },
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
                                                    Text(text = formatSalaryDisplay(bmJob.salaryRange), color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                                                    Box(
                                                        modifier = Modifier
                                                            .background(SeafoamMint.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(text = "🔥 ${bmJob.matchScore}%", color = SeafoamMint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                                            color = if (statusLabel == "Offer Received") SeafoamMint else ElectricTeal,
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
                                                                if (statusLabel == "Offer Received") SeafoamMint
                                                                else if (statusLabel == "Interviewing") SunsetOrange
                                                                else ElectricTeal
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
                                                            "Applied" -> ElectricTeal
                                                            "Interviewing" -> SunsetOrange
                                                            "Offer" -> SeafoamMint
                                                            else -> ElectricTeal
                                                        }

                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .clip(RoundedCornerShape(6.dp))
                                                                .background(if (isActive) activeColor else Color.Transparent)
                                                                .clickable {
                                                                    haptics(Haptic.Toggle)
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
                                onClick = {
                                    haptics(Haptic.Reject)
                                    viewModel.resetFilters()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftGray),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .border(1.dp, BorderHighlight, RoundedCornerShape(12.dp))
                            ) {
                                Text(text = "Reset", color = SunsetOrange, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    haptics(Haptic.Confirm)
                                    isFilterSidebarOpen = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text(text = "Apply", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Button(
                            onClick = {
                                haptics(Haptic.Click)
                                isFilterSidebarOpen = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
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
