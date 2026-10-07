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
import androidx.compose.ui.window.Dialog
import com.example.jee.data.*
import com.example.jee.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun TestsScreen(
    repository: JeeRepository
) {
    val tests by repository.tests.collectAsState()
    val latestAnalysis by repository.latestTestAnalysis.collectAsState()
    var showAddTestDialog by remember { mutableStateOf(false) }
    var analyzingTestName by remember { mutableStateOf<String?>(null) }

    val upcoming = tests.filter { !it.isCompleted }
    val completed = tests.filter { it.isCompleted }

    Scaffold(
        containerColor = DarkBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTestDialog = true },
                containerColor = RoseDanger,
                contentColor = Color.White,
                modifier = Modifier
                    .padding(bottom = 68.dp)
                    .testTag("add_test_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Test")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("tests_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // HEADER
            item {
                Column {
                    Text(
                        text = "TEST SIMULATION & ANALYSIS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoseDanger,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Test Manager & Strategy",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Automatic D-7 to D+1 preparation roadmap generated for every scheduled exam.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // RECENT TEST DIAGNOSTIC INSIGHT CARD
            item {
                CommandCard(
                    borderColor = CyanNeon,
                    backgroundColor = DarkSurfaceHighlight
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Insights, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(16.dp))
                            Text("LATEST TEST POST-MORTEM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanNeon, letterSpacing = 0.5.sp)
                        }
                        Text(latestAnalysis.date, fontSize = 11.sp, color = TextMuted)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(latestAnalysis.testName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ScoreStatChip(label = "Score", value = "${latestAnalysis.marksObtained}/${latestAnalysis.totalMarks}", color = CyanNeon)
                        ScoreStatChip(label = "Accuracy", value = "${String.format("%.1f", latestAnalysis.accuracyPercent)}%", color = EmeraldSuccess)
                        ScoreStatChip(label = "Lost to Silly Errors", value = "-${latestAnalysis.marksLostCareless}", color = RoseDanger)
                        ScoreStatChip(label = "Negative Marks", value = "-${latestAnalysis.marksLostNegatives}", color = AmberGold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkSurfaceElevated)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "💡 Strategy Insight: ${latestAnalysis.keyInsight}",
                            fontSize = 12.sp,
                            color = TextHighlight,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // UPCOMING TESTS & AUTO-GENERATED PREP PLAN
            item {
                SectionHeader(
                    title = "Upcoming Test Pipeline",
                    subtitle = "Automated D-Day Preparation Schedule"
                )
            }

            if (upcoming.isEmpty()) {
                item {
                    CommandCard(contentPadding = PaddingValues(20.dp)) {
                        Text("No upcoming tests scheduled. Tap + below to add your next mock test.", color = TextSecondary, fontSize = 13.sp)
                    }
                }
            } else {
                items(upcoming) { test ->
                    CommandCard(
                        borderColor = RoseDanger.copy(alpha = 0.5f),
                        backgroundColor = DarkSurfaceElevated
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
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(RoseDanger.copy(alpha = 0.15f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(test.examType.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = RoseDanger)
                                    }
                                    Text("•", color = TextMuted)
                                    Text("${test.durationMinutes} mins", fontSize = 11.sp, color = TextMuted)
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(test.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(test.date, fontSize = 12.sp, color = AmberGold, fontWeight = FontWeight.SemiBold)
                            }

                            // Countdown Box
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceHighlight)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Text("${test.daysUntil}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = RoseDanger)
                                Text("DAYS", fontSize = 9.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Syllabus Topics list
                        Text("Syllabus Coverage:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
                        Spacer(modifier = Modifier.height(4.dp))
                        test.syllabusTopics.forEach { topic ->
                            Text("• $topic", fontSize = 11.sp, color = TextSecondary)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Prep Pipeline timeline preview
                        Text("AUTOMATIC PREP PROTOCOL:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon, letterSpacing = 0.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TestPrepStepRow(step = "D-7", desc = "Syllabus locked & weak chapters identified")
                            TestPrepStepRow(step = "D-3", desc = "High-yield PYQ sprint & formula sheets")
                            TestPrepStepRow(step = "D-1", desc = "Mistake Log review & sleep preservation")
                            TestPrepStepRow(step = "D+1", desc = "Test Post-Mortem & negative mark classification")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { analyzingTestName = test.name },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().testTag("enter_test_result_button")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Log Test Results & Deep Analysis", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // PREVIOUS COMPLETED TESTS
            if (completed.isNotEmpty()) {
                item {
                    SectionHeader(title = "Completed Tests History")
                }

                items(completed) { test ->
                    CommandCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(test.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                Text("${test.date} • ${test.examType}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text(
                                text = "${test.score ?: 0} / ${test.totalMarks}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddTestDialog) {
        AddTestDialog(
            repository = repository,
            onDismiss = { showAddTestDialog = false }
        )
    }

    if (analyzingTestName != null) {
        TestAnalysisDialog(
            repository = repository,
            testName = analyzingTestName!!,
            onDismiss = { analyzingTestName = null }
        )
    }
}

@Composable
private fun ScoreStatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = TextMuted)
    }
}

@Composable
private fun TestPrepStepRow(step: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(DarkSurfaceHighlight)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(step, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = CyanNeon)
        }
        Text(desc, fontSize = 11.sp, color = TextSecondary)
    }
}

@Composable
private fun AddTestDialog(
    repository: JeeRepository,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var examType by remember { mutableStateOf("JEE Main Full Mock") }
    var daysUntil by remember { mutableIntStateOf(7) }
    var syllabus by remember { mutableStateOf("Physics: Mechanics, Chem: Physical, Math: Algebra") }

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = RoseDanger,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Schedule New Test", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Test Name") },
                placeholder = { Text("e.g. Allen Leader CBT #05") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = syllabus,
                onValueChange = { syllabus = it },
                label = { Text("Syllabus Chapters") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Days Remaining: $daysUntil days", fontSize = 13.sp, color = AmberGold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(3, 5, 7, 14).forEach { d ->
                        val selected = daysUntil == d
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selected) AmberGold else DarkSurfaceHighlight)
                                .clickable { daysUntil = d }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("${d}d", fontSize = 10.sp, color = if (selected) DarkBg else TextPrimary)
                        }
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
                        val test = TestItem(
                            id = UUID.randomUUID().toString(),
                            name = name.ifBlank { "Upcoming Mock Test" },
                            date = "In $daysUntil days",
                            examType = examType,
                            durationMinutes = 180,
                            syllabusTopics = listOf(syllabus),
                            isCompleted = false,
                            daysUntil = daysUntil
                        )
                        repository.addTest(test)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseDanger, contentColor = Color.White),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Schedule Test", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
