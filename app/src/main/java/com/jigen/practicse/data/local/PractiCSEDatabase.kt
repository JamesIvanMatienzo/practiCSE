package com.jigen.practicse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jigen.practicse.data.local.dao.ErrorReportDao
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.QuestionDao
import com.jigen.practicse.data.local.dao.SessionDao
import com.jigen.practicse.data.local.entity.ErrorReportEntity
import com.jigen.practicse.data.entity.QuestionEntity
import com.jigen.practicse.data.local.entity.SessionEntity
import com.jigen.practicse.data.local.entity.UserProgressEntity

@Database(
	entities = [QuestionEntity::class, SessionEntity::class, UserProgressEntity::class, ErrorReportEntity::class, com.jigen.practicse.data.local.entity.LeaderboardEntryEntity::class],
	version = 4,
	exportSchema = false
)	
@TypeConverters(Converters::class)
abstract class PractiCSEDatabase : RoomDatabase() {

	abstract fun questionDao(): QuestionDao

	abstract fun sessionDao(): SessionDao

	abstract fun progressDao(): ProgressDao

	abstract fun errorReportDao(): ErrorReportDao

	abstract fun leaderboardDao(): com.jigen.practicse.data.local.dao.LeaderboardDao

	companion object {

		private const val DATABASE_NAME = "practicse.db"

		@Volatile
		private var instance: PractiCSEDatabase? = null

		fun getInstance(context: Context): PractiCSEDatabase {
			return instance ?: synchronized(this) {
				try {
					instance ?: Room.databaseBuilder(
						context.applicationContext,
						PractiCSEDatabase::class.java,
						DATABASE_NAME
					).fallbackToDestructiveMigration().build().also { instance = it }
				} catch (e: Exception) {
					// Log migration or DB open errors to CrashReporter for observability
					com.jigen.practicse.util.CrashReporter.recordException(e, "RoomDatabaseOpenError")
					throw e
				}
			}
		}
	}
}
