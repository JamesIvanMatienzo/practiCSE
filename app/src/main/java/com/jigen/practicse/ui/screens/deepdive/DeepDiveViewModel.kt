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
import org.json.JSONArray
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
		val apiKey = BuildConfig.GROQ_API_KEY.trim()
		val baseUrl = BuildConfig.GROQ_BASE_URL.trim().ifBlank { "https://api.groq.com/openai/v1" }
		val model = BuildConfig.GROQ_MODEL.trim().ifBlank { "openai/gpt-oss-120b" }

		if (apiKey.isBlank()) {
			return@withContext "Add GROQ_API_KEY in .env.local to generate an AI explanation for this question.\n\nCorrect answer: ${question.correctAnswer}"
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
				JSONArray().apply {
					put(
						JSONObject().apply {
							put("role", "system")
							put("content", "You are a helpful exam tutor.")
						}
					)
					put(
						JSONObject().apply {
							put("role", "user")
							put("content", prompt)
						}
					)
				}
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
				return@withContext "No explanation was returned from AI (HTTP $responseCode). Correct answer: ${question.correctAnswer}"
			}

			if (responseCode !in 200..299) {
				return@withContext "AI request failed (HTTP $responseCode): $responseText\n\nCorrect answer: ${question.correctAnswer}"
			}

			try {
				val root = JSONObject(responseText)
				
				// Check for error response
				if (root.has("error")) {
					val errorObj = root.optJSONObject("error")
					val errorMsg = errorObj?.optString("message") ?: root.optString("error").toString()
					return@withContext "AI API Error: $errorMsg\n\nCorrect answer: ${question.correctAnswer}"
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
				
				val content = extractContent(message)
				
				if (content.isNotBlank()) {
					sanitizeAiOutput(content)
				} else {
					"AI returned an empty explanation. Correct answer: ${question.correctAnswer}"
				}
			} catch (jsonError: Exception) {
				"Failed to parse Grok response: ${jsonError.message}\n\nCorrect answer: ${question.correctAnswer}"
			}
		} catch (e: Exception) {
			"Network error while contacting AI: ${e.message}\n\nCorrect answer: ${question.correctAnswer}"
		} finally {
			connection.disconnect()
		}
	}

	private fun sanitizeAiOutput(raw: String): String {
		return raw
			.replace("\r", "")
			.replace(Regex("(?m)^\\s*#{1,6}\\s*"), "")
			.replace("**", "")
			.replace("__", "")
			.replace(Regex("(?m)^\\s*[\\-*+]\\s+"), "")
			.replace(Regex("(?m)^\\s*\\|?\\s*:?-{3,}.*$"), "")
			.replace("|", " ")
			.replace(Regex("[ \\t]{2,}"), " ")
			.replace(Regex("\\n{3,}"), "\\n\\n")
			.trim()
	}

	private fun extractContent(message: JSONObject): String {
		val raw = message.opt("content")
		if (raw is String) return raw.trim()
		if (raw is JSONArray) {
			val text = buildString {
				for (i in 0 until raw.length()) {
					val part = raw.opt(i)
					when (part) {
						is String -> append(part)
						is JSONObject -> {
							val nestedText = part.optString("text", "")
							if (nestedText.isNotBlank()) append(nestedText)
						}
					}
				}
			}
			return text.trim()
		}
		return ""
	}
}
