package com.jigen.practicse.data.local

import android.content.Context

private const val PREFS_NAME = "exam_config_prefs"
private const val KEY_NUMERICAL_COUNT = "numerical_count"
private const val KEY_VERBAL_COUNT = "verbal_count"
private const val KEY_GENERAL_COUNT = "general_count"

private const val DEFAULT_COUNT = 10

data class ExamQuestionConfig(
	val numericalCount: Int = DEFAULT_COUNT,
	val verbalCount: Int = DEFAULT_COUNT,
	val generalCount: Int = DEFAULT_COUNT
) {
	val totalAllExamCount: Int
		get() = numericalCount + verbalCount + generalCount

	fun countForCategoryKey(categoryKey: String): Int {
		return when (categoryKey) {
			"numerical_ability" -> numericalCount
			"verbal_ability" -> verbalCount
			"general_information" -> generalCount
			else -> totalAllExamCount
		}
	}
}

class ExamConfigStore(context: Context) {
	private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	fun getConfig(): ExamQuestionConfig {
		return ExamQuestionConfig(
			numericalCount = prefs.getInt(KEY_NUMERICAL_COUNT, DEFAULT_COUNT),
			verbalCount = prefs.getInt(KEY_VERBAL_COUNT, DEFAULT_COUNT),
			generalCount = prefs.getInt(KEY_GENERAL_COUNT, DEFAULT_COUNT)
		)
	}

	fun setNumericalCount(value: Int) {
		prefs.edit().putInt(KEY_NUMERICAL_COUNT, value).apply()
	}

	fun setVerbalCount(value: Int) {
		prefs.edit().putInt(KEY_VERBAL_COUNT, value).apply()
	}

	fun setGeneralCount(value: Int) {
		prefs.edit().putInt(KEY_GENERAL_COUNT, value).apply()
	}
}
