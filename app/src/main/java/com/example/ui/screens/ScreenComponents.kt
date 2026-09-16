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
import androidx.compose.ui.draw.shadow
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.JobMatch
import com.example.ui.DishaViewModel
import com.example.ui.theme.*

fun formatSalaryDisplay(salaryRange: String): String {
    val (min, max) = getSalaryLpa(salaryRange)
    return "₹$min L - ₹$max L"
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

@Composable
fun CustomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(containerColor = ElectricTeal),
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
            .background(DeepTeal, RoundedCornerShape(16.dp))
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
                    .background(CardTeal, RoundedCornerShape(20.dp))
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

private val BottomNavBarHeight = 72.dp
private val BottomNavBarBottomMargin = 12.dp
private val BottomNavBarContentSpacing = 16.dp

/**
 * Extra bottom padding scrollable content behind [AppBottomNavigationBar] must reserve so its
 * last item is never obscured by the bar. The bar floats as an overlay outside of Scaffold's
 * `bottomBar` slot (it needs to sit above a Box shared with the rest of the screen for the Haze
 * blur to sample correctly), so Scaffold's own innerPadding does NOT account for it - callers
 * must add this manually to their scrollable content's bottom padding/contentPadding.
 */
@Composable
fun bottomNavBarClearance(): Dp {
    val navigationBarInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return navigationBarInset + BottomNavBarBottomMargin + BottomNavBarHeight + BottomNavBarContentSpacing
}

/**
 * Floating glassmorphic pill bottom bar. [hazeState] must be the same instance applied via
 * `Modifier.hazeSource(hazeState)` to the scrollable content behind this bar (see the screens
 * that host it), otherwise there is nothing for the blur to sample and it degrades to a plain
 * translucent scrim.
 */
@Composable
fun AppBottomNavigationBar(
    currentTab: DishaScreen,
    onSelectTab: (DishaScreen) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple("Dashboard", DishaScreen.Dashboard, Icons.Default.Home),
        Triple("CV", DishaScreen.CV, Icons.Default.Edit),
        Triple("Automate", DishaScreen.Automate, Icons.Default.Settings),
        Triple("Assist", DishaScreen.Assist, Icons.Default.Star),
        Triple("Profile", DishaScreen.Profile, Icons.Default.Person)
    )

    val pillShape = RoundedCornerShape(32.dp)
    val glassStyle = HazeStyle(
        backgroundColor = DeepTeal,
        tint = HazeDefaults.tint(DeepTeal),
        blurRadius = 20.dp,
        noiseFactor = HazeDefaults.noiseFactor
    )

    Row(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(start = 18.dp, end = 18.dp, bottom = BottomNavBarBottomMargin)
            .fillMaxWidth()
            .height(BottomNavBarHeight)
            .shadow(elevation = 16.dp, shape = pillShape, ambientColor = Color.Black.copy(alpha = 0.3f), spotColor = Color.Black.copy(alpha = 0.3f))
            .clip(pillShape)
            .hazeEffect(state = hazeState, style = glassStyle)
            .border(1.dp, Color.White.copy(alpha = 0.15f), pillShape),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { (label, route, icon) ->
            val selected = currentTab == route
            val highlightColor by animateColorAsState(
                targetValue = if (selected) ElectricTeal.copy(alpha = 0.18f) else Color.Transparent,
                label = "tab_highlight"
            )
            val contentColor by animateColorAsState(
                targetValue = if (selected) ElectricTeal else TextSecondary,
                label = "tab_content_color"
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(highlightColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectTab(route) }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = label,
                    color = contentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
