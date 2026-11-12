package com.yearplanner.widget.config

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.widgetPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "widget_preferences")

fun Context.createWidgetPreferences(): WidgetPreferences {
    return WidgetPreferences(widgetPreferencesDataStore)
}

class WidgetPreferences(private val dataStore: DataStore<Preferences>) {

    companion object {
        // Today Widget
        private val TODAY_MAX_TASKS = intPreferencesKey("today_max_tasks")
        private val TODAY_SHOW_COMPLETED = booleanPreferencesKey("today_show_completed")
        private val TODAY_SHOW_OVERDUE = booleanPreferencesKey("today_show_overdue")
        private val TODAY_VIEW_MODE = stringPreferencesKey("today_view_mode") // "compact", "expanded"
        private val TODAY_PROJECT_FILTER = stringSetPreferencesKey("today_project_filter")
        private val TODAY_THEME = stringPreferencesKey("today_theme") // "light", "dark", "system"
        private val TODAY_PRIVACY_MODE = booleanPreferencesKey("today_privacy_mode")

        // Year Overview Widget
        private val OVERVIEW_SELECTED_YEAR = intPreferencesKey("overview_selected_year")
        private val OVERVIEW_VIEW_MODE = stringPreferencesKey("overview_view_mode") // "heatmap", "grid", "day_grid"
        private val OVERVIEW_PROJECT_FILTER = stringSetPreferencesKey("overview_project_filter")
        private val OVERVIEW_THEME = stringPreferencesKey("overview_theme")
        private val OVERVIEW_PRIVACY_MODE = booleanPreferencesKey("overview_privacy_mode")

        // Analytics Widget
        private val ANALYTICS_SHOW_STREAK = booleanPreferencesKey("analytics_show_streak")
        private val ANALYTICS_SHOW_PROJECT_BREAKDOWN = booleanPreferencesKey("analytics_show_project_breakdown")
        private val ANALYTICS_SHOW_SPARKLINES = booleanPreferencesKey("analytics_show_sparklines")
        private val ANALYTICS_THEME = stringPreferencesKey("analytics_theme")
        private val ANALYTICS_PRIVACY_MODE = booleanPreferencesKey("analytics_privacy_mode")

        // Year Agenda Widget
        private val AGENDA_MAX_TASKS = intPreferencesKey("agenda_max_tasks")
        private val AGENDA_SHOW_COMPLETED = booleanPreferencesKey("agenda_show_completed")
        private val AGENDA_SORT_ORDER = stringPreferencesKey("agenda_sort_order") // "date", "project", "priority"
        private val AGENDA_PROJECT_FILTER = stringSetPreferencesKey("agenda_project_filter")
        private val AGENDA_THEME = stringPreferencesKey("agenda_theme")
        private val AGENDA_PRIVACY_MODE = booleanPreferencesKey("agenda_privacy_mode")

        // General
        private val WIDGET_SURFACE_OPACITY = intPreferencesKey("widget_surface_opacity") // 0-100
    }

    suspend fun getTodayMaxTasks(appWidgetId: Int): Int {
        return dataStore.data
            .map { it[TODAY_MAX_TASKS] ?: 5 }
            .first()
    }

    suspend fun setTodayMaxTasks(appWidgetId: Int, value: Int) {
        dataStore.edit { it[TODAY_MAX_TASKS] = value.coerceIn(3, 15) }
    }

    suspend fun getTodayShowCompleted(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[TODAY_SHOW_COMPLETED] ?: false }.first()
    }

    suspend fun setTodayShowCompleted(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[TODAY_SHOW_COMPLETED] = value }
    }

    suspend fun getTodayShowOverdue(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[TODAY_SHOW_OVERDUE] ?: true }.first()
    }

    suspend fun setTodayShowOverdue(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[TODAY_SHOW_OVERDUE] = value }
    }

    suspend fun getTodayViewMode(appWidgetId: Int): String {
        return dataStore.data.map { it[TODAY_VIEW_MODE] ?: "compact" }.first()
    }

    suspend fun setTodayViewMode(appWidgetId: Int, value: String) {
        dataStore.edit { it[TODAY_VIEW_MODE] = value }
    }

    suspend fun getTodayProjectFilter(appWidgetId: Int): Set<String> {
        return dataStore.data.map { it[TODAY_PROJECT_FILTER] ?: emptySet() }.first()
    }

    suspend fun setTodayProjectFilter(appWidgetId: Int, value: Set<String>) {
        dataStore.edit { it[TODAY_PROJECT_FILTER] = value }
    }

    suspend fun getTodayTheme(appWidgetId: Int): String {
        return dataStore.data.map { it[TODAY_THEME] ?: "system" }.first()
    }

    suspend fun setTodayTheme(appWidgetId: Int, value: String) {
        dataStore.edit { it[TODAY_THEME] = value }
    }

    suspend fun getTodayPrivacyMode(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[TODAY_PRIVACY_MODE] ?: false }.first()
    }

    suspend fun setTodayPrivacyMode(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[TODAY_PRIVACY_MODE] = value }
    }

    // Year Overview
    suspend fun getOverviewSelectedYear(appWidgetId: Int): Int {
        return dataStore.data.map { it[OVERVIEW_SELECTED_YEAR] ?: java.time.Year.now().value }.first()
    }

    suspend fun setOverviewSelectedYear(appWidgetId: Int, value: Int) {
        dataStore.edit { it[OVERVIEW_SELECTED_YEAR] = value }
    }

    suspend fun getOverviewViewMode(appWidgetId: Int): String {
        return dataStore.data.map { it[OVERVIEW_VIEW_MODE] ?: "heatmap" }.first()
    }

    suspend fun setOverviewViewMode(appWidgetId: Int, value: String) {
        dataStore.edit { it[OVERVIEW_VIEW_MODE] = value }
    }

    // Analytics
    suspend fun getAnalyticsShowStreak(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[ANALYTICS_SHOW_STREAK] ?: true }.first()
    }

    suspend fun setAnalyticsShowStreak(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[ANALYTICS_SHOW_STREAK] = value }
    }

    suspend fun getAnalyticsShowProjectBreakdown(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[ANALYTICS_SHOW_PROJECT_BREAKDOWN] ?: true }.first()
    }

    suspend fun setAnalyticsShowProjectBreakdown(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[ANALYTICS_SHOW_PROJECT_BREAKDOWN] = value }
    }

    suspend fun getAnalyticsShowSparklines(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[ANALYTICS_SHOW_SPARKLINES] ?: false }.first()
    }

    suspend fun setAnalyticsShowSparklines(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[ANALYTICS_SHOW_SPARKLINES] = value }
    }

    // Year Agenda
    suspend fun getAgendaMaxTasks(appWidgetId: Int): Int {
        return dataStore.data.map { it[AGENDA_MAX_TASKS] ?: 8 }.first()
    }

    suspend fun setAgendaMaxTasks(appWidgetId: Int, value: Int) {
        dataStore.edit { it[AGENDA_MAX_TASKS] = value.coerceIn(3, 20) }
    }

    suspend fun getAgendaShowCompleted(appWidgetId: Int): Boolean {
        return dataStore.data.map { it[AGENDA_SHOW_COMPLETED] ?: false }.first()
    }

    suspend fun setAgendaShowCompleted(appWidgetId: Int, value: Boolean) {
        dataStore.edit { it[AGENDA_SHOW_COMPLETED] = value }
    }

    suspend fun getAgendaSortOrder(appWidgetId: Int): String {
        return dataStore.data.map { it[AGENDA_SORT_ORDER] ?: "date" }.first()
    }

    suspend fun setAgendaSortOrder(appWidgetId: Int, value: String) {
        dataStore.edit { it[AGENDA_SORT_ORDER] = value }
    }

    // General
    suspend fun getWidgetSurfaceOpacity(appWidgetId: Int): Float {
        return dataStore.data.map { (it[WIDGET_SURFACE_OPACITY] ?: 90) / 100f }.first()
    }

    suspend fun setWidgetSurfaceOpacity(appWidgetId: Int, value: Float) {
        dataStore.edit { it[WIDGET_SURFACE_OPACITY] = (value * 100).toInt().coerceIn(10, 100) }
    }

    // Helper to get all prefs for a widget kind
    data class TodayConfig(
        val maxTasks: Int = 5,
        val showCompleted: Boolean = false,
        val showOverdue: Boolean = true,
        val viewMode: String = "compact",
        val projectFilter: Set<String> = emptySet(),
        val theme: String = "system",
        val privacyMode: Boolean = false,
        val surfaceOpacity: Float = 0.9f
    )

    data class OverviewConfig(
        val selectedYear: Int = java.time.Year.now().value,
        val viewMode: String = "heatmap",
        val projectFilter: Set<String> = emptySet(),
        val theme: String = "system",
        val privacyMode: Boolean = false,
        val surfaceOpacity: Float = 0.9f
    )

    data class AnalyticsConfig(
        val showStreak: Boolean = true,
        val showProjectBreakdown: Boolean = true,
        val showSparklines: Boolean = false,
        val theme: String = "system",
        val privacyMode: Boolean = false,
        val surfaceOpacity: Float = 0.9f
    )

    data class AgendaConfig(
        val maxTasks: Int = 8,
        val showCompleted: Boolean = false,
        val sortOrder: String = "date",
        val projectFilter: Set<String> = emptySet(),
        val theme: String = "system",
        val privacyMode: Boolean = false,
        val surfaceOpacity: Float = 0.9f
    )

    suspend fun getTodayConfig(appWidgetId: Int): TodayConfig {
        return dataStore.data.map { prefs ->
            TodayConfig(
                maxTasks = prefs[TODAY_MAX_TASKS] ?: 5,
                showCompleted = prefs[TODAY_SHOW_COMPLETED] ?: false,
                showOverdue = prefs[TODAY_SHOW_OVERDUE] ?: true,
                viewMode = prefs[TODAY_VIEW_MODE] ?: "compact",
                projectFilter = prefs[TODAY_PROJECT_FILTER] ?: emptySet(),
                theme = prefs[TODAY_THEME] ?: "system",
                privacyMode = prefs[TODAY_PRIVACY_MODE] ?: false,
                surfaceOpacity = (prefs[WIDGET_SURFACE_OPACITY] ?: 90) / 100f
            )
        }.first()
    }

    suspend fun getOverviewConfig(appWidgetId: Int): OverviewConfig {
        return dataStore.data.map { prefs ->
            OverviewConfig(
                selectedYear = prefs[OVERVIEW_SELECTED_YEAR] ?: java.time.Year.now().value,
                viewMode = prefs[OVERVIEW_VIEW_MODE] ?: "heatmap",
                projectFilter = prefs[OVERVIEW_PROJECT_FILTER] ?: emptySet(),
                theme = prefs[OVERVIEW_THEME] ?: "system",
                privacyMode = prefs[OVERVIEW_PRIVACY_MODE] ?: false,
                surfaceOpacity = (prefs[WIDGET_SURFACE_OPACITY] ?: 90) / 100f
            )
        }.first()
    }

    suspend fun getAnalyticsConfig(appWidgetId: Int): AnalyticsConfig {
        return dataStore.data.map { prefs ->
            AnalyticsConfig(
                showStreak = prefs[ANALYTICS_SHOW_STREAK] ?: true,
                showProjectBreakdown = prefs[ANALYTICS_SHOW_PROJECT_BREAKDOWN] ?: true,
                showSparklines = prefs[ANALYTICS_SHOW_SPARKLINES] ?: false,
                theme = prefs[ANALYTICS_THEME] ?: "system",
                privacyMode = prefs[ANALYTICS_PRIVACY_MODE] ?: false,
                surfaceOpacity = (prefs[WIDGET_SURFACE_OPACITY] ?: 90) / 100f
            )
        }.first()
    }

    suspend fun getAgendaConfig(appWidgetId: Int): AgendaConfig {
        return dataStore.data.map { prefs ->
            AgendaConfig(
                maxTasks = prefs[AGENDA_MAX_TASKS] ?: 8,
                showCompleted = prefs[AGENDA_SHOW_COMPLETED] ?: false,
                sortOrder = prefs[AGENDA_SORT_ORDER] ?: "date",
                projectFilter = prefs[AGENDA_PROJECT_FILTER] ?: emptySet(),
                theme = prefs[AGENDA_THEME] ?: "system",
                privacyMode = prefs[AGENDA_PRIVACY_MODE] ?: false,
                surfaceOpacity = (prefs[WIDGET_SURFACE_OPACITY] ?: 90) / 100f
            )
        }.first()
    }
}
