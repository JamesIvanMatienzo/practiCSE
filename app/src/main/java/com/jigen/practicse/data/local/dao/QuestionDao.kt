package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {

	@Query("SELECT COUNT(*) FROM questions")
	suspend fun countQuestions(): Int

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insertAll(questions: List<QuestionEntity>)

	@Query("SELECT * FROM questions ORDER BY id ASC")
	suspend fun getAllQuestions(): List<QuestionEntity>

	@Query("SELECT * FROM questions ORDER BY id ASC")
	fun observeAllQuestions(): Flow<List<QuestionEntity>>
}
