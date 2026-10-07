package com.example.jee.data

enum class SubjectType(val displayName: String, val code: String) {
    PHYSICS("Physics", "PHY"),
    CHEMISTRY("Chemistry", "CHEM"),
    MATHEMATICS("Mathematics", "MATH")
}

enum class ClassLevel(val label: String) {
    CLASS_11("Class 11"),
    CLASS_12("Class 12"),
    DROPPER("Dropper")
}

enum class TargetExamType(val label: String) {
    JEE_MAIN("JEE Main"),
    JEE_ADVANCED("JEE Advanced"),
    BOTH("Both (Main + Adv)")
}

enum class TaskType(val label: String) {
    THEORY("Theory"),
    LECTURE("Lecture"),
    DPP("DPP"),
    PRACTICE("Practice"),
    PYQ("PYQ"),
    REVISION("Revision"),
    MOCK_TEST("Mock Test"),
    TEST_ANALYSIS("Test Analysis"),
    BACKLOG("Backlog"),
    ERROR_CORRECTION("Error Correction")
}

enum class TaskDifficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    ADVANCED("Advanced")
}

enum class TaskPriority(val label: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    MISSED,
    MOVED
}

enum class BacklogPriorityCategory(val label: String) {
    CRITICAL("Critical"),
    IMPORTANT("Important"),
    OPTIONAL("Optional")
}

enum class MistakeCategory(val label: String) {
    CONCEPTUAL("Conceptual"),
    CALCULATION("Calculation"),
    FORMULA("Formula"),
    MISREAD_QUESTION("Misread Question"),
    WRONG_APPROACH("Wrong Approach"),
    TIME_MANAGEMENT("Time Management"),
    SILLY_MISTAKE("Silly Mistake"),
    GUESSING("Guessing")
}

data class UserProfile(
    val studentName: String = "Arjun Verma",
    val studentClass: ClassLevel = ClassLevel.CLASS_12,
    val targetExam: TargetExamType = TargetExamType.BOTH,
    val targetYear: String = "2027",
    val targetDate: String = "April 2027",
    val daysRemaining: Int = 184,
    val coachingSchedule: String = "Mon-Fri (4:00 PM - 8:00 PM)",
    val schoolSchedule: String = "Mon-Fri (7:30 AM - 1:30 PM)",
    val wakeUpTime: String = "06:00 AM",
    val sleepTime: String = "11:30 PM",
    val weekdayAvailableHours: Float = 5.5f,
    val weekendAvailableHours: Float = 9.0f,
    val preferredSessionMinutes: Int = 50,
    val physicsConfidence: Int = 4, // 1 to 5
    val chemistryConfidence: Int = 3,
    val mathConfidence: Int = 4,
    val isOnboarded: Boolean = true,
    val currentStreakDays: Int = 14,
    val totalXp: Int = 3450,
    val rankTier: String = "AIR Top 500 Pace"
)

data class JeeChapter(
    val id: String,
    val subject: SubjectType,
    val name: String,
    val classLevel: ClassLevel = ClassLevel.CLASS_11,
    val weightageScore: Int = 8, // out of 10
    val theoryPercent: Int = 80,
    val lecturesPercent: Int = 85,
    val notesPercent: Int = 90,
    val basicQuestionsPercent: Int = 60,
    val pyqMainPercent: Int = 45,
    val pyqAdvPercent: Int = 25,
    val revision1Percent: Int = 40,
    val revision2Percent: Int = 15,
    val testScorePercent: Int = 72,
    val isBacklog: Boolean = false,
    val isWeak: Boolean = false
) {
    val overallMastery: Int
        get() = (
            theoryPercent * 0.15f +
            basicQuestionsPercent * 0.20f +
            pyqMainPercent * 0.25f +
            pyqAdvPercent * 0.15f +
            revision1Percent * 0.15f +
            testScorePercent * 0.10f
        ).toInt().coerceIn(0, 100)
}

data class PlannerTask(
    val id: String,
    val subject: SubjectType,
    val chapter: String,
    val topic: String,
    val taskType: TaskType,
    val estimatedMinutes: Int,
    val difficulty: TaskDifficulty = TaskDifficulty.MEDIUM,
    val priority: TaskPriority = TaskPriority.HIGH,
    val deadline: String = "Today, 9:00 PM",
    val dayOffset: Int = 0, // 0 = Today, 1 = Tomorrow, 2 = Day After, etc.
    val status: TaskStatus = TaskStatus.PENDING,
    val note: String = ""
)

data class BacklogItem(
    val id: String,
    val subject: SubjectType,
    val chapter: String,
    val topic: String,
    val priorityCategory: BacklogPriorityCategory,
    val estimatedMinutes: Int,
    val prerequisiteReason: String,
    val testRelevance: String,
    val status: TaskStatus = TaskStatus.PENDING
)

data class BacklogAttackPlanItem(
    val dayLabel: String,
    val subject: SubjectType,
    val chapter: String,
    val topic: String,
    val durationMinutes: Int,
    val timeSlot: String
)

data class TestItem(
    val id: String,
    val name: String,
    val date: String,
    val examType: String, // "JEE Main Full Mock", "Part Test", "Advanced Paper 1"
    val durationMinutes: Int = 180,
    val syllabusTopics: List<String>,
    val isCompleted: Boolean = false,
    val score: Int? = null,
    val totalMarks: Int = 300,
    val daysUntil: Int = 5
)

data class TestAnalysisLog(
    val id: String,
    val testName: String,
    val date: String,
    val totalMarks: Int = 300,
    val marksObtained: Int = 194,
    val attempted: Int = 62,
    val correct: Int = 51,
    val incorrect: Int = 11,
    val unattempted: Int = 13,
    val timeSpentMinutes: Int = 175,
    val sillyMistakesCount: Int = 4,
    val conceptualMistakesCount: Int = 5,
    val marksLostNegatives: Int = 11,
    val marksLostCareless: Int = 20,
    val accuracyPercent: Float = 82.2f,
    val attemptRatePercent: Float = 82.6f,
    val keyInsight: String = "Your biggest issue is not theory. You lost 20 marks on careless calculation in Physics and spent 14 minutes on 2 low-probability Calculus questions."
)

data class MistakeRecord(
    val id: String,
    val questionSummary: String,
    val subject: SubjectType,
    val chapter: String,
    val mistakeType: MistakeCategory,
    val whyItHappened: String,
    val correctApproach: String,
    val date: String,
    val isMastered: Boolean = false
)

data class RoadmapPhase(
    val id: String,
    val title: String,
    val timeline: String,
    val status: String, // "COMPLETED", "IN_PROGRESS", "UPCOMING"
    val progressPercent: Int,
    val focusChapters: List<String>,
    val milestoneGoal: String
)

data class NextActionRecommendation(
    val subject: SubjectType,
    val chapter: String,
    val topic: String,
    val taskType: TaskType,
    val estimatedMinutes: Int,
    val priority: TaskPriority,
    val rationale: String,
    val taskId: String? = null
)

data class ReplanAdjustmentEvent(
    val timestamp: String,
    val reason: String,
    val tasksAdjustedCount: Int,
    val protectedBlocks: List<String>,
    val explanation: String
)
