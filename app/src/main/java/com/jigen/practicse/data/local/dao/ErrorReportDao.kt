package com.jigen.practicse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jigen.practicse.data.local.entity.ErrorReportEntity

@Dao
interface ErrorReportDao {
	@Insert(onConflict = OnConflictStrategy.REPLACE)
	suspend fun insert(errorReport: ErrorReportEntity)

	@Query("SELECT * FROM error_reports WHERE isFlagged = 1")
	suspend fun getFlaggedReports(): List<ErrorReportEntity>
}