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
fun MistakesScreen(
    repository: JeeRepository
) {
    val mistakes by repository.mistakes.collectAsState()
    var selectedCategoryFilter by remember { mutableStateOf<MistakeCategory?>(null) }
    var showAddMistakeDialog by remember { mutableStateOf(false) }

    val filteredMistakes = remember(mistakes, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) mistakes else mistakes.filter { it.mistakeType == selectedCategoryFilter }
    }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddMistakeDialog = true },
                containerColor = RoseDanger,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_mistake_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Mistake")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("mistakes_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER
            item {
                Column {
                    Text(
                        text = "ERROR LOG & RETRIEVAL PRACTICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Mistake Database",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Convert test failures into marks. Review why errors happened and anchor the correct approach.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // RECURRING ERROR DETECTOR CALLOUT
            item {
                CommandCard(
                    borderColor = AmberGold,
                    backgroundColor = DarkSurfaceHighlight
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = AmberGold, modifier = Modifier.size(20.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECURRING PATTERN DETECTED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Calculation & sign errors occurred 7 times this month, mostly in Physics (Kinematics & NLM).",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Remedy: Always write vector components explicitly before calculating tangents.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            // FILTER TABS
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${mistakes.size})", fontSize = 11.sp) }
                    )
                    MistakeCategory.values().take(4).forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = if (selectedCategoryFilter == cat) null else cat },
                            label = { Text(cat.label, fontSize = 11.sp) }
                        )
                    }
                }
            }

            // MISTAKE LOGS
            items(filteredMistakes, key = { it.id }) { mistake ->
                CommandCard(
                    borderColor = if (mistake.isMastered) EmeraldSuccess.copy(alpha = 0.4f) else RoseDanger.copy(alpha = 0.3f),
                    backgroundColor = if (mistake.isMastered) DarkSurfaceElevated.copy(alpha = 0.5f) else DarkSurface
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                SubjectBadge(subject = mistake.subject)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(RoseDanger.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = mistake.mistakeType.label.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoseDanger
                                    )
                                }
                                Text("•", color = TextMuted)
                                Text(mistake.date, fontSize = 10.sp, color = TextMuted)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "${mistake.chapter}: ${mistake.questionSummary}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }

                        // Mastered button toggle
                        IconButton(onClick = { repository.toggleMistakeMastery(mistake.id) }) {
                            Icon(
                                imageVector = if (mistake.isMastered) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Mastered",
                                tint = if (mistake.isMastered) EmeraldSuccess else TextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Why it happened box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceHighlight)
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("❌ Why it happened:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = RoseDanger)
                            Text(mistake.whyItHappened, fontSize = 11.sp, color = TextPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Correct approach box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.1f))
                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("✅ Correct Approach:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = EmeraldSuccess)
                            Text(mistake.correctApproach, fontSize = 11.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }

    if (showAddMistakeDialog) {
        AddMistakeDialog(
            repository = repository,
            onDismiss = { showAddMistakeDialog = false }
        )
    }
}

@Composable
private fun AddMistakeDialog(
    repository: JeeRepository,
    onDismiss: () -> Unit
) {
    var subject by remember { mutableStateOf(SubjectType.PHYSICS) }
    var chapter by remember { mutableStateOf("") }
    var questionSummary by remember { mutableStateOf("") }
    var mistakeType by remember { mutableStateOf(MistakeCategory.CALCULATION) }
    var whyItHappened by remember { mutableStateOf("") }
    var correctApproach by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = RoseDanger,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Log Question Mistake", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(10.dp))

            // Subject selector
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = chapter,
                onValueChange = { chapter = it },
                label = { Text("Chapter") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = questionSummary,
                onValueChange = { questionSummary = it },
                label = { Text("Question Summary") },
                placeholder = { Text("e.g. River boat angle with vertical") },
                modifier = Modifier.fillMaxWidth().testTag("mistake_summary_input"),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = whyItHappened,
                onValueChange = { whyItHappened = it },
                label = { Text("Why did you get it wrong?") },
                placeholder = { Text("e.g. Inverted formula in hurry") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = correctApproach,
                onValueChange = { correctApproach = it },
                label = { Text("Correct Method / Principle") },
                placeholder = { Text("e.g. Always resolve along axes first") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val record = MistakeRecord(
                            id = UUID.randomUUID().toString(),
                            questionSummary = questionSummary.ifBlank { "Numerical question" },
                            subject = subject,
                            chapter = chapter.ifBlank { "Mechanics" },
                            mistakeType = mistakeType,
                            whyItHappened = whyItHappened.ifBlank { "Miscalculated in hurry" },
                            correctApproach = correctApproach.ifBlank { "Careful algebraic expansion" },
                            date = "Today",
                            isMastered = false
                        )
                        repository.logMistake(record)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Log Mistake", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
