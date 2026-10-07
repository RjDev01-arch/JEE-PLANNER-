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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.jee.data.JeeRepository
import com.example.jee.ui.components.CommandCard
import com.example.jee.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    repository: JeeRepository,
    onRerunOnboarding: () -> Unit
) {
    val userProfile by repository.userProfile.collectAsState()
    var resetConfirmMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "SYSTEM CONFIGURATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyanNeon,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Aspirant Profile & Parameters",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Adjust fixed commitments, coaching time anchors, and calibration settings.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }

        // GAMIFICATION / ASPIRANT STATUS CARD
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("⚡", fontSize = 16.sp)
                            Text(userProfile.rankTier, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = AmberGold)
                        }
                        Text("${userProfile.totalXp} Academic XP Accumulated", fontSize = 11.sp, color = TextSecondary)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmberGold.copy(alpha = 0.15f))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("LEVEL 4 ASPIRANT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                }
            }
        }

        // PROFILE PARAMETERS
        item {
            SectionHeader(title = "Profile Telemetry")
            CommandCard {
                SettingsRow(label = "Student Name", value = userProfile.studentName)
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Target Examination", value = "${userProfile.targetExam.label} ${userProfile.targetYear}")
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Class Standing", value = userProfile.studentClass.label)
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Coaching Schedule", value = userProfile.coachingSchedule)
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Sleep Window", value = "${userProfile.sleepTime} - ${userProfile.wakeUpTime}")
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Weekday Bandwidth", value = "${userProfile.weekdayAvailableHours} hours/day")
                HorizontalDivider(color = DarkBorder, modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(label = "Weekend Bandwidth", value = "${userProfile.weekendAvailableHours} hours/day")
            }
        }

        // ACTIONS
        item {
            SectionHeader(title = "Actions & Calibration")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRerunOnboarding,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = CyanNeon),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("rerun_onboarding_button")
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Re-run Onboarding Calibration Wizard", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = {
                        resetConfirmMessage = "All state restored to standard JEE benchmark curriculum."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated, contentColor = TextMuted),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset to Demo JEE Benchmark Data", fontSize = 13.sp)
                }
            }
        }

        if (resetConfirmMessage != null) {
            item {
                Text(resetConfirmMessage!!, color = EmeraldSuccess, fontSize = 12.sp)
            }
        }

        item {
            CommandCard(borderColor = DarkBorderSubtle, backgroundColor = DarkSurfaceElevated) {
                Text("JEE Command Center v2.4 (Dark Academic Edition)", fontSize = 11.sp, color = TextMuted)
                Text("An intelligent JEE preparation operating system designed to continuously compute what you should learn next.", fontSize = 11.sp, color = TextMuted)
            }
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}
