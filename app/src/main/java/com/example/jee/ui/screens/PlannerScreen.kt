package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.jee.data.*
import com.example.jee.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun PlannerScreen(
    repository: JeeRepository,
    onStartFocusSession: (PlannerTask) -> Unit,
    onOpenReplannerDialog: () -> Unit
) {
    val tasks by repository.tasks.collectAsState()
    var selectedDayFilter by remember { mutableIntStateOf(0) } // 0 = Today, 1 = Tomorrow, 2 = Week, -1 = All
    var selectedSubjectFilter by remember { mutableStateOf<SubjectType?>(null) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedDayFilter, selectedSubjectFilter) {
        tasks.filter { task ->
            val dayMatch = when (selectedDayFilter) {
                0 -> task.dayOffset == 0
                1 -> task.dayOffset == 1
                2 -> task.dayOffset >= 2
                else -> true
            }
            val subjectMatch = selectedSubjectFilter == null || task.subject == selectedSubjectFilter
            dayMatch && subjectMatch
        }
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = CyanNeon,
                contentColor = DarkBg,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_task_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("planner_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER
            item {
                Column {
                    Text(
                        text = "INTELLIGENT TIMETABLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Adaptive Task Planner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Prioritized against upcoming mock tests, syllabus weightage, and spaced revision.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // AUTO-REPLANNER ACTION STRIP
            item {
                CommandCard(
                    borderColor = IndigoGlow.copy(alpha = 0.5f),
                    backgroundColor = DarkSurfaceElevated,
                    contentPadding = PaddingValues(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.AutoMode, contentDescription = null, tint = IndigoGlow, modifier = Modifier.size(16.dp))
                            Text(
                                text = "AI Auto-Replanner Actions",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoGlow
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = onOpenReplannerDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight, contentColor = AmberGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reduce_load_button")
                        ) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reduce Load", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { repository.replanWeek() },
                            colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceHighlight, contentColor = CyanNeon),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("replan_week_button")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Replan Week", fontSize = 11.sp)
                        }
                    }
                }
            }

            // DAY SELECTOR TABS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceElevated)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val days = listOf(0 to "Today", 1 to "Tomorrow", 2 to "This Week", -1 to "All")
                    days.forEach { (offset, label) ->
                        val selected = selectedDayFilter == offset
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) CyanNeon else Color.Transparent)
                                .clickable { selectedDayFilter = offset }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) DarkBg else TextSecondary
                            )
                        }
                    }
                }
            }

            // SUBJECT FILTER CHIPS
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedSubjectFilter == null,
                            onClick = { selectedSubjectFilter = null },
                            label = { Text("All Subjects", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon
                            )
                        )
                    }
                    SubjectType.values().forEach { sub ->
                        item {
                            FilterChip(
                                selected = selectedSubjectFilter == sub,
                                onClick = { selectedSubjectFilter = if (selectedSubjectFilter == sub) null else sub },
                                label = { Text(sub.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = when (sub) {
                                        SubjectType.PHYSICS -> PhysicsColor.copy(alpha = 0.2f)
                                        SubjectType.CHEMISTRY -> ChemistryColor.copy(alpha = 0.2f)
                                        SubjectType.MATHEMATICS -> MathsColor.copy(alpha = 0.2f)
                                    },
                                    selectedLabelColor = when (sub) {
                                        SubjectType.PHYSICS -> PhysicsColor
                                        SubjectType.CHEMISTRY -> ChemistryColor
                                        SubjectType.MATHEMATICS -> MathsColor
                                    }
                                )
                            )
                        }
                    }
                }
            }

            // TASK LIST
            if (filteredTasks.isEmpty()) {
                item {
                    CommandCard(contentPadding = PaddingValues(24.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All Cleared for Selected View!", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Hit the + button to add an urgent practice task.", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    TaskPlannerCard(
                        task = task,
                        onComplete = {
                            repository.updateTaskStatus(
                                task.id,
                                if (task.status == TaskStatus.COMPLETED) TaskStatus.PENDING else TaskStatus.COMPLETED
                            )
                        },
                        onMissed = {
                            repository.markTaskAsMissedAndReplan(task.id)
                        },
                        onMove = {
                            val newTasks = tasks.map {
                                if (it.id == task.id) it.copy(dayOffset = it.dayOffset + 1, deadline = "Tomorrow") else it
                            }
                            repository.updateTaskStatus(task.id, TaskStatus.MOVED)
                        },
                        onDelete = { repository.deleteTask(task.id) },
                        onStartFocus = { onStartFocusSession(task) }
                    )
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            repository = repository,
            onDismiss = { showAddTaskDialog = false }
        )
    }
}

@Composable
private fun TaskPlannerCard(
    task: PlannerTask,
    onComplete: () -> Unit,
    onMissed: () -> Unit,
    onMove: () -> Unit,
    onDelete: () -> Unit,
    onStartFocus: () -> Unit
) {
    val isDone = task.status == TaskStatus.COMPLETED

    CommandCard(
        borderColor = if (isDone) EmeraldSuccess.copy(alpha = 0.3f) else DarkBorder,
        backgroundColor = if (isDone) DarkSurfaceElevated.copy(alpha = 0.5f) else DarkSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectBadge(subject = task.subject)
                    TaskTypeBadge(taskType = task.taskType)
                    PriorityBadge(priority = task.priority)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${task.chapter}: ${task.topic}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDone) TextMuted else TextPrimary
                )

                if (task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = task.note,
                        fontSize = 11.sp,
                        color = if (task.status == TaskStatus.MOVED || task.status == TaskStatus.MISSED) AmberGold else TextSecondary
                    )
                }
            }

            IconButton(onClick = onComplete) {
                Icon(
                    imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = if (isDone) EmeraldSuccess else TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metadata & Actions row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Schedule, contentDescription = null, tint = AmberGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${task.estimatedMinutes}m", fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.SemiBold)
                }

                Text("•", color = TextMuted)
                Text(task.deadline, fontSize = 11.sp, color = TextMuted)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onStartFocus, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Focus", tint = CyanNeon, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onMissed, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Snooze, contentDescription = "Missed", tint = AmberGold, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun AddTaskDialog(
    repository: JeeRepository,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf(SubjectType.PHYSICS) }
    var chapter by remember { mutableStateOf("Kinematics") }
    var topic by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf(TaskType.PYQ) }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var priority by remember { mutableStateOf(TaskPriority.HIGH) }

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyanNeon,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Add New JEE Study Block", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            // Subject selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SubjectType.values().forEach { sub ->
                    val selected = subject == sub
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) CyanNeon.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (selected) CyanNeon else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { subject = sub }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sub.displayName, fontSize = 11.sp, color = if (selected) CyanNeon else TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic / Focus Module") },
                placeholder = { Text("e.g. 20 PYQs or DPP 04") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_task_topic_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Duration: ${durationMinutes}m", fontSize = 13.sp, color = AmberGold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 45, 60, 90).forEach { mins ->
                        val selected = durationMinutes == mins
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) AmberGold else DarkSurfaceHighlight)
                                .clickable { durationMinutes = mins }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${mins}m", fontSize = 10.sp, color = if (selected) DarkBg else TextPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val task = PlannerTask(
                            id = UUID.randomUUID().toString(),
                            subject = subject,
                            chapter = chapter.ifBlank { "Core" },
                            topic = topic.ifBlank { "PYQ Practice Session" },
                            taskType = taskType,
                            estimatedMinutes = durationMinutes,
                            priority = priority,
                            deadline = "Today",
                            dayOffset = 0,
                            status = TaskStatus.PENDING
                        )
                        repository.addTask(task)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("submit_new_task_button")
                ) {
                    Text("Add to Schedule", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
