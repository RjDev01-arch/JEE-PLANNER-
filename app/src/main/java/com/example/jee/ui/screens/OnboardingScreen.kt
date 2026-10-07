package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.jee.data.ClassLevel
import com.example.jee.data.JeeRepository
import com.example.jee.data.TargetExamType
import com.example.jee.ui.components.CommandCard
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    repository: JeeRepository,
    onFinish: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Step 1: Basic Profile
    var studentName by remember { mutableStateOf("Arjun Verma") }
    var selectedClass by remember { mutableStateOf(ClassLevel.CLASS_12) }
    var selectedTargetExam by remember { mutableStateOf(TargetExamType.BOTH) }
    var targetYear by remember { mutableStateOf("2027") }

    // Step 2: Routine & Bandwidth
    var coachingSchedule by remember { mutableStateOf("Mon-Fri (4:00 PM - 8:00 PM)") }
    var schoolSchedule by remember { mutableStateOf("Mon-Fri (7:30 AM - 1:30 PM)") }
    var wakeUpTime by remember { mutableStateOf("06:00 AM") }
    var sleepTime by remember { mutableStateOf("11:30 PM") }
    var weekdayHours by remember { mutableFloatStateOf(5.5f) }
    var weekendHours by remember { mutableFloatStateOf(9.0f) }
    var sessionLength by remember { mutableIntStateOf(50) }

    // Step 3: Confidence & Calibration
    var phyConfidence by remember { mutableIntStateOf(4) }
    var chemConfidence by remember { mutableIntStateOf(3) }
    var mathConfidence by remember { mutableIntStateOf(4) }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "JEE COMMAND CENTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Aspirant Calibration Wizard",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = "Step $step of 3",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { step / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = CyanNeon,
                    trackColor = DarkSurfaceHighlight
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    OutlinedButton(
                        onClick = { step-- },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DarkBorder)),
                        modifier = Modifier.testTag("onboarding_back_button")
                    ) {
                        Text("Back")
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            repository.completeOnboarding(
                                name = studentName,
                                studentClass = selectedClass,
                                targetExam = selectedTargetExam,
                                targetYear = targetYear,
                                coachingSchedule = coachingSchedule,
                                schoolSchedule = schoolSchedule,
                                wakeUpTime = wakeUpTime,
                                sleepTime = sleepTime,
                                weekdayHours = weekdayHours,
                                weekendHours = weekendHours,
                                sessionLength = sessionLength,
                                phyConfidence = phyConfidence,
                                chemConfidence = chemConfidence,
                                mathConfidence = mathConfidence
                            )
                            onFinish()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = DarkBg
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (step == 3) "Launch Command Center" else "Continue",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (step == 3) Icons.Default.RocketLaunch else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (step) {
                1 -> {
                    item {
                        Text(
                            text = "Target Examination & Academic Profile",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Tell us your baseline so the system can compute exact milestone pacing.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Student Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("onboarding_name_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                    }

                    item {
                        Text("Current Academic Status", fontSize = 13.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ClassLevel.values().forEach { level ->
                                val selected = selectedClass == level
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (selected) CyanNeon.copy(alpha = 0.15f) else DarkSurface)
                                        .border(
                                            1.dp,
                                            if (selected) CyanNeon else DarkBorder,
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { selectedClass = level }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) CyanNeon else TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text("Target Examination", fontSize = 13.sp, color = TextSecondary)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            TargetExamType.values().forEach { exam ->
                                val selected = selectedTargetExam == exam
                                CommandCard(
                                    borderColor = if (selected) CyanNeon else DarkBorder,
                                    backgroundColor = if (selected) DarkSurfaceHighlight else DarkSurface,
                                    contentPadding = PaddingValues(14.dp),
                                    onClick = { selectedTargetExam = exam }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = exam.label,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) CyanNeon else TextPrimary
                                        )
                                        RadioButton(
                                            selected = selected,
                                            onClick = { selectedTargetExam = exam },
                                            colors = RadioButtonDefaults.colors(selectedColor = CyanNeon)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        OutlinedTextField(
                            value = targetYear,
                            onValueChange = { targetYear = it },
                            label = { Text("Target Attempt Year (e.g. 2027)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanNeon,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                    }
                }

                2 -> {
                    item {
                        Text(
                            text = "Daily Routine & Sustainable Study Hours",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "The replanner will protect your sleep and commitments automatically.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = coachingSchedule,
                            onValueChange = { coachingSchedule = it },
                            label = { Text("Coaching Hours (Protected)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoGlow,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = schoolSchedule,
                            onValueChange = { schoolSchedule = it },
                            label = { Text("School / Self-Study Morning Hours") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoGlow,
                                unfocusedBorderColor = DarkBorder,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            )
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = wakeUpTime,
                                onValueChange = { wakeUpTime = it },
                                label = { Text("Wake-up Time") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanNeon,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                )
                            )
                            OutlinedTextField(
                                value = sleepTime,
                                onValueChange = { sleepTime = it },
                                label = { Text("Sleep Time") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CyanNeon,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedContainerColor = DarkSurface,
                                    unfocusedContainerColor = DarkSurface
                                )
                            )
                        }
                    }

                    item {
                        CommandCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weekday Self-Study: ${String.format("%.1f", weekdayHours)} hrs", color = TextPrimary)
                                Text("Realistic Target", color = CyanNeon, fontSize = 12.sp)
                            }
                            Slider(
                                value = weekdayHours,
                                onValueChange = { weekdayHours = it },
                                valueRange = 2f..10f,
                                steps = 15,
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanNeon,
                                    activeTrackColor = CyanNeon,
                                    inactiveTrackColor = DarkSurfaceHighlight
                                )
                            )
                        }
                    }

                    item {
                        CommandCard(contentPadding = PaddingValues(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Weekend Self-Study: ${String.format("%.1f", weekendHours)} hrs", color = TextPrimary)
                                Text("Sprint Target", color = AmberGold, fontSize = 12.sp)
                            }
                            Slider(
                                value = weekendHours,
                                onValueChange = { weekendHours = it },
                                valueRange = 4f..14f,
                                steps = 19,
                                colors = SliderDefaults.colors(
                                    thumbColor = AmberGold,
                                    activeTrackColor = AmberGold,
                                    inactiveTrackColor = DarkSurfaceHighlight
                                )
                            )
                        }
                    }

                    item {
                        Text("Preferred Study Session Length", fontSize = 13.sp, color = TextSecondary)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(45, 50, 60, 90).forEach { mins ->
                                val selected = sessionLength == mins
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) CyanNeon.copy(alpha = 0.15f) else DarkSurface)
                                        .border(1.dp, if (selected) CyanNeon else DarkBorder, RoundedCornerShape(8.dp))
                                        .clickable { sessionLength = mins }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${mins}m",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) CyanNeon else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                3 -> {
                    item {
                        Text(
                            text = "Subject Confidence Calibration",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "The AI planner will prioritize weaker subjects and schedule appropriate question volume.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    item {
                        ConfidenceSliderCard(
                            subjectName = "Physics",
                            color = PhysicsColor,
                            confidence = phyConfidence,
                            onChanged = { phyConfidence = it }
                        )
                    }

                    item {
                        ConfidenceSliderCard(
                            subjectName = "Chemistry",
                            color = ChemistryColor,
                            confidence = chemConfidence,
                            onChanged = { chemConfidence = it }
                        )
                    }

                    item {
                        ConfidenceSliderCard(
                            subjectName = "Mathematics",
                            color = MathsColor,
                            confidence = mathConfidence,
                            onChanged = { mathConfidence = it }
                        )
                    }

                    item {
                        CommandCard(
                            borderColor = CyanNeon.copy(alpha = 0.4f),
                            backgroundColor = DarkSurfaceHighlight
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanNeon)
                                Column {
                                    Text(
                                        text = "Initial Roadmap Ready to Generate",
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "We will preload your full syllabus, backlog engine, and adaptive timetable.",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConfidenceSliderCard(
    subjectName: String,
    color: Color,
    confidence: Int,
    onChanged: (Int) -> Unit
) {
    CommandCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(subjectName, fontWeight = FontWeight.Bold, color = color)
            Text(
                text = when (confidence) {
                    1 -> "Needs Heavy Foundation"
                    2 -> "Below Average"
                    3 -> "Moderate (JEE Main level)"
                    4 -> "Strong (Comfortable with PYQs)"
                    else -> "Advanced (Top Rank Standard)"
                },
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        Slider(
            value = confidence.toFloat(),
            onValueChange = { onChanged(it.toInt()) },
            valueRange = 1f..5f,
            steps = 3,
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = DarkSurfaceHighlight
            )
        )
    }
}
