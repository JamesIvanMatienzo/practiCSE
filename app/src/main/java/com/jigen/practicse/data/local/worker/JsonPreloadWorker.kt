package com.jigen.practicse.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.local.entity.QuestionEntity
import org.json.JSONArray
import org.json.JSONObject

class JsonPreloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return runCatching {
            val database = PractiCSEDatabase.getInstance(applicationContext)
            val questionDao = database.questionDao()

            if (questionDao.countQuestions() > 0) {
                return Result.success()
            }

            val seedQuestions = loadSeedQuestions()
            if (seedQuestions.isNotEmpty()) {
                questionDao.insertAll(seedQuestions)
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }

    private fun loadSeedQuestions(): List<QuestionEntity> {
        val unifiedQuestions = readQuestionsFromAsset("questions.json", null)
        if (unifiedQuestions.isNotEmpty()) {
            return unifiedQuestions
        }

        return readQuestionsFromAsset("question_bank/professional.json", "Professional") +
            readQuestionsFromAsset("question_bank/sub_professional.json", "Sub-Professional")
    }

    private fun readQuestionsFromAsset(
        assetPath: String,
        defaultTrack: String?
    ): List<QuestionEntity> {
        val rawJson = runCatching {
            applicationContext.assets.open(assetPath).bufferedReader().use { it.readText() }
        }.getOrNull()?.trim().orEmpty()

        if (rawJson.isBlank()) {
            return emptyList()
        }

        val questionsArray = when {
            rawJson.startsWith("[") -> JSONArray(rawJson)
            rawJson.startsWith("{") -> JSONObject(rawJson).optJSONArray("questions") ?: JSONArray()
            else -> JSONArray(rawJson)
        }

        return buildList {
            for (index in 0 until questionsArray.length()) {
                val item = questionsArray.optJSONObject(index) ?: continue
                item.toQuestionEntity(index + 1, defaultTrack)?.let(::add)
            }
        }
    }

    private fun JSONObject.toQuestionEntity(
        fallbackId: Int,
        defaultTrack: String?
    ): QuestionEntity? {
        val questionText = optString("questionText")
            .takeIf { it.isNotBlank() }
            ?: optString("question_text").takeIf { it.isNotBlank() }
            ?: return null

        val optionsArray = optJSONArray("options") ?: JSONArray()
        val options = buildList {
            for (index in 0 until optionsArray.length()) {
                optionsArray.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }

        if (options.isEmpty()) {
            return null
        }

        return QuestionEntity(
            id = optInt("id", fallbackId),
            track = optString("track").takeIf { it.isNotBlank() } ?: defaultTrack ?: "Professional",
            category = optString("category").takeIf { it.isNotBlank() } ?: "General",
            passage = optString("passage").takeIf { it.isNotBlank() },
            questionText = questionText,
            options = options,
            correctIndex = optInt("correctIndex", optInt("correct_index", 0)),
            aiLogic = optString("aiLogic").takeIf { it.isNotBlank() }
                ?: optString("ai_logic").takeIf { it.isNotBlank() }
        )
    }
}