package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.jee.data.JeeRepository
import com.example.jee.data.SubjectType
import com.example.jee.data.TaskStatus
import com.example.jee.ui.components.*
import com.example.ui.theme.*

@Composable
fun AnalyticsScreen(
    repository: JeeRepository
) {
    val userProfile by repository.userProfile.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val backlogItems by repository.backlogItems.collectAsState()
    val chapters by repository.chapters.collectAsState()

    val completedTasksCount = tasks.count { it.status == TaskStatus.COMPLETED }
    val totalTasksCount = tasks.size.coerceAtLeast(1)
    val taskCompletionRate = ((completedTasksCount.toFloat() / totalTasksCount.toFloat()) * 100f).toInt()

    val phyMastery = repository.getSubjectMastery(SubjectType.PHYSICS)
    val chemMastery = repository.getSubjectMastery(SubjectType.CHEMISTRY)
    val mathMastery = repository.getSubjectMastery(SubjectType.MATHEMATICS)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("analytics_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER
        item {
            Column {
                Text(
                    text = "ACADEMIC TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Performance Diagnostics",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "High-precision analysis of your problem volume, hour efficiency, and score trends.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // TOP METRICS ROW
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMetricBox(
                    label = "Completion Rate",
                    value = "$taskCompletionRate%",
                    subtext = "$completedTasksCount / $totalTasksCount Tasks",
                    accentColor = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                StatMetricBox(
                    label = "Questions Solved",
                    value = "185",
                    subtext = "65 PYQs • 120 DPP",
                    accentColor = CyanNeon,
                    modifier = Modifier.weight(1f)
                )
                StatMetricBox(
                    label = "Study Hours",
                    value = "34.5 hrs",
                    subtext = "Planned: 38 hrs",
                    accentColor = AmberGold,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // PLANNED VS ACTUAL STUDY HOURS (BAR COMPARISON)
        item {
            CommandCard(
                borderColor = IndigoGlow.copy(alpha = 0.4f),
                backgroundColor = DarkSurfaceElevated
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Planned vs Actual Study Hours (This Week)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("89% Efficiency", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }

                Spacer(modifier = Modifier.height(14.dp))

                val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                val planned = listOf(5.5f, 5.5f, 5.5f, 5.5f, 5.5f, 9.0f, 9.0f)
                val actual = listOf(5.0f, 4.5f, 5.5f, 3.5f, 5.0f, 8.0f, 3.0f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        val p = planned[index]
                        val a = actual[index]

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                // Planned bar
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height((p * 11).dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(DarkSurfaceHighlight)
                                )
                                // Actual bar
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .height((a * 11).dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(if (a >= p * 0.8f) CyanNeon else AmberGold)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(day, fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DarkSurfaceHighlight))
                        Text("Planned", fontSize = 10.sp, color = TextMuted)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanNeon))
                        Text("Actual", fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }

        // SUBJECT BALANCE RATIO
        item {
            CommandCard {
                Text("Subject Time Allocation Ratio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    Box(modifier = Modifier.weight(0.38f).fillMaxHeight().background(PhysicsColor))
                    Box(modifier = Modifier.weight(0.32f).fillMaxHeight().background(ChemistryColor))
                    Box(modifier = Modifier.weight(0.30f).fillMaxHeight().background(MathsColor))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SubjectRatioLegend(subject = "Physics (38%)", color = PhysicsColor, mastery = phyMastery)
                    SubjectRatioLegend(subject = "Chemistry (32%)", color = ChemistryColor, mastery = chemMastery)
                    SubjectRatioLegend(subject = "Maths (30%)", color = MathsColor, mastery = mathMastery)
                }
            }
        }

        // STRATEGIC INTELLIGENCE REPORT
        item {
            SectionHeader(title = "Weekly Strategic Intelligence Report")
        }

        item {
            ReportCardItem(
                title = "WHAT IMPROVED",
                desc = "Kinematics 2D Relative Motion accuracy jumped from 58% to 84%. Daily problem-solving cadence maintained for 14 straight days.",
                color = EmeraldSuccess,
                icon = Icons.Default.TrendingUp
            )
        }

        item {
            ReportCardItem(
                title = "WHAT GOT WORSE",
                desc = "Chemistry problem speed dipped on Thursday; spent 75 minutes on Thermodynamics enthalpy numericals with sign convention confusion.",
                color = RoseDanger,
                icon = Icons.Default.TrendingDown
            )
        }

        item {
            ReportCardItem(
                title = "BIGGEST BOTTLENECK",
                desc = "Center of Mass backlog is blocking Rotational Dynamics progress. Must clear before Sunday's Mechanics part test.",
                color = AmberGold,
                icon = Icons.Default.Block
            )
        }

        item {
            ReportCardItem(
                title = "NEXT WEEK'S PRIORITY",
                desc = "Execute 4 focused Backlog Attack sessions (4x60m). Complete 50 timed PYQs in Chemical Bonding and Functions.",
                color = CyanNeon,
                icon = Icons.Default.Flag
            )
        }
    }
}

@Composable
private fun SubjectRatioLegend(subject: String, color: Color, mastery: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Column {
            Text(subject, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text("$mastery% Mastery", fontSize = 10.sp, color = color)
        }
    }
}

@Composable
private fun ReportCardItem(title: String, desc: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    CommandCard(
        borderColor = color.copy(alpha = 0.4f),
        backgroundColor = DarkSurfaceElevated,
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            Column {
                Text(text = title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = desc, fontSize = 12.sp, color = TextPrimary, lineHeight = 16.sp)
            }
        }
    }
}
