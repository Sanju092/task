package com.yearplanner.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.glance.GlanceAppWidget
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.dp
import androidx.glance.unit.sp
import com.yearplanner.ui.MainActivity
import com.yearplanner.ui.QuickAddActivity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TodayWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val today = LocalDate.now()
        val formattedDate = today.format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Text(
                            text = "Today",
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                        Text(
                            text = formattedDate,
                            style = TextStyle(
                                fontSize = 14.sp,
                                color = ColorProvider(Color(0xFFB0B0B0))
                            )
                        )
                    }
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                Text(
                    text = "Tap to view today's tasks",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = ColorProvider(Color(0xFF81C784))
                    ),
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
                )

                Spacer(modifier = GlanceModifier.height(12.dp))

                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(4.dp)
                            .background(Color(0xFF2196F3))
                            .clickable(actionStartActivity(Intent(context, QuickAddActivity::class.java))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Add Task",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(Color.White)
                            ),
                            modifier = GlanceModifier.padding(8.dp)
                        )
                    }
                    Box(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .padding(4.dp)
                            .background(Color(0xFF333333))
                            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Open App",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = ColorProvider(Color.White)
                            ),
                            modifier = GlanceModifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

class YearOverviewWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val year = LocalDate.now().year

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            ) {
                Text(
                    text = "Year Overview $year",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.White)
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Tap to view all 12 months in Year Planner",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(Color(0xFFB0B0B0))
                    )
                )
            }
        }
    }
}

class YearAgendaWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, glanceId: GlanceId) {
        val year = LocalDate.now().year

        provideContent {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .padding(12.dp)
                    .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            ) {
                Text(
                    text = "Agenda $year",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorProvider(Color.White)
                    )
                )
                Spacer(modifier = GlanceModifier.height(8.dp))
                Text(
                    text = "Tap to open year agenda",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = ColorProvider(Color(0xFFB0B0B0))
                    )
                )
            }
        }
    }
}
