package com.yearplanner.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.yearplanner.data.model.WidgetKind

abstract class BaseWidgetReceiver(private val widgetKind: WidgetKind) : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = when (widgetKind) {
            WidgetKind.TODAY -> TodayWidget()
            WidgetKind.YEAR_OVERVIEW -> YearOverviewWidget()
            WidgetKind.YEAR_AGENDA -> YearAgendaWidget()
            WidgetKind.ANALYTICS -> AnalyticsWidget()
        }
}

class TodayWidgetReceiver : BaseWidgetReceiver(WidgetKind.TODAY)
class YearOverviewWidgetReceiver : BaseWidgetReceiver(WidgetKind.YEAR_OVERVIEW)
class YearAgendaWidgetReceiver : BaseWidgetReceiver(WidgetKind.YEAR_AGENDA)
class AnalyticsWidgetReceiver : BaseWidgetReceiver(WidgetKind.ANALYTICS)
