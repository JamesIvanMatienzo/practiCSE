package com.jigen.practicse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "error_reports")
data class ErrorReportEntity(
	@PrimaryKey(autoGenerate = true) val id: Long = 0,
	val questionId: Int,
	val reportedAtMillis: Long,
	val isFlagged: Boolean = true
)