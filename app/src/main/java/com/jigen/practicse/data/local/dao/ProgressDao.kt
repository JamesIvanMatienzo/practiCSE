package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.UserProgressEntity

@Dao
interface ProgressDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(progress: UserProgressEntity)

	@Query("SELECT * FROM user_progress WHERE questionId = :questionId LIMIT 1")
	suspend fun getProgress(questionId: Int): UserProgressEntity?

	@Query("SELECT * FROM user_progress")
	suspend fun getAllProgress(): List<UserProgressEntity>

	@Query("SELECT category, COUNT(*) as total, SUM(CASE WHEN isCorrect THEN 1 ELSE 0 END) as correct FROM user_progress GROUP BY category")
	suspend fun getProgressByCategory(): List<CategoryProgress>
}

data class CategoryProgress(
	val category: String,
	val total: Int,
	val correct: Int
)