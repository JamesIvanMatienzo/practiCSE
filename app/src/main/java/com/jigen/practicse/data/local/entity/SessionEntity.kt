package com.jigen.practicse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_session")
data class SessionEntity(
	@PrimaryKey val id: Int = 1,
	val lastScore: Int = 0,
	val lastTrack: String? = null,
	val lastCategory: String? = null,
	val lastQuestionIndex: Int = 0,
	val remainingTimeMillis: Long = 0L,
	val sessionSeed: Long = 0L,
	val questionIdsJson: String? = null,
	val flaggedQuestionIdsJson: String? = null,
	val examEndTimeMillis: Long? = null
)
