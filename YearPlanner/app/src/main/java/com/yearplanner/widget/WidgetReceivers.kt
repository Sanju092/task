package com.yearplanner.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceAppWidgetReceiver
import androidx.glance.GlanceId
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.yearplanner.data.model.WidgetKind

abstract class BaseWidgetReceiver(private val widgetKind: WidgetKind) : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget
        get() = when (widgetKind) {
            WidgetKind.TODAY -> TodayWidget()
            WidgetKind.YEAR_OVERVIEW -> YearOverviewWidget()
            WidgetKind.YEAR_AGENDA -> YearAgendaWidget()
        }
}

class TodayWidgetReceiver : BaseWidgetReceiver(WidgetKind.TODAY)
class YearOverviewWidgetReceiver : BaseWidgetReceiver(WidgetKind.YEAR_OVERVIEW)
class YearAgendaWidgetReceiver : BaseWidgetReceiver(WidgetKind.YEAR_AGENDA)