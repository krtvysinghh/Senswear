package com.senswear.app.domain

import com.senswear.app.core.domain.model.Goal
import com.senswear.app.core.domain.model.GoalType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GoalProgressTest {

    @Test
    fun `Goal calculates correct progress fraction and completion state`() {
        val goal = Goal(
            id = "g1",
            title = "Steps",
            targetValue = 10000.0,
            unit = "steps",
            currentValue = 8421.0,
            type = GoalType.DAILY_STEPS
        )

        assertEquals(0.8421f, goal.progressFraction, 0.001f)
        assertFalse(goal.isCompleted)

        val completedGoal = goal.copy(currentValue = 10500.0)
        assertTrue(completedGoal.isCompleted)
        assertEquals(1.05f, completedGoal.progressFraction, 0.01f)
    }
}
