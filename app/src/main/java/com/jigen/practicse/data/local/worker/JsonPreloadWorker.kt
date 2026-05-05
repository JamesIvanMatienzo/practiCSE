package com.jigen.practicse.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jigen.practicse.data.local.PractiCSEDatabase
import com.jigen.practicse.data.entity.QuestionEntity
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

            // If we already have questions, don't overwrite them
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
        val rawJson = runCatching {
            applicationContext.assets.open("question_bank/questions.json").bufferedReader().use { it.readText() }
        }.getOrNull()?.trim().orEmpty()

        if (rawJson.isBlank()) return emptyList()

        val questionsArray = JSONArray(rawJson)

        return buildList {
            for (index in 0 until questionsArray.length()) {
                val item = questionsArray.optJSONObject(index) ?: continue
                // Give it a sequential ID starting at 1
                item.toQuestionEntity(index + 1)?.let(::add)
            }
        }
    }

    private fun JSONObject.toQuestionEntity(assignedId: Int): QuestionEntity? {
        val questionText = optString("questionText").takeIf { it.isNotBlank() } ?: return null
        val correctAnswer = optString("correctAnswer").takeIf { it.isNotBlank() } ?: return null
        
        val wrongChoicesArray = optJSONArray("wrongChoices") ?: JSONArray()
        val wrongChoices = buildList {
            for (index in 0 until wrongChoicesArray.length()) {
                wrongChoicesArray.optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }

        if (wrongChoices.isEmpty()) return null

        val refText = optString("referenceText")

        return QuestionEntity(
            id = assignedId,
            category = optString("category", "General"),
            subCategory = optString("subCategory", "General"),
            questionText = questionText,
            referenceText = if (refText == "null" || refText.isBlank()) null else refText,
            correctAnswer = correctAnswer,
            wrongChoices = wrongChoices
        )
    }
}