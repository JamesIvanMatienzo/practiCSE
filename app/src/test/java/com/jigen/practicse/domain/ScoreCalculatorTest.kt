package com.jigen.practicse.domain

import org.junit.Assert.*
import org.junit.Test

class ScoreCalculatorTest {

    @Test
    fun `correct percentage for perfect score`() {
        val result = ScoreCalculator.calculate(165, 165, 80)
        assertEquals(100.0, result.percentage, 0.0001)
        assertTrue(result.passed)
    }

    @Test
    fun `correct percentage for zero correct`() {
        val result = ScoreCalculator.calculate(165, 0, 80)
        assertEquals(0.0, result.percentage, 0.0001)
        assertFalse(result.passed)
    }

    @Test
    fun `pass threshold boundary`() {
        // 80% of 165 = 132.0, so 132 correct should be exactly 80%
        val result = ScoreCalculator.calculate(165, 132, 80)
        assertEquals(80.0, result.percentage, 0.0001)
        assertTrue(result.passed)
    }

    @Test
    fun `near boundary below fail`() {
        val result = ScoreCalculator.calculate(165, 131, 80)
        assertEquals((131 * 100.0) / 165, result.percentage, 0.0001)
        assertFalse(result.passed)
    }
}
