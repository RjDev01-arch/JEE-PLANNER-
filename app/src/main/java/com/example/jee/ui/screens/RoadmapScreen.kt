package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jee.data.JeeDemoData
import com.example.jee.data.JeeRepository
import com.example.jee.data.RoadmapPhase
import com.example.jee.ui.components.CommandCard
import com.example.jee.ui.components.GlowProgressBar
import com.example.jee.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun RoadmapScreen(
    repository: JeeRepository
) {
    val userProfile by repository.userProfile.collectAsState()
    val roadmapPhases = remember { JeeDemoData.initialRoadmapPhases() }
    var selectedPhase by remember { mutableStateOf<RoadmapPhase?>(roadmapPhases.first()) }
    var hierarchyLevel by remember { mutableStateOf("YEAR → MONTH") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("roadmap_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP INTRO
        item {
            Column {
                Text(
                    text = "STRATEGIC ARCHITECTURE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Master JEE Journey Roadmap",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "End-to-end flightpath to ${userProfile.targetExam.label} ${userProfile.targetYear}. Synchronized with testing cycles.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // HIERARCHY SELECTOR PILL
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf("YEAR → MONTH", "WEEK TARGETS", "STUDY SESSIONS").forEach { view ->
                    val isSelected = hierarchyLevel == view
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyanNeon else Color.Transparent)
                            .clickable { hierarchyLevel = view }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = view,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) DarkBg else TextSecondary
                        )
                    }
                }
            }
        }

        // SELECTED PHASE INSPECTOR / DRILL-DOWN MODAL
        if (selectedPhase != null) {
            item {
                CommandCard(
                    borderColor = CyanNeon,
                    backgroundColor = DarkSurfaceHighlight
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE MILESTONE DRILLDOWN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedPhase!!.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${selectedPhase!!.timeline} • ${selectedPhase!!.status}",
                                fontSize = 12.sp,
                                color = AmberGold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanNeon.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${selectedPhase!!.progressPercent}% Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    GlowProgressBar(progress = selectedPhase!!.progressPercent / 100f, barColor = CyanNeon)

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Goal Target: ${selectedPhase!!.milestoneGoal}",
                        fontSize = 12.sp,
                        color = TextHighlight,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Core Focus Areas:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        selectedPhase!!.focusChapters.forEach { chap ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DarkSurfaceElevated)
                                    .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(text = chap, fontSize = 10.sp, color = TextPrimary)
                            }
                        }
                    }
                }
            }
        }

        // TIMELINE ROADMAP NODES
        item {
            SectionHeader(
                title = "Milestone Sequence",
                subtitle = "Click any phase node to inspect roadmap metrics"
            )
        }

        items(roadmapPhases) { phase ->
            val isCurrent = phase.status == "IN_PROGRESS"
            val isCompleted = phase.status == "COMPLETED"
            val isSelected = selectedPhase?.id == phase.id

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedPhase = phase },
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Vertical Timeline Indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> EmeraldSuccess
                                    isCurrent -> CyanNeon
                                    else -> DarkSurfaceElevated
                                }
                            )
                            .border(
                                2.dp,
                                when {
                                    isCompleted -> EmeraldSuccess
                                    isCurrent -> CyanNeon
                                    else -> DarkBorder
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = DarkBg, modifier = Modifier.size(12.dp))
                        } else if (isCurrent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DarkBg)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(60.dp)
                            .background(if (isCompleted) EmeraldSuccess else DarkBorder)
                    )
                }

                // Phase Card
                CommandCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (isSelected) CyanNeon else if (isCurrent) IndigoGlow.copy(alpha = 0.5f) else DarkBorder,
                    backgroundColor = if (isSelected) DarkSurfaceElevated else DarkSurface,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = phase.timeline,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isCurrent) CyanNeon else TextMuted
                        )
                        Text(
                            text = if (isCompleted) "ACHIEVED" else if (isCurrent) "IN FLIGHT" else "PLANNED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) EmeraldSuccess else if (isCurrent) CyanNeon else TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phase.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = phase.milestoneGoal,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
