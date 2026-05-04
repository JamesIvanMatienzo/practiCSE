package com.jigen.practicse.domain

data class ScoreResult(val totalQuestions: Int, val correct: Int, val percentage: Double, val passed: Boolean)

object ScoreCalculator {
    fun calculate(totalQuestions: Int, correctAnswers: Int, passingThresholdPercent: Int): ScoreResult {
        val percentage = if (totalQuestions > 0) (correctAnswers * 100.0) / totalQuestions else 0.0
        val passed = percentage >= passingThresholdPercent
        return ScoreResult(totalQuestions = totalQuestions, correct = correctAnswers, percentage = percentage, passed = passed)
    }
}
