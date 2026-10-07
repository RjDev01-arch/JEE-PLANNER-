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
import androidx.compose.material.icons.automirrored.filled.Send
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
import com.example.jee.ai.JeeCoachEngine
import com.example.jee.data.JeeRepository
import com.example.jee.ui.components.CommandCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val sender: String, // "user" or "coach"
    val text: String,
    val timestamp: String = "Just now"
)

@Composable
fun AiCoachScreen(
    repository: JeeRepository
) {
    val coachEngine = remember { JeeCoachEngine(repository) }
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "coach",
                text = "⚡ JEE Command AI Mentor initialized. Telemetry synchronized: 14 chapters, 5 pending tasks, Sunday Part Test #04 in 5 days. Ask me anything about your roadmap, backlog, test preparation, or nightly plan."
            )
        )
    }

    val quickChips = listOf(
        "What should I study tonight?",
        "Can I finish this backlog in 20 days?",
        "My test is in 5 days. What should I prioritize?",
        "Why am I falling behind?",
        "Replan my week."
    )

    fun sendUserQuery(query: String) {
        if (query.isBlank() || isThinking) return
        messages.add(ChatMessage(sender = "user", text = query))
        inputText = ""
        isThinking = true

        coroutineScope.launch {
            val response = coachEngine.askCoach(query)
            messages.add(ChatMessage(sender = "coach", text = response))
            isThinking = false
        }
    }

    Scaffold(
        containerColor = DarkBg,
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 72.dp)
                    .background(DarkSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // QUICK CHIPS ROW
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(quickChips) { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurfaceHighlight)
                                .border(1.dp, CyanNeon.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .clickable { sendUserQuery(chip) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(text = chip, fontSize = 11.sp, color = CyanNeon, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // INPUT ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask your JEE strategy coach...", fontSize = 13.sp, color = TextMuted) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("coach_input_field"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanNeon,
                            unfocusedBorderColor = DarkBorder,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        maxLines = 2
                    )

                    IconButton(
                        onClick = { sendUserQuery(inputText) },
                        enabled = inputText.isNotBlank() && !isThinking,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (inputText.isNotBlank() && !isThinking) CyanNeon else DarkSurfaceHighlight)
                            .testTag("coach_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isThinking) DarkBg else TextMuted
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("ai_coach_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SmartToy, contentDescription = null, tint = CyanNeon, modifier = Modifier.size(20.dp))
                        Text(
                            text = "AI JEE STUDY COACH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "Real-Time Telemetry Grounded Mentor",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            items(messages) { msg ->
                val isCoach = msg.sender == "coach"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isCoach) Arrangement.Start else Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .widthIn(max = 320.dp)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isCoach) 2.dp else 14.dp,
                                    bottomEnd = if (isCoach) 14.dp else 2.dp
                                )
                            )
                            .background(if (isCoach) DarkSurfaceElevated else CyanNeon.copy(alpha = 0.2f))
                            .border(
                                1.dp,
                                if (isCoach) IndigoGlow.copy(alpha = 0.4f) else CyanNeon.copy(alpha = 0.6f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = if (isCoach) "AI Strategy Coach" else "You",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCoach) CyanNeon else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = CyanNeon,
                            strokeWidth = 2.dp
                        )
                        Text("Analyzing student telemetry & formulating strategy...", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}
