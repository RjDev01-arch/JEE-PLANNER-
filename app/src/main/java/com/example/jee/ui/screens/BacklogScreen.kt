package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
fun BacklogScreen(
    repository: JeeRepository,
    onStartFocusSession: (PlannerTask) -> Unit
) {
    val backlogItems by repository.backlogItems.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf<BacklogPriorityCategory?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val pendingBacklogs = backlogItems.filter { it.status != TaskStatus.COMPLETED }
    val criticalCount = pendingBacklogs.count { it.priorityCategory == BacklogPriorityCategory.CRITICAL }
    val importantCount = pendingBacklogs.count { it.priorityCategory == BacklogPriorityCategory.IMPORTANT }
    val optionalCount = pendingBacklogs.count { it.priorityCategory == BacklogPriorityCategory.OPTIONAL }
    val totalHoursNeeded = pendingBacklogs.sumOf { it.estimatedMinutes } / 60.0

    val attackPlan = remember { repository.getBacklogAttackPlan() }

    val filteredItems = remember(backlogItems, selectedCategoryFilter) {
        pendingBacklogs.filter {
            selectedCategoryFilter == null || it.priorityCategory == selectedCategoryFilter
        }
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AmberGold,
                contentColor = DarkBg,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_backlog_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Backlog")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("backlog_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER
            item {
                Column {
                    Text(
                        text = "BACKLOG ELIMINATION ENGINE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Triage & Attack Planner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Prioritized by prerequisite dependency, JEE weightage, and upcoming mock test relevance.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // SUMMARY METRICS (TOTAL, CRITICAL, IMPORTANT, OPTIONAL)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatMetricBox(
                        label = "Total Backlog",
                        value = "${String.format("%.1f", totalHoursNeeded)} hrs",
                        subtext = "${pendingBacklogs.size} Chapters",
                        accentColor = AmberGold,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricBox(
                        label = "Critical",
                        value = "$criticalCount",
                        subtext = "Must clear first",
                        accentColor = RoseDanger,
                        modifier = Modifier.weight(1f)
                    )
                    StatMetricBox(
                        label = "Important",
                        value = "$importantCount",
                        subtext = "High scoring",
                        accentColor = IndigoGlow,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // "BACKLOG ATTACK PLAN" CARD
            item {
                CommandCard(
                    borderColor = AmberGold,
                    backgroundColor = DarkSurfaceElevated
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
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = AmberGold)
                            Text(
                                text = "BACKLOG ATTACK PLAN",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = "Daily Slotting",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Automated secondary evening blocks to eliminate debt without derailing current syllabus:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        attackPlan.forEach { slot ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceHighlight)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(slot.dayLabel, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = AmberGold)
                                        SubjectBadge(subject = slot.subject)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("${slot.chapter}: ${slot.topic}", fontSize = 12.sp, color = TextPrimary)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${slot.durationMinutes}m", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                                    Text(slot.timeSlot, fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }
            }

            // CATEGORY FILTER TABS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${pendingBacklogs.size})", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == BacklogPriorityCategory.CRITICAL,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == BacklogPriorityCategory.CRITICAL) null else BacklogPriorityCategory.CRITICAL },
                        label = { Text("Critical ($criticalCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoseDanger.copy(alpha = 0.2f),
                            selectedLabelColor = RoseDanger
                        )
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == BacklogPriorityCategory.IMPORTANT,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == BacklogPriorityCategory.IMPORTANT) null else BacklogPriorityCategory.IMPORTANT },
                        label = { Text("Important ($importantCount)", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndigoGlow.copy(alpha = 0.2f),
                            selectedLabelColor = IndigoGlow
                        )
                    )
                    FilterChip(
                        selected = selectedCategoryFilter == BacklogPriorityCategory.OPTIONAL,
                        onClick = { selectedCategoryFilter = if (selectedCategoryFilter == BacklogPriorityCategory.OPTIONAL) null else BacklogPriorityCategory.OPTIONAL },
                        label = { Text("Optional ($optionalCount)", fontSize = 11.sp) }
                    )
                }
            }

            // BACKLOG ITEMS LIST
            items(filteredItems, key = { it.id }) { item ->
                CommandCard(
                    borderColor = when (item.priorityCategory) {
                        BacklogPriorityCategory.CRITICAL -> RoseDanger.copy(alpha = 0.4f)
                        BacklogPriorityCategory.IMPORTANT -> IndigoGlow.copy(alpha = 0.4f)
                        BacklogPriorityCategory.OPTIONAL -> DarkBorder
                    }
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
                                SubjectBadge(subject = item.subject)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when (item.priorityCategory) {
                                                BacklogPriorityCategory.CRITICAL -> RoseDanger.copy(alpha = 0.15f)
                                                BacklogPriorityCategory.IMPORTANT -> IndigoGlow.copy(alpha = 0.15f)
                                                BacklogPriorityCategory.OPTIONAL -> TextMuted.copy(alpha = 0.15f)
                                            }
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.priorityCategory.label.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (item.priorityCategory) {
                                            BacklogPriorityCategory.CRITICAL -> RoseDanger
                                            BacklogPriorityCategory.IMPORTANT -> IndigoGlow
                                            BacklogPriorityCategory.OPTIONAL -> TextMuted
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${item.chapter}: ${item.topic}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "${item.estimatedMinutes}m needed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Prerequisite Impact: ${item.prerequisiteReason}",
                        fontSize = 11.sp,
                        color = TextHighlight,
                        lineHeight = 15.sp
                    )
                    Text(
                        text = "Exam Weightage: ${item.testRelevance}",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                repository.convertBacklogToTodayTask(item)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Schedule Today", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                repository.resolveBacklogItem(item.id)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Mark Cleared", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBacklogDialog(
            repository = repository,
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun AddBacklogDialog(
    repository: JeeRepository,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf(SubjectType.PHYSICS) }
    var chapter by remember { mutableStateOf("") }
    var topic by remember { mutableStateOf("") }
    var priorityCategory by remember { mutableStateOf(BacklogPriorityCategory.CRITICAL) }
    var estimatedMinutes by remember { mutableIntStateOf(90) }
    var prerequisiteReason by remember { mutableStateOf("Foundation for next block of chapters") }
    var testRelevance by remember { mutableStateOf("High yield in upcoming mock tests") }

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = AmberGold,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Register Backlog Item", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            // Subject
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
                            .background(if (selected) AmberGold.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (selected) AmberGold else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { subject = sub }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(sub.displayName, fontSize = 11.sp, color = if (selected) AmberGold else TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Weak Topic") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AmberGold,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Priority Category
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BacklogPriorityCategory.values().forEach { cat ->
                    val selected = priorityCategory == cat
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) RoseDanger.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .border(1.dp, if (selected) RoseDanger else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { priorityCategory = cat }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(cat.label, fontSize = 11.sp, color = if (selected) RoseDanger else TextPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val item = BacklogItem(
                            id = UUID.randomUUID().toString(),
                            subject = subject,
                            chapter = chapter.ifBlank { "Mechanics" },
                            topic = topic.ifBlank { "Unfinished Module" },
                            priorityCategory = priorityCategory,
                            estimatedMinutes = estimatedMinutes,
                            prerequisiteReason = prerequisiteReason,
                            testRelevance = testRelevance
                        )
                        repository.addBacklogItem(item)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold, contentColor = DarkBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Backlog", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
