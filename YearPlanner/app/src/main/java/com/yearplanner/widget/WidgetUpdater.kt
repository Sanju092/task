package com.yearplanner.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun updateAllWidgets(context: Context) {
        try {
            TodayWidget().updateAll(context)
        } catch (_: Exception) {}
        try {
            YearOverviewWidget().updateAll(context)
        } catch (_: Exception) {}
        try {
            YearAgendaWidget().updateAll(context)
        } catch (_: Exception) {}
        try {
            AnalyticsWidget().updateAll(context)
        } catch (_: Exception) {}
    }
}
