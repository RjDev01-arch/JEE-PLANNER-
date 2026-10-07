package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.example.jee.data.*
import com.example.jee.ui.components.*
import com.example.ui.theme.*

@Composable
fun DashboardScreen(
    repository: JeeRepository,
    onStartExecutionMode: (PlannerTask?) -> Unit,
    onOpenReplannerDialog: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    onNavigateToSyllabus: () -> Unit,
    onNavigateToBacklog: () -> Unit,
    onNavigateToTests: () -> Unit,
    onNavigateToAiCoach: () -> Unit
) {
    val userProfile by repository.userProfile.collectAsState()
    val tasks by repository.tasks.collectAsState()
    val backlogItems by repository.backlogItems.collectAsState()
    val tests by repository.tests.collectAsState()
    val recentReplanEvent by repository.recentReplanEvent.collectAsState()

    val overallMastery = repository.getOverallMastery()
    val phyMastery = repository.getSubjectMastery(SubjectType.PHYSICS)
    val chemMastery = repository.getSubjectMastery(SubjectType.CHEMISTRY)
    val mathMastery = repository.getSubjectMastery(SubjectType.MATHEMATICS)

    val upcomingTest = tests.firstOrNull { !it.isCompleted }
    val nextAction = remember(tasks, backlogItems) { repository.getNextActionRecommendation() }

    val criticalBacklogCount = backlogItems.count { it.priorityCategory == BacklogPriorityCategory.CRITICAL && it.status == TaskStatus.PENDING }
    val importantBacklogCount = backlogItems.count { it.priorityCategory == BacklogPriorityCategory.IMPORTANT && it.status == TaskStatus.PENDING }
    val optionalBacklogCount = backlogItems.count { it.priorityCategory == BacklogPriorityCategory.OPTIONAL && it.status == TaskStatus.PENDING }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP GREETING & EXAM COUNTDOWN HERO
        item {
            GlowingHeroCard(accentColor = CyanNeon) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess)
                            )
                            Text(
                                text = "SYSTEM ONLINE • ${userProfile.rankTier.uppercase()}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanNeon,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = userProfile.studentName,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${userProfile.studentClass.label} • ${userProfile.targetExam.label} ${userProfile.targetYear}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Countdown Badge
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceElevated)
                            .border(1.dp, CyanNeon.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "${userProfile.daysRemaining}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = AmberGold
                        )
                        Text(
                            text = "DAYS TO JEE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Streak & Study stats bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🔥", fontSize = 16.sp)
                        Column {
                            Text("${userProfile.currentStreakDays} Days", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                            Text("Consistency Streak", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.HourglassTop, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                        Column {
                            Text("4.5 / 5.5 hrs", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Today's Study Load", fontSize = 10.sp, color = TextMuted)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = IndigoGlow, modifier = Modifier.size(16.dp))
                        Column {
                            Text("185 Qs (65 PYQ)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("This Week", fontSize = 10.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        // REPLAN EVENT NOTIFICATION (IF ADJUSTED)
        if (recentReplanEvent != null) {
            item {
                CommandCard(
                    borderColor = IndigoGlow.copy(alpha = 0.6f),
                    backgroundColor = DarkSurfaceHighlight,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.AutoMode, contentDescription = null, tint = IndigoGlow, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Replan Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoGlow
                            )
                            Text(
                                text = recentReplanEvent!!.explanation,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // PROMINENT SECTION: "WHAT SHOULD I DO RIGHT NOW?"
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        Icons.Default.Radar,
                        contentDescription = null,
                        tint = CyanNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "WHAT SHOULD I DO RIGHT NOW?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                CommandCard(
                    borderColor = CyanNeon,
                    backgroundColor = DarkSurfaceElevated,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SubjectBadge(subject = nextAction.subject)
                            TaskTypeBadge(taskType = nextAction.taskType)
                        }
                        PriorityBadge(priority = nextAction.priority)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${nextAction.chapter} → ${nextAction.topic}",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${nextAction.estimatedMinutes} minutes allocation",
                            fontSize = 12.sp,
                            color = AmberGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = nextAction.rationale,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val task = tasks.find { it.id == nextAction.taskId }
                                onStartExecutionMode(task)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CyanNeon,
                                contentColor = DarkBg
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_now_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Focus Session", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                onOpenReplannerDialog()
                            },
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.testTag("defer_replan_button")
                        ) {
                            Icon(Icons.Default.SwapCalls, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replan")
                        }
                    }
                }
            }
        }

        // SYLLABUS MASTERY SUMMARY
        item {
            Column {
                SectionHeader(
                    title = "Syllabus Mastery",
                    subtitle = "Weighted for JEE Main & Advanced",
                    actionLabel = "View Tracker",
                    onActionClick = onNavigateToSyllabus
                )

                CommandCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Overall Syllabus Mastery",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "$overallMastery%",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    GlowProgressBar(progress = overallMastery / 100f, barColor = CyanNeon, height = 8.dp)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Subject Breakdown bars
                    SubjectMasteryRow(
                        name = "Physics",
                        mastery = phyMastery,
                        color = PhysicsColor,
                        focusChapter = "Current: Rotational Dynamics (42%)"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectMasteryRow(
                        name = "Chemistry",
                        mastery = chemMastery,
                        color = ChemistryColor,
                        focusChapter = "Current: Thermodynamics (50%)"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SubjectMasteryRow(
                        name = "Mathematics",
                        mastery = mathMastery,
                        color = MathsColor,
                        focusChapter = "Current: Functions & Calculus (70%)"
                    )
                }
            }
        }

        // UPCOMING TEST & BACKLOG INTEL (TWO COLUMNS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upcoming Test Card
                CommandCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (upcomingTest != null) RoseDanger.copy(alpha = 0.4f) else DarkBorder,
                    onClick = onNavigateToTests
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = RoseDanger, modifier = Modifier.size(14.dp))
                        Text(
                            text = "UPCOMING TEST",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoseDanger,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = upcomingTest?.name ?: "No test logged",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (upcomingTest != null) "In ${upcomingTest.daysUntil} days • ${upcomingTest.examType}" else "Tap to add test",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                // Backlog Summary Card
                CommandCard(
                    modifier = Modifier.weight(1f),
                    borderColor = if (criticalBacklogCount > 0) AmberGold.copy(alpha = 0.4f) else DarkBorder,
                    onClick = onNavigateToBacklog
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = AmberGold, modifier = Modifier.size(14.dp))
                        Text(
                            text = "BACKLOG ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$criticalBacklogCount Critical",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$importantBacklogCount Imp • $optionalBacklogCount Opt",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // QUICK ADAPTIVE REPLANNER BAR
        item {
            CommandCard(
                borderColor = DarkBorder,
                backgroundColor = DarkSurfaceHighlight
            ) {
                Text(
                    text = "ADAPTIVE REPLANNER CONTROLS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenReplannerDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp), tint = AmberGold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Lost Hours", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            repository.replanWeek()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp), tint = CyanNeon)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Replan Week", fontSize = 12.sp)
                    }

                    Button(
                        onClick = onNavigateToAiCoach,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(14.dp), tint = IndigoGlow)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ask Coach", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectMasteryRow(
    name: String,
    mastery: Int,
    color: Color,
    focusChapter: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            }
            Text("$mastery%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        GlowProgressBar(progress = mastery / 100f, barColor = color, height = 5.dp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(focusChapter, fontSize = 10.sp, color = TextMuted)
    }
}
