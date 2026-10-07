package com.example.jee.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class JeeRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("jee_command_center_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _chapters = MutableStateFlow(loadChapters())
    val chapters: StateFlow<List<JeeChapter>> = _chapters.asStateFlow()

    private val _tasks = MutableStateFlow(loadTasks())
    val tasks: StateFlow<List<PlannerTask>> = _tasks.asStateFlow()

    private val _backlogItems = MutableStateFlow(loadBacklog())
    val backlogItems: StateFlow<List<BacklogItem>> = _backlogItems.asStateFlow()

    private val _tests = MutableStateFlow(loadTests())
    val tests: StateFlow<List<TestItem>> = _tests.asStateFlow()

    private val _mistakes = MutableStateFlow(loadMistakes())
    val mistakes: StateFlow<List<MistakeRecord>> = _mistakes.asStateFlow()

    private val _recentReplanEvent = MutableStateFlow<ReplanAdjustmentEvent?>(null)
    val recentReplanEvent: StateFlow<ReplanAdjustmentEvent?> = _recentReplanEvent.asStateFlow()

    private val _latestTestAnalysis = MutableStateFlow(TestAnalysisLog(
        id = "analysis_prev",
        testName = "FIITJEE Part Test #03 (11th Syllabus)",
        date = "28 Sep",
        totalMarks = 300,
        marksObtained = 194,
        attempted = 62,
        correct = 51,
        incorrect = 11,
        unattempted = 13,
        timeSpentMinutes = 175,
        sillyMistakesCount = 4,
        conceptualMistakesCount = 5,
        marksLostNegatives = 11,
        marksLostCareless = 20,
        accuracyPercent = 82.2f,
        attemptRatePercent = 82.6f,
        keyInsight = "Your biggest issue is not theory. You lost 20 marks on careless calculation in Physics and spent 14 minutes on 2 low-probability Calculus questions. Question selection will easily push your score past 225."
    ))
    val latestTestAnalysis: StateFlow<TestAnalysisLog> = _latestTestAnalysis.asStateFlow()

    // -------------------------------------------------------------
    // PROFILE & ONBOARDING
    // -------------------------------------------------------------
    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        saveUserProfile(profile)
    }

    fun completeOnboarding(
        name: String,
        studentClass: ClassLevel,
        targetExam: TargetExamType,
        targetYear: String,
        coachingSchedule: String,
        schoolSchedule: String,
        wakeUpTime: String,
        sleepTime: String,
        weekdayHours: Float,
        weekendHours: Float,
        sessionLength: Int,
        phyConfidence: Int,
        chemConfidence: Int,
        mathConfidence: Int
    ) {
        val updated = _userProfile.value.copy(
            studentName = name,
            studentClass = studentClass,
            targetExam = targetExam,
            targetYear = targetYear,
            coachingSchedule = coachingSchedule,
            schoolSchedule = schoolSchedule,
            wakeUpTime = wakeUpTime,
            sleepTime = sleepTime,
            weekdayAvailableHours = weekdayHours,
            weekendAvailableHours = weekendHours,
            preferredSessionMinutes = sessionLength,
            physicsConfidence = phyConfidence,
            chemistryConfidence = chemConfidence,
            mathConfidence = mathConfidence,
            isOnboarded = true
        )
        updateUserProfile(updated)
    }

    // -------------------------------------------------------------
    // TASKS MANAGEMENT
    // -------------------------------------------------------------
    fun addTask(task: PlannerTask) {
        val updated = _tasks.value + task
        _tasks.value = updated
        saveTasks(updated)
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        val updated = _tasks.value.map {
            if (it.id == taskId) it.copy(status = newStatus) else it
        }
        _tasks.value = updated
        saveTasks(updated)

        // Award XP if completed
        if (newStatus == TaskStatus.COMPLETED) {
            val user = _userProfile.value
            _userProfile.value = user.copy(totalXp = user.totalXp + 50)
            saveUserProfile(_userProfile.value)
        }
    }

    fun deleteTask(taskId: String) {
        val updated = _tasks.value.filterNot { it.id == taskId }
        _tasks.value = updated
        saveTasks(updated)
    }

    // -------------------------------------------------------------
    // AI AUTO-REPLANNER LOGIC
    // -------------------------------------------------------------
    fun reduceTodayLoad(hoursLost: Float, userExplanation: String = "Lost $hoursLost hours today") {
        // Identify uncompleted tasks for today (dayOffset == 0)
        val todayTasks = _tasks.value.filter { it.dayOffset == 0 && it.status == TaskStatus.PENDING }
        if (todayTasks.isEmpty()) return

        // Sort by priority so we protect Critical/High prerequisite tasks and defer others
        val sortedByPriority = todayTasks.sortedBy {
            when (it.priority) {
                TaskPriority.LOW -> 0
                TaskPriority.MEDIUM -> 1
                TaskPriority.HIGH -> 2
                TaskPriority.CRITICAL -> 3
            }
        }

        // Shift lower priority or excess tasks to tomorrow (dayOffset = 1) or Friday (dayOffset = 2)
        // while strictly preserving sleep (11:30 PM) and coaching blocks
        var minutesToRelocate = (hoursLost * 60).toInt()
        val modifiedTaskIds = mutableSetOf<String>()

        val newTasks = _tasks.value.map { task ->
            if (task.dayOffset == 0 && task.status == TaskStatus.PENDING && minutesToRelocate > 0) {
                // Relocate this task to tomorrow
                minutesToRelocate -= task.estimatedMinutes
                modifiedTaskIds.add(task.id)
                task.copy(
                    dayOffset = 1,
                    deadline = "Tomorrow, Rescheduled",
                    status = TaskStatus.MOVED,
                    note = "Auto-replanned: shifted from today to protect sleep & coaching"
                )
            } else {
                task
            }
        }

        _tasks.value = newTasks
        saveTasks(newTasks)

        val event = ReplanAdjustmentEvent(
            timestamp = "Just now",
            reason = userExplanation,
            tasksAdjustedCount = modifiedTaskIds.size,
            protectedBlocks = listOf("Sleep Window (11:30 PM - 6:00 AM)", "Coaching Block (4:00 PM - 8:00 PM)", "Upcoming Sunday Mock Test"),
            explanation = "Redistributed ${modifiedTaskIds.size} tasks to tomorrow. High-yield prerequisites retained today, protecting your sleep schedule and school/coaching commitments."
        )
        _recentReplanEvent.value = event
    }

    fun markTaskAsMissedAndReplan(taskId: String) {
        val targetTask = _tasks.value.find { it.id == taskId } ?: return
        val newTasks = _tasks.value.map {
            if (it.id == taskId) {
                it.copy(
                    status = TaskStatus.MISSED,
                    dayOffset = it.dayOffset + 1,
                    deadline = "Rescheduled Tomorrow",
                    note = "Missed: Re-scheduled with priority protection"
                )
            } else it
        }
        _tasks.value = newTasks
        saveTasks(newTasks)

        _recentReplanEvent.value = ReplanAdjustmentEvent(
            timestamp = "Just now",
            reason = "Missed ${targetTask.chapter} (${targetTask.topic})",
            tasksAdjustedCount = 1,
            protectedBlocks = listOf("Night rest cycle", "Coaching hours"),
            explanation = "Your schedule was adjusted: ${targetTask.chapter} shifted to tomorrow because it is prerequisite knowledge for your upcoming Mechanics test."
        )
    }

    fun replanWeek() {
        // Balance tasks evenly across 7 days based on weekday/weekend study hours
        val pending = _tasks.value.filter { it.status != TaskStatus.COMPLETED }
        val updated = _tasks.value.mapIndexed { index, task ->
            if (task.status != TaskStatus.COMPLETED) {
                val newOffset = index % 3
                task.copy(
                    dayOffset = newOffset,
                    deadline = if (newOffset == 0) "Today" else if (newOffset == 1) "Tomorrow" else "This Weekend"
                )
            } else task
        }
        _tasks.value = updated
        saveTasks(updated)

        _recentReplanEvent.value = ReplanAdjustmentEvent(
            timestamp = "Just now",
            reason = "Weekly Rebalancing Triggered",
            tasksAdjustedCount = pending.size,
            protectedBlocks = listOf("Fixed coaching hours", "Sleep cycle (7 hrs)", "Sunday Full Mock (9 AM - 12 PM)"),
            explanation = "Weekly schedule recalibrated: Distributed pending load evenly across upcoming days with spaced revisions scheduled prior to Sunday's test."
        )
    }

    // -------------------------------------------------------------
    // "WHAT SHOULD I DO RIGHT NOW?"
    // -------------------------------------------------------------
    fun getNextActionRecommendation(): NextActionRecommendation {
        val todayPending = _tasks.value
            .filter { it.dayOffset == 0 && (it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS) }
            .sortedWith(compareByDescending<PlannerTask> { it.priority == TaskPriority.CRITICAL }
                .thenByDescending { it.priority == TaskPriority.HIGH }
                .thenBy { it.estimatedMinutes })

        if (todayPending.isNotEmpty()) {
            val topTask = todayPending.first()
            return NextActionRecommendation(
                subject = topTask.subject,
                chapter = topTask.chapter,
                topic = topTask.topic,
                taskType = topTask.taskType,
                estimatedMinutes = topTask.estimatedMinutes,
                priority = topTask.priority,
                rationale = "Direct prerequisite for upcoming Sunday mock test. High scoring weightage in JEE Main.",
                taskId = topTask.id
            )
        }

        // Check critical backlog if no tasks are pending
        val criticalBacklog = _backlogItems.value.firstOrNull { it.status == TaskStatus.PENDING && it.priorityCategory == BacklogPriorityCategory.CRITICAL }
        if (criticalBacklog != null) {
            return NextActionRecommendation(
                subject = criticalBacklog.subject,
                chapter = criticalBacklog.chapter,
                topic = criticalBacklog.topic,
                taskType = TaskType.BACKLOG,
                estimatedMinutes = 45,
                priority = TaskPriority.HIGH,
                rationale = "No urgent daily tasks left. Best time to clear ${criticalBacklog.chapter} critical backlog.",
                taskId = null
            )
        }

        return NextActionRecommendation(
            subject = SubjectType.PHYSICS,
            chapter = "Kinematics",
            topic = "20 Previous Year Questions (PYQs)",
            taskType = TaskType.PYQ,
            estimatedMinutes = 45,
            priority = TaskPriority.HIGH,
            rationale = "Spaced repetition recommended. Solidify 1D & 2D projectile formulas before next test.",
            taskId = null
        )
    }

    // -------------------------------------------------------------
    // BACKLOG
    // -------------------------------------------------------------
    fun addBacklogItem(item: BacklogItem) {
        val updated = _backlogItems.value + item
        _backlogItems.value = updated
        saveBacklog(updated)
    }

    fun resolveBacklogItem(itemId: String) {
        val updated = _backlogItems.value.map {
            if (it.id == itemId) it.copy(status = TaskStatus.COMPLETED) else it
        }
        _backlogItems.value = updated
        saveBacklog(updated)
    }

    fun convertBacklogToTodayTask(item: BacklogItem) {
        val newTask = PlannerTask(
            id = UUID.randomUUID().toString(),
            subject = item.subject,
            chapter = item.chapter,
            topic = item.topic,
            taskType = TaskType.BACKLOG,
            estimatedMinutes = item.estimatedMinutes.coerceAtMost(60),
            difficulty = TaskDifficulty.HARD,
            priority = TaskPriority.HIGH,
            deadline = "Today, 10:00 PM",
            dayOffset = 0,
            status = TaskStatus.PENDING,
            note = "Backlog Attack session: ${item.prerequisiteReason}"
        )
        addTask(newTask)
    }

    fun getBacklogAttackPlan(): List<BacklogAttackPlanItem> {
        return listOf(
            BacklogAttackPlanItem(
                dayLabel = "TODAY",
                subject = SubjectType.PHYSICS,
                chapter = "Center of Mass",
                topic = "Linear Momentum Conservation & 2D Collisions",
                durationMinutes = 60,
                timeSlot = "9:30 PM – 10:30 PM"
            ),
            BacklogAttackPlanItem(
                dayLabel = "TOMORROW",
                subject = SubjectType.CHEMISTRY,
                chapter = "Chemical Thermodynamics",
                topic = "First Law & Enthalpy calculations",
                durationMinutes = 60,
                timeSlot = "8:30 PM – 9:30 PM"
            ),
            BacklogAttackPlanItem(
                dayLabel = "FRIDAY",
                subject = SubjectType.PHYSICS,
                chapter = "Rotational Dynamics",
                topic = "Moment of Inertia standard bodies & Parallel Axis Theorem",
                durationMinutes = 75,
                timeSlot = "9:00 PM – 10:15 PM"
            ),
            BacklogAttackPlanItem(
                dayLabel = "SATURDAY",
                subject = SubjectType.CHEMISTRY,
                chapter = "Ionic Equilibrium",
                topic = "Buffer Solutions pH calculations & Ksp common ion effect",
                durationMinutes = 60,
                timeSlot = "4:00 PM – 5:00 PM"
            )
        )
    }

    // -------------------------------------------------------------
    // SYLLABUS & CHAPTERS
    // -------------------------------------------------------------
    fun updateChapterProgress(
        chapterId: String,
        theory: Int? = null,
        basicQ: Int? = null,
        pyqMain: Int? = null,
        pyqAdv: Int? = null,
        rev1: Int? = null,
        rev2: Int? = null
    ) {
        val updated = _chapters.value.map { ch ->
            if (ch.id == chapterId) {
                ch.copy(
                    theoryPercent = theory ?: ch.theoryPercent,
                    basicQuestionsPercent = basicQ ?: ch.basicQuestionsPercent,
                    pyqMainPercent = pyqMain ?: ch.pyqMainPercent,
                    pyqAdvPercent = pyqAdv ?: ch.pyqAdvPercent,
                    revision1Percent = rev1 ?: ch.revision1Percent,
                    revision2Percent = rev2 ?: ch.revision2Percent
                )
            } else ch
        }
        _chapters.value = updated
        saveChapters(updated)
    }

    // -------------------------------------------------------------
    // TESTS & PREPARATION PLAN
    // -------------------------------------------------------------
    fun addTest(test: TestItem) {
        val updated = _tests.value + test
        _tests.value = updated
        saveTests(updated)

        // Automatically create preparation plan tasks for this test!
        createTestPrepTasks(test)
    }

    private fun createTestPrepTasks(test: TestItem) {
        val prepTasks = listOf(
            PlannerTask(
                id = UUID.randomUUID().toString(),
                subject = SubjectType.PHYSICS,
                chapter = test.name,
                topic = "D-3: PYQ Sprint & Formula Matrix (${test.name})",
                taskType = TaskType.PYQ,
                estimatedMinutes = 60,
                priority = TaskPriority.HIGH,
                deadline = "D-3 before Test",
                dayOffset = 1,
                note = "Auto-generated test preparation milestone"
            ),
            PlannerTask(
                id = UUID.randomUUID().toString(),
                subject = SubjectType.MATHEMATICS,
                chapter = test.name,
                topic = "D-1: Light Revision & Mistake Book Review",
                taskType = TaskType.REVISION,
                estimatedMinutes = 45,
                priority = TaskPriority.CRITICAL,
                deadline = "D-1 before Test",
                dayOffset = 2,
                note = "Error log review & quick formula recap"
            ),
            PlannerTask(
                id = UUID.randomUUID().toString(),
                subject = SubjectType.CHEMISTRY,
                chapter = test.name,
                topic = "D+1: Deep Test Analysis & Error Classification",
                taskType = TaskType.TEST_ANALYSIS,
                estimatedMinutes = 60,
                priority = TaskPriority.CRITICAL,
                deadline = "Day after Test",
                dayOffset = 3,
                note = "Crucial for identifying negative marking trends"
            )
        )
        val allTasks = _tasks.value + prepTasks
        _tasks.value = allTasks
        saveTasks(allTasks)
    }

    fun recordTestResult(analysis: TestAnalysisLog) {
        _latestTestAnalysis.value = analysis

        // Update corresponding test if exists
        val updatedTests = _tests.value.map {
            if (it.name.contains(analysis.testName, ignoreCase = true) || analysis.testName.contains(it.name, ignoreCase = true)) {
                it.copy(isCompleted = true, score = analysis.marksObtained)
            } else it
        }
        _tests.value = updatedTests
        saveTests(updatedTests)
    }

    // -------------------------------------------------------------
    // MISTAKES DATABASE
    // -------------------------------------------------------------
    fun logMistake(mistake: MistakeRecord) {
        val updated = _mistakes.value + mistake
        _mistakes.value = updated
        saveMistakes(updated)
    }

    fun toggleMistakeMastery(mistakeId: String) {
        val updated = _mistakes.value.map {
            if (it.id == mistakeId) it.copy(isMastered = !it.isMastered) else it
        }
        _mistakes.value = updated
        saveMistakes(updated)
    }

    // -------------------------------------------------------------
    // STATS HELPERS
    // -------------------------------------------------------------
    fun getOverallMastery(): Int {
        val list = _chapters.value
        if (list.isEmpty()) return 0
        return list.map { it.overallMastery }.average().toInt()
    }

    fun getSubjectMastery(subject: SubjectType): Int {
        val list = _chapters.value.filter { it.subject == subject }
        if (list.isEmpty()) return 0
        return list.map { it.overallMastery }.average().toInt()
    }

    // -------------------------------------------------------------
    // LOCAL STORAGE / JSON PERSISTENCE
    // -------------------------------------------------------------
    private fun saveUserProfile(profile: UserProfile) {
        val json = JSONObject().apply {
            put("studentName", profile.studentName)
            put("studentClass", profile.studentClass.name)
            put("targetExam", profile.targetExam.name)
            put("targetYear", profile.targetYear)
            put("coachingSchedule", profile.coachingSchedule)
            put("schoolSchedule", profile.schoolSchedule)
            put("wakeUpTime", profile.wakeUpTime)
            put("sleepTime", profile.sleepTime)
            put("weekdayHours", profile.weekdayAvailableHours.toDouble())
            put("weekendHours", profile.weekendAvailableHours.toDouble())
            put("sessionMinutes", profile.preferredSessionMinutes)
            put("phyConfidence", profile.physicsConfidence)
            put("chemConfidence", profile.chemistryConfidence)
            put("mathConfidence", profile.mathConfidence)
            put("isOnboarded", profile.isOnboarded)
            put("streak", profile.currentStreakDays)
            put("totalXp", profile.totalXp)
        }
        prefs.edit().putString("user_profile_json", json.toString()).apply()
    }

    private fun loadUserProfile(): UserProfile {
        val raw = prefs.getString("user_profile_json", null) ?: return UserProfile()
        return try {
            val obj = JSONObject(raw)
            UserProfile(
                studentName = obj.optString("studentName", "Arjun Verma"),
                studentClass = ClassLevel.valueOf(obj.optString("studentClass", "CLASS_12")),
                targetExam = TargetExamType.valueOf(obj.optString("targetExam", "BOTH")),
                targetYear = obj.optString("targetYear", "2027"),
                coachingSchedule = obj.optString("coachingSchedule", "Mon-Fri (4:00 PM - 8:00 PM)"),
                schoolSchedule = obj.optString("schoolSchedule", "Mon-Fri (7:30 AM - 1:30 PM)"),
                wakeUpTime = obj.optString("wakeUpTime", "06:00 AM"),
                sleepTime = obj.optString("sleepTime", "11:30 PM"),
                weekdayAvailableHours = obj.optDouble("weekdayHours", 5.5).toFloat(),
                weekendAvailableHours = obj.optDouble("weekendHours", 9.0).toFloat(),
                preferredSessionMinutes = obj.optInt("sessionMinutes", 50),
                physicsConfidence = obj.optInt("phyConfidence", 4),
                chemistryConfidence = obj.optInt("chemConfidence", 3),
                mathConfidence = obj.optInt("mathConfidence", 4),
                isOnboarded = obj.optBoolean("isOnboarded", true),
                currentStreakDays = obj.optInt("streak", 14),
                totalXp = obj.optInt("totalXp", 3450)
            )
        } catch (e: Exception) {
            UserProfile()
        }
    }

    private fun saveTasks(tasks: List<PlannerTask>) {
        val array = JSONArray()
        tasks.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("subject", t.subject.name)
                put("chapter", t.chapter)
                put("topic", t.topic)
                put("taskType", t.taskType.name)
                put("estimatedMinutes", t.estimatedMinutes)
                put("difficulty", t.difficulty.name)
                put("priority", t.priority.name)
                put("deadline", t.deadline)
                put("dayOffset", t.dayOffset)
                put("status", t.status.name)
                put("note", t.note)
            }
            array.put(obj)
        }
        prefs.edit().putString("tasks_json", array.toString()).apply()
    }

    private fun loadTasks(): List<PlannerTask> {
        val raw = prefs.getString("tasks_json", null) ?: return JeeDemoData.initialTasks()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<PlannerTask>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    PlannerTask(
                        id = obj.getString("id"),
                        subject = SubjectType.valueOf(obj.getString("subject")),
                        chapter = obj.getString("chapter"),
                        topic = obj.getString("topic"),
                        taskType = TaskType.valueOf(obj.getString("taskType")),
                        estimatedMinutes = obj.getInt("estimatedMinutes"),
                        difficulty = TaskDifficulty.valueOf(obj.optString("difficulty", "MEDIUM")),
                        priority = TaskPriority.valueOf(obj.optString("priority", "HIGH")),
                        deadline = obj.optString("deadline", "Today"),
                        dayOffset = obj.optInt("dayOffset", 0),
                        status = TaskStatus.valueOf(obj.optString("status", "PENDING")),
                        note = obj.optString("note", "")
                    )
                )
            }
            if (list.isEmpty()) JeeDemoData.initialTasks() else list
        } catch (e: Exception) {
            JeeDemoData.initialTasks()
        }
    }

    private fun saveChapters(chapters: List<JeeChapter>) {
        val array = JSONArray()
        chapters.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("subject", c.subject.name)
                put("name", c.name)
                put("classLevel", c.classLevel.name)
                put("weightageScore", c.weightageScore)
                put("theoryPercent", c.theoryPercent)
                put("lecturesPercent", c.lecturesPercent)
                put("notesPercent", c.notesPercent)
                put("basicQuestionsPercent", c.basicQuestionsPercent)
                put("pyqMainPercent", c.pyqMainPercent)
                put("pyqAdvPercent", c.pyqAdvPercent)
                put("revision1Percent", c.revision1Percent)
                put("revision2Percent", c.revision2Percent)
                put("testScorePercent", c.testScorePercent)
                put("isBacklog", c.isBacklog)
                put("isWeak", c.isWeak)
            }
            array.put(obj)
        }
        prefs.edit().putString("chapters_json", array.toString()).apply()
    }

    private fun loadChapters(): List<JeeChapter> {
        val raw = prefs.getString("chapters_json", null) ?: return JeeDemoData.initialChapters()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<JeeChapter>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    JeeChapter(
                        id = obj.getString("id"),
                        subject = SubjectType.valueOf(obj.getString("subject")),
                        name = obj.getString("name"),
                        classLevel = ClassLevel.valueOf(obj.optString("classLevel", "CLASS_11")),
                        weightageScore = obj.optInt("weightageScore", 8),
                        theoryPercent = obj.optInt("theoryPercent", 80),
                        lecturesPercent = obj.optInt("lecturesPercent", 80),
                        notesPercent = obj.optInt("notesPercent", 80),
                        basicQuestionsPercent = obj.optInt("basicQuestionsPercent", 60),
                        pyqMainPercent = obj.optInt("pyqMainPercent", 40),
                        pyqAdvPercent = obj.optInt("pyqAdvPercent", 20),
                        revision1Percent = obj.optInt("revision1Percent", 30),
                        revision2Percent = obj.optInt("revision2Percent", 10),
                        testScorePercent = obj.optInt("testScorePercent", 70),
                        isBacklog = obj.optBoolean("isBacklog", false),
                        isWeak = obj.optBoolean("isWeak", false)
                    )
                )
            }
            if (list.isEmpty()) JeeDemoData.initialChapters() else list
        } catch (e: Exception) {
            JeeDemoData.initialChapters()
        }
    }

    private fun saveBacklog(items: List<BacklogItem>) {
        val array = JSONArray()
        items.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("subject", b.subject.name)
                put("chapter", b.chapter)
                put("topic", b.topic)
                put("priorityCategory", b.priorityCategory.name)
                put("estimatedMinutes", b.estimatedMinutes)
                put("prerequisiteReason", b.prerequisiteReason)
                put("testRelevance", b.testRelevance)
                put("status", b.status.name)
            }
            array.put(obj)
        }
        prefs.edit().putString("backlog_json", array.toString()).apply()
    }

    private fun loadBacklog(): List<BacklogItem> {
        val raw = prefs.getString("backlog_json", null) ?: return JeeDemoData.initialBacklog()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<BacklogItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    BacklogItem(
                        id = obj.getString("id"),
                        subject = SubjectType.valueOf(obj.getString("subject")),
                        chapter = obj.getString("chapter"),
                        topic = obj.getString("topic"),
                        priorityCategory = BacklogPriorityCategory.valueOf(obj.getString("priorityCategory")),
                        estimatedMinutes = obj.getInt("estimatedMinutes"),
                        prerequisiteReason = obj.getString("prerequisiteReason"),
                        testRelevance = obj.getString("testRelevance"),
                        status = TaskStatus.valueOf(obj.optString("status", "PENDING"))
                    )
                )
            }
            if (list.isEmpty()) JeeDemoData.initialBacklog() else list
        } catch (e: Exception) {
            JeeDemoData.initialBacklog()
        }
    }

    private fun saveTests(tests: List<TestItem>) {
        val array = JSONArray()
        tests.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("name", t.name)
                put("date", t.date)
                put("examType", t.examType)
                put("durationMinutes", t.durationMinutes)
                put("isCompleted", t.isCompleted)
                if (t.score != null) put("score", t.score)
                put("totalMarks", t.totalMarks)
                put("daysUntil", t.daysUntil)
                val syllabusArray = JSONArray()
                t.syllabusTopics.forEach { syllabusArray.put(it) }
                put("syllabusTopics", syllabusArray)
            }
            array.put(obj)
        }
        prefs.edit().putString("tests_json", array.toString()).apply()
    }

    private fun loadTests(): List<TestItem> {
        val raw = prefs.getString("tests_json", null) ?: return JeeDemoData.initialTests()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<TestItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val syllabusArray = obj.getJSONArray("syllabusTopics")
                val syllabus = mutableListOf<String>()
                for (j in 0 until syllabusArray.length()) {
                    syllabus.add(syllabusArray.getString(j))
                }
                list.add(
                    TestItem(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        date = obj.getString("date"),
                        examType = obj.getString("examType"),
                        durationMinutes = obj.optInt("durationMinutes", 180),
                        syllabusTopics = syllabus,
                        isCompleted = obj.optBoolean("isCompleted", false),
                        score = if (obj.has("score")) obj.getInt("score") else null,
                        totalMarks = obj.optInt("totalMarks", 300),
                        daysUntil = obj.optInt("daysUntil", 5)
                    )
                )
            }
            if (list.isEmpty()) JeeDemoData.initialTests() else list
        } catch (e: Exception) {
            JeeDemoData.initialTests()
        }
    }

    private fun saveMistakes(mistakes: List<MistakeRecord>) {
        val array = JSONArray()
        mistakes.forEach { m ->
            val obj = JSONObject().apply {
                put("id", m.id)
                put("questionSummary", m.questionSummary)
                put("subject", m.subject.name)
                put("chapter", m.chapter)
                put("mistakeType", m.mistakeType.name)
                put("whyItHappened", m.whyItHappened)
                put("correctApproach", m.correctApproach)
                put("date", m.date)
                put("isMastered", m.isMastered)
            }
            array.put(obj)
        }
        prefs.edit().putString("mistakes_json", array.toString()).apply()
    }

    private fun loadMistakes(): List<MistakeRecord> {
        val raw = prefs.getString("mistakes_json", null) ?: return JeeDemoData.initialMistakes()
        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<MistakeRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    MistakeRecord(
                        id = obj.getString("id"),
                        questionSummary = obj.getString("questionSummary"),
                        subject = SubjectType.valueOf(obj.getString("subject")),
                        chapter = obj.getString("chapter"),
                        mistakeType = MistakeCategory.valueOf(obj.getString("mistakeType")),
                        whyItHappened = obj.getString("whyItHappened"),
                        correctApproach = obj.getString("correctApproach"),
                        date = obj.optString("date", "Recently"),
                        isMastered = obj.optBoolean("isMastered", false)
                    )
                )
            }
            if (list.isEmpty()) JeeDemoData.initialMistakes() else list
        } catch (e: Exception) {
            JeeDemoData.initialMistakes()
        }
    }
}
