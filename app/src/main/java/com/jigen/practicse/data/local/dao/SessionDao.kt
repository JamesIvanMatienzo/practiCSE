package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

	@Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
	suspend fun getSession(): SessionEntity?

	@Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
	fun observeSession(): Flow<SessionEntity?>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(session: SessionEntity)

	@Delete
	suspend fun delete(session: SessionEntity)

	@Query("DELETE FROM user_session WHERE examEndTimeMillis IS NOT NULL")
	suspend fun clearCompletedSessions()

	@Query("DELETE FROM user_session")
	suspend fun deleteAllSessions()
}
