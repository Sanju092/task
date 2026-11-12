package com.yearplanner

import android.app.Application
import androidx.room.Room
import com.yearplanner.data.db.YearPlannerDatabase
import com.yearplanner.data.repository.TaskRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class YearPlannerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize database
        YearPlannerDatabase.getInstance(this)
    }
}