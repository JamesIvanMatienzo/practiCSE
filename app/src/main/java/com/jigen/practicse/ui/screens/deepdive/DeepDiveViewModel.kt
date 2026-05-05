package com.jigen.practicse.ui.screens.deepdive

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jigen.practicse.BuildConfig
import com.jigen.practicse.data.entity.QuestionEntity
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.dao.QuestionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed class DeepDiveUiState {
	data object Loading : DeepDiveUiState()
	data class Success(
		val question: QuestionEntity,
		val explanation: String
	) : DeepDiveUiState()
	data class Error(val message: String) : DeepDiveUiState()
}

class DeepDiveViewModel(
	private val questionDao: QuestionDao,
	private val questionId: Int
) : ViewModel() {

	private val _uiState = MutableStateFlow<DeepDiveUiState>(DeepDiveUiState.Loading)
	val uiState: StateFlow<DeepDiveUiState> = _uiState.asStateFlow()

	companion object {
		fun factory(context: Context, questionId: String): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
			@Suppress("UNCHECKED_CAST")
			override fun <T : ViewModel> create(modelClass: Class<T>): T {
				val database = PractiCSEDatabase.getInstance(context)
				return DeepDiveViewModel(
					database.questionDao(),
					questionId.toIntOrNull() ?: -1
				) as T
			}
		}
	}

	init {
		viewModelScope.launch {
			loadDeepDive()
		}
	}

	private suspend fun loadDeepDive() {
		if (questionId <= 0) {
			_uiState.value = DeepDiveUiState.Error("Invalid question id")
			return
		}

		try {
			val question = questionDao.getQuestionById(questionId)
			if (question == null) {
				_uiState.value = DeepDiveUiState.Error("Question not found")
				return
			}

			val explanation = generateExplanation(question)
			_uiState.value = DeepDiveUiState.Success(question = question, explanation = explanation)
		} catch (e: Exception) {
			_uiState.value = DeepDiveUiState.Error(e.message ?: "Failed to load deep dive")
		}
	}

	private suspend fun generateExplanation(question: QuestionEntity): String = withContext(Dispatchers.IO) {
		val apiKey = BuildConfig.GROK_API_KEY.trim()
		val baseUrl = BuildConfig.GROK_BASE_URL.trim().ifBlank { "https://api.x.ai/v1" }
		val model = BuildConfig.GROK_MODEL.trim().ifBlank { "grok-3-mini" }

		if (apiKey.isBlank()) {
			return@withContext "Add GROK_API_KEY in .env.local to generate an AI explanation for this question.\n\nCorrect answer: ${question.correctAnswer}"
		}

		val options = (question.wrongChoices + question.correctAnswer).distinct()
		val prompt = buildString {
			appendLine("You are a concise civil service exam tutor.")
			appendLine("Explain this multiple-choice question step by step.")
			appendLine("Keep the answer practical, clear, and under 250 words.")
			appendLine("Question: ${question.questionText}")
			question.referenceText?.takeIf { it.isNotBlank() }?.let {
				appendLine("Reference text: $it")
			}
			appendLine("Correct answer: ${question.correctAnswer}")
			appendLine("Choices: ${options.joinToString(", ")}")
			appendLine("Give a short reason why the correct answer is right and why the other choices are wrong.")
		}

		val requestBody = JSONObject().apply {
			put("model", model)
			put(
				"messages",
				listOf(
					mapOf(
						"role" to "system",
						"content" to "You are a helpful exam tutor."
					),
					mapOf(
						"role" to "user",
						"content" to prompt
					)
				)
			)
			put("temperature", 0.2)
		}

		val connection = (URL("${baseUrl.trimEnd('/')}/chat/completions").openConnection() as HttpURLConnection).apply {
			requestMethod = "POST"
			connectTimeout = 15_000
			readTimeout = 30_000
			doOutput = true
			setRequestProperty("Authorization", "Bearer $apiKey")
			setRequestProperty("Content-Type", "application/json")
		}

		try {
			connection.outputStream.use { output ->
				output.write(requestBody.toString().toByteArray())
			}

			val responseCode = connection.responseCode
			val responseText = if (responseCode in 200..299) {
				connection.inputStream.bufferedReader().use { it.readText() }
			} else {
				connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
			}

			if (responseText.isBlank()) {
				return@withContext "No explanation was returned from Grok (HTTP $responseCode). Correct answer: ${question.correctAnswer}"
			}

			try {
				val root = JSONObject(responseText)
				
				// Check for error response
				if (root.has("error")) {
					val errorObj = root.optJSONObject("error")
					val errorMsg = errorObj?.optString("message") ?: root.optString("error").toString()
					return@withContext "Grok API Error: $errorMsg\n\nCorrect answer: ${question.correctAnswer}"
				}
				
				val choices = root.optJSONArray("choices")
				if (choices == null || choices.length() == 0) {
					return@withContext "No choices found in Grok response. Correct answer: ${question.correctAnswer}"
				}
				
				val firstChoice = choices.getJSONObject(0)
				val message = firstChoice.optJSONObject("message")
				if (message == null) {
					return@withContext "Invalid message format in Grok response. Correct answer: ${question.correctAnswer}"
				}
				
				val content = message.optString("content", "").trim()
				
				if (content.isNotBlank()) {
					content
				} else {
					"Grok returned an empty explanation. Correct answer: ${question.correctAnswer}"
				}
			} catch (jsonError: Exception) {
				"Failed to parse Grok response: ${jsonError.message}\n\nCorrect answer: ${question.correctAnswer}"
			}
		} catch (e: Exception) {
			"Network error while contacting Grok: ${e.message}\n\nCorrect answer: ${question.correctAnswer}"
		} finally {
			connection.disconnect()
		}
	}
}
