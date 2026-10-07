package com.example.jee.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
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
import com.example.jee.ui.components.CommandCard
import com.example.ui.theme.*

@Composable
fun AutoReplannerDialog(
    repository: JeeRepository,
    onDismiss: () -> Unit
) {
    var hoursLost by remember { mutableFloatStateOf(2.5f) }
    var selectedReason by remember { mutableStateOf("Lost study hours due to unexpected delay") }

    val presetReasons = listOf(
        "I lost study hours today",
        "Coaching class overran schedule",
        "Low energy / feeling unwell",
        "Stuck on difficult numerical problems",
        "School practical / exam commitment"
    )

    Dialog(onDismissRequest = onDismiss) {
        CommandCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = IndigoGlow,
            backgroundColor = DarkSurface,
            contentPadding = PaddingValues(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.AutoMode, contentDescription = null, tint = CyanNeon)
                    Column {
                        Text(
                            text = "AI AUTO-REPLANNER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Adaptive Load Balancing",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "When you fall behind, the system adapts intelligently instead of overloading tomorrow. It strictly preserves sleep, coaching, and upcoming mock test prerequisites.",
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // SLIDER FOR HOURS LOST
            CommandCard(
                borderColor = DarkBorder,
                backgroundColor = DarkSurfaceElevated,
                contentPadding = PaddingValues(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Hours Lost Today:", fontSize = 13.sp, color = TextPrimary)
                    Text("${String.format("%.1f", hoursLost)} hours", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                }
                Slider(
                    value = hoursLost,
                    onValueChange = { hoursLost = it },
                    valueRange = 0.5f..5.0f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = AmberGold,
                        activeTrackColor = AmberGold,
                        inactiveTrackColor = DarkSurfaceHighlight
                    ),
                    modifier = Modifier.testTag("hours_lost_slider")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Select Trigger Context:", fontSize = 12.sp, color = TextSecondary)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                presetReasons.take(3).forEach { reason ->
                    val selected = selectedReason == reason
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) CyanNeon.copy(alpha = 0.12f) else DarkSurfaceElevated)
                            .border(1.dp, if (selected) CyanNeon else DarkBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = reason,
                            fontSize = 12.sp,
                            color = if (selected) CyanNeon else TextPrimary,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PROTECTED BLOCKS CALLOUT
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceHighlight)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                Text(
                    text = "Protected: Sleep (11:30 PM - 6:00 AM) & Coaching block are strictly preserved.",
                    fontSize = 11.sp,
                    color = EmeraldSuccess,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        repository.reduceTodayLoad(hoursLost, selectedReason)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = DarkBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("confirm_replan_button")
                ) {
                    Text("Auto-Rebalance Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
