package com.jigen.practicse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
	@PrimaryKey val id: Int,
	val track: String,
	val category: String,
	val passage: String? = null,
	val questionText: String,
	val options: List<String>,
	val correctIndex: Int,
	val aiLogic: String? = null
)
