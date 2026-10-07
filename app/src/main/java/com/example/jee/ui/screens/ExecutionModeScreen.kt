package com.example.jee.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import com.example.jee.data.JeeRepository
import com.example.jee.data.PlannerTask
import com.example.jee.data.TaskStatus
import com.example.jee.ui.components.CommandCard
import com.example.jee.ui.components.SubjectBadge
import com.example.jee.ui.components.TaskTypeBadge
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ExecutionModeScreen(
    repository: JeeRepository,
    initialTask: PlannerTask?,
    onExitExecutionMode: () -> Unit
) {
    val tasks by repository.tasks.collectAsState()

    // Active task
    val currentTask = initialTask ?: tasks.firstOrNull { it.status == TaskStatus.PENDING } ?: tasks.firstOrNull()
    val nextTask = tasks.filter { it.id != currentTask?.id && it.status == TaskStatus.PENDING }.firstOrNull()

    val totalDurationSeconds = (currentTask?.estimatedMinutes ?: 45) * 60
    var secondsRemaining by remember(currentTask) { mutableIntStateOf(totalDurationSeconds) }
    var isRunning by remember { mutableStateOf(false) }
    var showReflectionDialog by remember { mutableStateOf(false) }

    // Timer tick effect
    LaunchedEffect(isRunning, secondsRemaining) {
        if (isRunning && secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        } else if (isRunning && secondsRemaining == 0) {
            isRunning = false
            showReflectionDialog = true
        }
    }

    val progressFraction = if (totalDurationSeconds > 0) {
        1f - (secondsRemaining.toFloat() / totalDurationSeconds.toFloat())
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progressFraction, label = "focus_progress")

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (isRunning) EmeraldSuccess else AmberGold))
                    Text(
                        text = if (isRunning) "FOCUS SESSION ACTIVE" else "FOCUS PAUSED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRunning) EmeraldSuccess else AmberGold,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(onClick = onExitExecutionMode) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Focus", tint = TextMuted)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // CURRENT TASK INFO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                if (currentTask != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SubjectBadge(subject = currentTask.subject)
                        TaskTypeBadge(taskType = currentTask.taskType)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentTask.chapter,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentTask.topic,
                        fontSize = 15.sp,
                        color = CyanNeon,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text("No Pending Task Selected", fontSize = 18.sp, color = TextPrimary)
                }
            }

            // CIRCULAR / CENTER COUNTDOWN DISPLAY
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceElevated)
                    .border(4.dp, CyanNeon.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${(animatedProgress * 100).toInt()}% COMPLETED",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // CONTROLS (START, PAUSE, COMPLETE, SKIP, MOVE LATER)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primary Play / Pause & Complete
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { isRunning = !isRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) AmberGold else CyanNeon,
                            contentColor = DarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(52.dp)
                            .testTag("focus_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "PAUSE" else "START FOCUS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = {
                            isRunning = false
                            showReflectionDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = DarkBg
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("focus_complete_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("FINISH", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Secondary Controls: Skip / Move to Later
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (currentTask != null) {
                                repository.markTaskAsMissedAndReplan(currentTask.id)
                            }
                            onExitExecutionMode()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Skip / Missed", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            if (currentTask != null) {
                                repository.updateTaskStatus(currentTask.id, TaskStatus.MOVED)
                            }
                            onExitExecutionMode()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextMuted)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Move to Later", fontSize = 12.sp)
                    }
                }

                // NEXT TASK PREVIEW
                if (nextTask != null) {
                    CommandCard(
                        borderColor = DarkBorder,
                        backgroundColor = DarkSurfaceElevated,
                        contentPadding = PaddingValues(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("NEXT TASK IN QUEUE:", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Text("${nextTask.chapter}: ${nextTask.topic}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            }
                            Text("${nextTask.estimatedMinutes}m", fontSize = 12.sp, color = AmberGold)
                        }
                    }
                }
            }
        }
    }

    // REFLECTION DIALOG: "WHAT HAPPENED?"
    if (showReflectionDialog) {
        Dialog(onDismissRequest = { showReflectionDialog = false }) {
            CommandCard(
                borderColor = CyanNeon,
                backgroundColor = DarkSurface,
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "Session Debrief: What happened?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your response feeds into the adaptive workload calibration.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))

                val outcomes = listOf(
                    "Completed" to TaskStatus.COMPLETED,
                    "Partially completed" to TaskStatus.IN_PROGRESS,
                    "Could not start" to TaskStatus.MISSED,
                    "Too difficult (Needs theory review)" to TaskStatus.MOVED,
                    "Distracted / Low energy" to TaskStatus.MOVED
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    outcomes.forEach { (label, status) ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceHighlight)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    if (currentTask != null) {
                                        repository.updateTaskStatus(currentTask.id, status)
                                    }
                                    showReflectionDialog = false
                                    onExitExecutionMode()
                                }
                                .padding(12.dp)
                        ) {
                            Text(text = label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}
