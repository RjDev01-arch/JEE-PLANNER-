package com.example.jee.ai

import com.example.jee.data.JeeRepository
import com.example.jee.data.SubjectType
import com.example.jee.data.TaskPriority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class JeeCoachEngine(private val repository: JeeRepository) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun askCoach(query: String): String = withContext(Dispatchers.IO) {
        val profile = repository.userProfile.value
        val tasks = repository.tasks.value
        val backlog = repository.backlogItems.value
        val tests = repository.tests.value
        val chapters = repository.chapters.value
        val mistakes = repository.mistakes.value

        val upcomingTest = tests.firstOrNull { !it.isCompleted }
        val criticalBacklogCount = backlog.count { it.priorityCategory.name == "CRITICAL" }
        val pendingToday = tasks.count { it.dayOffset == 0 && it.status.name != "COMPLETED" }
        val phyMastery = repository.getSubjectMastery(SubjectType.PHYSICS)
        val chemMastery = repository.getSubjectMastery(SubjectType.CHEMISTRY)
        val mathMastery = repository.getSubjectMastery(SubjectType.MATHEMATICS)

        // Try calling Gemini if API key is valid and not placeholder
        val apiKey = try {
            val key = com.example.BuildConfig.GEMINI_API_KEY
            if (key.isNotBlank() && key != "MY_GEMINI_API_KEY") key else null
        } catch (e: Exception) {
            null
        }

        if (apiKey != null) {
            try {
                val prompt = """
                    You are an elite, no-nonsense JEE Advanced & Main Command Center AI Coach.
                    The student is: ${profile.studentName}, Target Exam: ${profile.targetExam.label} ${profile.targetYear}.
                    Live Student Telemetry:
                    - Physics Mastery: $phyMastery%, Chem: $chemMastery%, Math: $mathMastery%
                    - Critical Backlog items: $criticalBacklogCount
                    - Pending tasks today: $pendingToday
                    - Upcoming test: ${upcomingTest?.name ?: "None"} (in ${upcomingTest?.daysUntil ?: 0} days)
                    - Recurring mistakes: ${mistakes.take(3).joinToString { "${it.subject}: ${it.mistakeType.label}" }}
                    
                    Student Question: "$query"
                    
                    Instructions:
                    1. Give an actionable, high-precision academic recommendation grounded in their real data.
                    2. Explicitly specify chapter, question type (PYQs/DPP), time blocks, and negative mark elimination strategy.
                    3. Keep it crisp, motivating yet realistic (no generic fluff). Max 120 words.
                """.trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
                val jsonBody = JSONObject().apply {
                    val contentsArray = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val partsArray = JSONArray().apply {
                                put(JSONObject().put("text", prompt))
                            }
                            put("parts", partsArray)
                        }
                        put(contentObj)
                    }
                    put("contents", contentsArray)
                }

                val req = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(req).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string()
                    if (!respBody.isNullOrBlank()) {
                        val respJson = JSONObject(respBody)
                        val text = respJson.optJSONArray("candidates")
                            ?.optJSONObject(0)
                            ?.optJSONObject("content")
                            ?.optJSONArray("parts")
                            ?.optJSONObject(0)
                            ?.optString("text")
                        if (!text.isNullOrBlank()) {
                            return@withContext text.trim()
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to local intelligent heuristics
            }
        }

        // Local Expert JEE Heuristic Reasoning Engine
        generateExpertLocalResponse(
            query = query,
            profile = profile,
            tasks = tasks,
            backlog = backlog,
            tests = tests,
            chapters = chapters,
            mistakes = mistakes,
            phyMastery = phyMastery,
            chemMastery = chemMastery,
            mathMastery = mathMastery
        )
    }

    private fun generateExpertLocalResponse(
        query: String,
        profile: com.example.jee.data.UserProfile,
        tasks: List<com.example.jee.data.PlannerTask>,
        backlog: List<com.example.jee.data.BacklogItem>,
        tests: List<com.example.jee.data.TestItem>,
        chapters: List<com.example.jee.data.JeeChapter>,
        mistakes: List<com.example.jee.data.MistakeRecord>,
        phyMastery: Int,
        chemMastery: Int,
        mathMastery: Int
    ): String {
        val qLower = query.lowercase()

        return when {
            qLower.contains("tonight") || qLower.contains("right now") || qLower.contains("what should i study") -> {
                val nextAction = repository.getNextActionRecommendation()
                """
                🎯 Direct Command for Tonight:
                Focus on ${nextAction.subject.displayName} → ${nextAction.chapter} (${nextAction.topic}).
                
                ⏱️ Target Duration: ${nextAction.estimatedMinutes} minutes
                📌 Task Type: ${nextAction.taskType.label}
                💡 Strategy: ${nextAction.rationale}
                
                Preserve your ${profile.sleepTime} sleep cutoff. Do not begin open-ended theory past 10:30 PM. Conclude with 15 minutes of formula sheet revision.
                """.trimIndent()
            }

            qLower.contains("backlog") || qLower.contains("20 days") -> {
                val critical = backlog.filter { it.priorityCategory.name == "CRITICAL" }
                val totalMinutes = critical.sumOf { it.estimatedMinutes }
                val hoursNeeded = totalMinutes / 60.0
                val dailyBacklogTargetMinutes = (totalMinutes / 20.0).toInt().coerceAtLeast(45)

                """
                📊 Backlog Feasibility Audit:
                Yes, completely achievable with disciplined slotting.
                
                • Total Critical Backlog: ${critical.size} modules (~${String.format("%.1f", hoursNeeded)} hours).
                • Daily allocation required: Just $dailyBacklogTargetMinutes minutes/day over the next 20 days.
                • High Priority Order: 
                  1. ${critical.getOrNull(0)?.chapter ?: "Center of Mass"} (Foundation for Rotation)
                  2. ${critical.getOrNull(1)?.chapter ?: "Thermodynamics"} (Guaranteed 8 marks in JEE)
                  
                Rule: Never use core fresh study hours for backlog. Attack backlogs in the secondary evening slot (9:00 PM - 10:00 PM).
                """.trimIndent()
            }

            qLower.contains("5 days") || qLower.contains("test") || qLower.contains("prioritize") -> {
                val test = tests.firstOrNull { !it.isCompleted }
                val testName = test?.name ?: "Upcoming Mock Test"
                """
                ⚡ 5-Day Test Triage Protocol for $testName:
                
                • D-4 & D-3 (Syllabus Lock): Stop learning new concepts. Solve 20-30 timed PYQs for high-weightage chapters (Kinematics, Bonding, Quadratics).
                • D-2 (Formula & Edge Cases): Review your Mistake Log. Re-solve the 4 problems where you lost marks to sign conventions and inverted tangents.
                • D-1 (Speed Temperament): Light 2-hour formula recap. Sleep early by 10:30 PM.
                • D-Day: Target easy scoring questions in Round 1 (first 60 mins). Never spend >3 mins on an unsolved math problem.
                """.trimIndent()
            }

            qLower.contains("falling behind") || qLower.contains("why") || qLower.contains("distracted") -> {
                val missedTasks = tasks.count { it.status.name == "MISSED" || it.status.name == "MOVED" }
                """
                🔍 Diagnostic Analysis:
                You have $missedTasks rescheduled tasks this week. The root causes identified from your telemetry:
                
                1. Overestimating weekday bandwidth: You have ${profile.weekdayAvailableHours}h free, but task blocks totaled more than available energy after coaching.
                2. Low Chemistry speed ($chemMastery% mastery): Inorganic and physical formulas are consuming extra review time.
                
                💡 Remedy:
                Hit [Reduce Today's Load] to push non-critical tasks forward. Focus on 2 high-leverage sessions daily instead of 5 fragmented ones.
                """.trimIndent()
            }

            qLower.contains("replan") -> {
                repository.replanWeek()
                """
                🔄 Intelligent Schedule Rebalancing Complete!
                
                • Re-allocated pending tasks across your available weekday (${profile.weekdayAvailableHours}h) and weekend (${profile.weekendAvailableHours}h) slots.
                • Protected: Sleep window (${profile.sleepTime} - ${profile.wakeUpTime}) and coaching commitments.
                • Prioritized high-scoring prerequisites for your upcoming Sunday mock test.
                
                Check your Planner tab to view the adjusted timetable.
                """.trimIndent()
            }

            else -> {
                """
                👨‍🏫 Coach Insight for ${profile.studentName}:
                
                Your current syllabus standing is Physics: $phyMastery%, Chemistry: $chemMastery%, Math: $mathMastery%.
                
                Key focus right now:
                1. Complete your highest priority daily task in ${nextRecommendedSubject(phyMastery, chemMastery, mathMastery)}.
                2. Plug careless mistakes logged in your Mistake Database before Sunday's test.
                3. Keep your ${profile.currentStreakDays}-day streak alive! Consistency beats intense marathon cramming.
                """.trimIndent()
            }
        }
    }

    private fun nextRecommendedSubject(p: Int, c: Int, m: Int): String {
        return when {
            c <= p && c <= m -> "Chemistry (highest score-to-time ratio in JEE)"
            p <= m -> "Physics (reinforce Mechanics numerical problem speed)"
            else -> "Mathematics (requires continuous daily problem solving)"
        }
    }
}
