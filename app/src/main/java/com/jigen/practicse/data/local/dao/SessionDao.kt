package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.SessionEntity

@Dao
interface SessionDao {

	@Query("SELECT * FROM user_session WHERE id = 1 LIMIT 1")
	suspend fun getSession(): SessionEntity?

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun upsert(session: SessionEntity)
}
