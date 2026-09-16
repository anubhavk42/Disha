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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingRefineScreen(viewModel: DishaViewModel, onDone: () -> Unit, onCancel: () -> Unit) {
    val rolesFlow by viewModel.preferenceRoles.collectAsState()
    val location by viewModel.selectedLocation.collectAsState()
    var newRoleName by remember { mutableStateOf("") }
    var minVal by remember { mutableStateOf(12f) }
    var maxVal by remember { mutableStateOf(45f) }

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
                Text(
                    text = "Cancel",
                    color = TextSecondary,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onCancel() }
                )
                Text(
                    text = "Refine Your Match Preferences",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(2f)
                )
                Spacer(modifier = Modifier.weight(1f))
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
                                .background(ElectricTeal)
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
                        colors = CardDefaults.cardColors(containerColor = DeepTeal),
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
                            val salaryRangeColors = SliderDefaults.colors(
                                activeTrackColor = ElectricTeal,
                                thumbColor = SunsetOrange
                            )
                            val startInteraction = remember { MutableInteractionSource() }
                            val endInteraction = remember { MutableInteractionSource() }
                            RangeSlider(
                                value = minVal..maxVal,
                                onValueChange = { range ->
                                    minVal = range.start
                                    maxVal = range.endInclusive
                                },
                                valueRange = 5f..100f,
                                colors = salaryRangeColors,
                                startInteractionSource = startInteraction,
                                endInteractionSource = endInteraction,
                                startThumb = {
                                    SliderDefaults.Thumb(
                                        interactionSource = startInteraction,
                                        colors = salaryRangeColors,
                                        thumbSize = DpSize(20.dp, 20.dp)
                                    )
                                },
                                endThumb = {
                                    SliderDefaults.Thumb(
                                        interactionSource = endInteraction,
                                        colors = salaryRangeColors,
                                        thumbSize = DpSize(20.dp, 20.dp)
                                    )
                                },
                                track = { rangeSliderState ->
                                    SliderDefaults.Track(
                                        rangeSliderState = rangeSliderState,
                                        colors = salaryRangeColors,
                                        drawStopIndicator = null
                                    )
                                }
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
