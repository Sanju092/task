package com.yearplanner.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.yearplanner.data.db.converters.Converters
import com.yearplanner.data.db.dao.*
import com.yearplanner.data.model.*

@Database(
    entities = [
        Project::class,
        Task::class,
        RecurrenceSeries::class,
        TaskOccurrence::class,
        Reminder::class,
        WidgetConfig::class,
        UndoAction::class,
        GenerationCoverage::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class YearPlannerDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
    abstract fun recurrenceSeriesDao(): RecurrenceSeriesDao
    abstract fun taskOccurrenceDao(): TaskOccurrenceDao
    abstract fun reminderDao(): ReminderDao
    abstract fun widgetConfigDao(): WidgetConfigDao
    abstract fun undoActionDao(): UndoActionDao
    abstract fun generationCoverageDao(): GenerationCoverageDao

    companion object {
        @Volatile
        private var INSTANCE: YearPlannerDatabase? = null

        fun getInstance(context: Context): YearPlannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YearPlannerDatabase::class.java,
                    "year_planner.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migrations will go here
            }
        }
    }
}