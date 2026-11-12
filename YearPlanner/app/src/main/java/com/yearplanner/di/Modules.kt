package com.yearplanner.di

import android.content.Context
import androidx.room.Room
import com.yearplanner.data.db.YearPlannerDatabase
import com.yearplanner.data.repository.TaskRepository
import com.yearplanner.domain.recurrence.RecurrenceGenerator
import com.yearplanner.domain.usecase.TaskUseCases
import com.yearplanner.reminders.ReminderScheduler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@Suppress("UNUSED_PARAMETER") context: Context): YearPlannerDatabase {
        return YearPlannerDatabase.getInstance(context)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideTaskRepository(database: YearPlannerDatabase): TaskRepository {
        return TaskRepository(database)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideTaskUseCases(repository: TaskRepository): TaskUseCases {
        return TaskUseCases(repository)
    }

    @Provides
    @Singleton
    fun provideRecurrenceGenerator(): RecurrenceGenerator {
        return RecurrenceGenerator
    }
}

@Module
@InstallIn(SingletonComponent::class)
object ReminderModule {

    @Provides
    @Singleton
    fun provideReminderScheduler(context: Context, repository: TaskRepository): ReminderScheduler {
        return ReminderScheduler(context, repository)
    }
}