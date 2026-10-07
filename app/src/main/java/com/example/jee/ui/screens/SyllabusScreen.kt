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

@Composable
fun SyllabusScreen(
    repository: JeeRepository
) {
    val chapters by repository.chapters.collectAsState()
    var selectedSubject by remember { mutableStateOf(SubjectType.PHYSICS) }
    var selectedClassFilter by remember { mutableStateOf<ClassLevel?>(null) }
    var editingChapter by remember { mutableStateOf<JeeChapter?>(null) }

    val filteredChapters = remember(chapters, selectedSubject, selectedClassFilter) {
        chapters.filter { ch ->
            val subMatch = ch.subject == selectedSubject
            val classMatch = selectedClassFilter == null || ch.classLevel == selectedClassFilter
            subMatch && classMatch
        }
    }

    val subjectMastery = repository.getSubjectMastery(selectedSubject)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("syllabus_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // TOP HEADER
        item {
            Column {
                Text(
                    text = "SYLLABUS MASTERY ENGINE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "JEE Chapter Matrix",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Mastery is multi-dimensional: Theory + PYQs + Revisions + Test performance.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // SUBJECT TABS (PHYSICS / CHEMISTRY / MATHEMATICS)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurfaceElevated)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubjectType.values().forEach { sub ->
                    val isSelected = selectedSubject == sub
                    val (color, label) = when (sub) {
                        SubjectType.PHYSICS -> PhysicsColor to "Physics"
                        SubjectType.CHEMISTRY -> ChemistryColor to "Chemistry"
                        SubjectType.MATHEMATICS -> MathsColor to "Maths"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
                            .border(1.dp, if (isSelected) color else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { selectedSubject = sub }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) color else TextSecondary
                        )
                    }
                }
            }
        }

        // SUBJECT MASTERY SUMMARY CARD
        item {
            CommandCard(
                borderColor = when (selectedSubject) {
                    SubjectType.PHYSICS -> PhysicsColor.copy(alpha = 0.4f)
                    SubjectType.CHEMISTRY -> ChemistryColor.copy(alpha = 0.4f)
                    SubjectType.MATHEMATICS -> MathsColor.copy(alpha = 0.4f)
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${selectedSubject.displayName} Overall Standing",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${filteredChapters.size} Chapters Tracked",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "$subjectMastery% Mastery",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (selectedSubject) {
                            SubjectType.PHYSICS -> PhysicsColor
                            SubjectType.CHEMISTRY -> ChemistryColor
                            SubjectType.MATHEMATICS -> MathsColor
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                GlowProgressBar(
                    progress = subjectMastery / 100f,
                    barColor = when (selectedSubject) {
                        SubjectType.PHYSICS -> PhysicsColor
                        SubjectType.CHEMISTRY -> ChemistryColor
                        SubjectType.MATHEMATICS -> MathsColor
                    }
                )
            }
        }

        // CLASS 11 / 12 TOGGLE
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedClassFilter == null,
                    onClick = { selectedClassFilter = null },
                    label = { Text("All Classes", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedClassFilter == ClassLevel.CLASS_11,
                    onClick = { selectedClassFilter = if (selectedClassFilter == ClassLevel.CLASS_11) null else ClassLevel.CLASS_11 },
                    label = { Text("Class 11", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedClassFilter == ClassLevel.CLASS_12,
                    onClick = { selectedClassFilter = if (selectedClassFilter == ClassLevel.CLASS_12) null else ClassLevel.CLASS_12 },
                    label = { Text("Class 12", fontSize = 11.sp) }
                )
            }
        }

        // CHAPTER CARDS
        items(filteredChapters, key = { it.id }) { chapter ->
            ChapterMasteryCard(
                chapter = chapter,
                onClick = { editingChapter = chapter }
            )
        }
    }

    if (editingChapter != null) {
        ChapterProgressEditorDialog(
            chapter = editingChapter!!,
            onDismiss = { editingChapter = null },
            onSave = { theory, basicQ, pyqMain, pyqAdv, rev1, rev2 ->
                repository.updateChapterProgress(
                    chapterId = editingChapter!!.id,
                    theory = theory,
                    basicQ = basicQ,
                    pyqMain = pyqMain,
                    pyqAdv = pyqAdv,
                    rev1 = rev1,
                    rev2 = rev2
                )
                editingChapter = null
            }
        )
    }
}

@Composable
private fun ChapterMasteryCard(
    chapter: JeeChapter,
    onClick: () -> Unit
) {
    CommandCard(
        borderColor = if (chapter.isWeak) RoseDanger.copy(alpha = 0.4f) else DarkBorder,
        onClick = onClick
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
                    Text(
                        text = chapter.classLevel.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted
                    )
                    Text("•", color = TextMuted)
                    Text(
                        text = "Weightage: ${chapter.weightageScore}/10",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberGold
                    )
                    if (chapter.isBacklog) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(RoseDanger.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("BACKLOG", fontSize = 9.sp, color = RoseDanger, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chapter.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Overall Mastery Ring/Box
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${chapter.overallMastery}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (chapter.overallMastery >= 70) EmeraldSuccess else if (chapter.overallMastery >= 50) AmberGold else RoseDanger
                )
                Text(
                    text = "Mastery",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-dimensional breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MetricSubProgress(label = "Theory", percent = chapter.theoryPercent)
            MetricSubProgress(label = "Questions", percent = chapter.basicQuestionsPercent)
            MetricSubProgress(label = "PYQs", percent = chapter.pyqMainPercent)
            MetricSubProgress(label = "Revision", percent = chapter.revision1Percent)
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to update progress & test log",
            fontSize = 10.sp,
            color = CyanNeon,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun MetricSubProgress(label: String, percent: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "$percent%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (percent >= 60) TextPrimary else TextMuted
        )
    }
}

@Composable
private fun ChapterProgressEditorDialog(
    chapter: JeeChapter,
    onDismiss: () -> Unit,
    onSave: (theory: Int, basicQ: Int, pyqMain: Int, pyqAdv: Int, rev1: Int, rev2: Int) -> Unit
) {
    var theory by remember { mutableFloatStateOf(chapter.theoryPercent.toFloat()) }
    var basicQ by remember { mutableFloatStateOf(chapter.basicQuestionsPercent.toFloat()) }
    var pyqMain by remember { mutableFloatStateOf(chapter.pyqMainPercent.toFloat()) }
    var pyqAdv by remember { mutableFloatStateOf(chapter.pyqAdvPercent.toFloat()) }
    var rev1 by remember { mutableFloatStateOf(chapter.revision1Percent.toFloat()) }
    var rev2 by remember { mutableFloatStateOf(chapter.revision2Percent.toFloat()) }

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = CyanNeon,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(18.dp)
        ) {
            Text(
                text = "Update Progress: ${chapter.name}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            ProgressSlider(label = "Theory & Lectures", value = theory, onChanged = { theory = it }, color = CyanNeon)
            ProgressSlider(label = "Basic Questions & DPPs", value = basicQ, onChanged = { basicQ = it }, color = IndigoGlow)
            ProgressSlider(label = "JEE Main PYQs", value = pyqMain, onChanged = { pyqMain = it }, color = AmberGold)
            ProgressSlider(label = "JEE Advanced PYQs", value = pyqAdv, onChanged = { pyqAdv = it }, color = VioletElectric)
            ProgressSlider(label = "Revision 1 Spaced Recall", value = rev1, onChanged = { rev1 = it }, color = EmeraldSuccess)
            ProgressSlider(label = "Revision 2 Final Formula Sheet", value = rev2, onChanged = { rev2 = it }, color = EmeraldSuccess)

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
                        onSave(theory.toInt(), basicQ.toInt(), pyqMain.toInt(), pyqAdv.toInt(), rev1.toInt(), rev2.toInt())
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = DarkBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Mastery", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProgressSlider(
    label: String,
    value: Float,
    onChanged: (Float) -> Unit,
    color: Color
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextSecondary)
            Text("${value.toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Slider(
            value = value,
            onValueChange = onChanged,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}
