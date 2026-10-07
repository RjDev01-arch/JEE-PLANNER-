package com.example

import com.example.jee.data.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testChapterMasteryCalculation() {
    val chapter = JeeChapter(
      id = "test_kinematics",
      subject = SubjectType.PHYSICS,
      name = "Kinematics",
      theoryPercent = 80,
      basicQuestionsPercent = 55,
      pyqMainPercent = 30,
      pyqAdvPercent = 0,
      revision1Percent = 20,
      revision2Percent = 0,
      testScorePercent = 70
    )
    val mastery = chapter.overallMastery
    assertTrue("Mastery should be computed accurately", mastery in 30..60)
  }

  @Test
  fun testTaskPriorityModel() {
    val task = PlannerTask(
      id = "t1",
      subject = SubjectType.MATHEMATICS,
      chapter = "Quadratic Equations",
      topic = "Location of roots",
      taskType = TaskType.PYQ,
      estimatedMinutes = 45,
      priority = TaskPriority.CRITICAL
    )
    assertEquals(TaskPriority.CRITICAL, task.priority)
    assertEquals(45, task.estimatedMinutes)
  }
}

