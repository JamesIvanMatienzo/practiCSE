package com.jigen.practicse.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.random.Random

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String, // "General Information", "Numerical Ability", "Verbal Ability"
    val subCategory: String, // e.g., "Reading Comprehension", "Synonyms"
    val questionText: String,
    val referenceText: String? = null, // Nullable for reading comprehension passages
    val correctAnswer: String,
    val wrongChoices: List<String> // Handled by a Room TypeConverter
) {
    /**
     * Returns a randomized list of all answer choices (correct + wrong).
     * Useful for shuffling options once per session during exam initialization.
     */
    fun getShuffledOptions(sessionSeed: Long): List<String> {
        return (wrongChoices + correctAnswer).shuffled(Random(sessionSeed + id.toLong()))
    }
}