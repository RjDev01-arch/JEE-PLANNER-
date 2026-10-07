package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.jee.data.JeeRepository
import com.example.jee.data.TestAnalysisLog
import com.example.jee.ui.components.CommandCard
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun TestAnalysisDialog(
    repository: JeeRepository,
    testName: String,
    onDismiss: () -> Unit
) {
    var marksObtained by remember { mutableStateOf("194") }
    var totalMarks by remember { mutableStateOf("300") }
    var attempted by remember { mutableStateOf("62") }
    var correct by remember { mutableStateOf("51") }
    var incorrect by remember { mutableStateOf("11") }
    var unattempted by remember { mutableStateOf("13") }
    var timeSpent by remember { mutableStateOf("175") }
    var carelessMistakes by remember { mutableStateOf("4") }
    var conceptualMistakes by remember { mutableStateOf("5") }

    val marks = marksObtained.toIntOrNull() ?: 0
    val total = totalMarks.toIntOrNull() ?: 300
    val att = attempted.toIntOrNull() ?: 0
    val cor = correct.toIntOrNull() ?: 0
    val incorr = incorrect.toIntOrNull() ?: 0
    val careless = carelessMistakes.toIntOrNull() ?: 0

    val accuracy = if (att > 0) (cor.toFloat() / att.toFloat()) * 100f else 0f
    val attemptRate = if (total > 0) (att.toFloat() / 75f) * 100f else 0f
    val marksLostNegatives = incorr * 1 // in JEE Main, each wrong question loses 1 negative mark
    val marksLostCareless = careless * 5 // 4 marks lost from not getting it right + 1 negative mark

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            borderColor = CyanNeon,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text("Test Performance Diagnostic", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(testName, fontSize = 12.sp, color = AmberGold)
            Spacer(modifier = Modifier.height(12.dp))

            // Score inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = marksObtained,
                    onValueChange = { marksObtained = it },
                    label = { Text("Score") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f).testTag("test_score_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
                OutlinedTextField(
                    value = totalMarks,
                    onValueChange = { totalMarks = it },
                    label = { Text("Total") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Attempted / Correct / Incorrect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = attempted,
                    onValueChange = { attempted = it },
                    label = { Text("Attempted") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyanNeon, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
                OutlinedTextField(
                    value = correct,
                    onValueChange = { correct = it },
                    label = { Text("Correct") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = EmeraldSuccess, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
                OutlinedTextField(
                    value = incorrect,
                    onValueChange = { incorrect = it },
                    label = { Text("Wrong") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = RoseDanger, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = carelessMistakes,
                    onValueChange = { carelessMistakes = it },
                    label = { Text("Careless Qs") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberGold, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
                OutlinedTextField(
                    value = timeSpent,
                    onValueChange = { timeSpent = it },
                    label = { Text("Mins Spent") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoGlow, focusedContainerColor = DarkSurfaceElevated, unfocusedContainerColor = DarkSurfaceElevated)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // COMPUTED TELEMETRY
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceHighlight)
                    .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                    .padding(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("DIAGNOSTIC DERIVATIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanNeon, letterSpacing = 0.8.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Accuracy:", fontSize = 12.sp, color = TextSecondary)
                        Text("${String.format("%.1f", accuracy)}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (accuracy >= 80) EmeraldSuccess else AmberGold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Marks Lost to Negatives & Silly Errors:", fontSize = 12.sp, color = TextSecondary)
                        Text("-$marksLostCareless marks", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoseDanger)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Potential Unlocked Score:", fontSize = 12.sp, color = TextSecondary)
                        Text("${(marks + marksLostCareless).coerceAtMost(total)} / $total", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
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
                        val insight = "Your biggest bottleneck is not theory mastery. You lost $marksLostCareless marks to careless calculations in Physics and rushed attempts. Eliminating careless errors unlocks an immediate +${marksLostCareless} mark jump."
                        val result = TestAnalysisLog(
                            id = UUID.randomUUID().toString(),
                            testName = testName,
                            date = "Today",
                            totalMarks = total,
                            marksObtained = marks,
                            attempted = att,
                            correct = cor,
                            incorrect = incorr,
                            unattempted = 75 - att,
                            timeSpentMinutes = timeSpent.toIntOrNull() ?: 180,
                            sillyMistakesCount = careless,
                            conceptualMistakesCount = conceptualMistakes.toIntOrNull() ?: 3,
                            marksLostNegatives = marksLostNegatives,
                            marksLostCareless = marksLostCareless,
                            accuracyPercent = accuracy,
                            attemptRatePercent = attemptRate,
                            keyInsight = insight
                        )
                        repository.recordTestResult(result)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Analysis", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
