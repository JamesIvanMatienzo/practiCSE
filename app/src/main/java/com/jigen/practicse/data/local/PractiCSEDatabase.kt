package com.jigen.practicse.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jigen.practicse.data.local.dao.ProgressDao
import com.jigen.practicse.data.local.dao.QuestionDao
import com.jigen.practicse.data.local.dao.SessionDao
import com.jigen.practicse.data.local.entity.QuestionEntity
import com.jigen.practicse.data.local.entity.SessionEntity
import com.jigen.practicse.data.local.entity.UserProgressEntity

@Database(
	entities = [QuestionEntity::class, SessionEntity::class, UserProgressEntity::class],
	version = 2,
	exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PractiCSEDatabase : RoomDatabase() {

	abstract fun questionDao(): QuestionDao

	abstract fun sessionDao(): SessionDao

	abstract fun progressDao(): ProgressDao

	companion object {

		private const val DATABASE_NAME = "practicse.db"

		@Volatile
		private var instance: PractiCSEDatabase? = null

		fun getInstance(context: Context): PractiCSEDatabase {
			return instance ?: synchronized(this) {
				instance ?: Room.databaseBuilder(
					context.applicationContext,
					PractiCSEDatabase::class.java,
					DATABASE_NAME
				).fallbackToDestructiveMigration().build().also { instance = it }
			}
		}
	}
}
