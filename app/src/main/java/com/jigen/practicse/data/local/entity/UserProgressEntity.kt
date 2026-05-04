package com.jigen.practicse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_progress")
data class UserProgressEntity(
	@PrimaryKey val questionId: Int,
	val selectedIndex: Int,
	val isCorrect: Boolean,
	val answeredAtMillis: Long,
	val track: String,
	val category: String
)