package com.yearplanner.widget.config

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.model.WidgetKind
import com.yearplanner.ui.theme.Theme
import com.yearplanner.widget.WidgetUpdater
import kotlinx.coroutines.launch

class WidgetConfigureActivity : ComponentActivity() {

    private var widgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID
    private var widgetKind: WidgetKind = WidgetKind.TODAY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val extras = intent.extras
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            val kindStr = extras.getString("widget_kind") ?: "TODAY"
            widgetKind = try {
                WidgetKind.valueOf(kindStr)
            } catch (_: Exception) {
                WidgetKind.TODAY
            }
        }
        
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            Theme {
                WidgetConfigureScreen(
                    widgetId = widgetId,
                    widgetKind = widgetKind,
                    onSave = {
                        val resultIntent = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun WidgetConfigureScreen(
    widgetId: Int,
    widgetKind: WidgetKind,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var theme by remember { mutableStateOf("system") }
    var surfaceOpacity by remember { mutableFloatStateOf(0.9f) }
    var privacyMode by remember { mutableStateOf(false) }
    
    // Today specific
    var todayMaxTasks by remember { mutableIntStateOf(5) }
    var todayShowCompleted by remember { mutableStateOf(false) }
    var todayShowOverdue by remember { mutableStateOf(true) }
    var todayViewMode by remember { mutableStateOf("compact") }
    
    // Overview specific
    var overviewViewMode by remember { mutableStateOf("heatmap") }
    
    // Agenda specific
    var agendaMaxTasks by remember { mutableIntStateOf(8) }
    var agendaShowCompleted by remember { mutableStateOf(false) }
    var agendaSortOrder by remember { mutableStateOf("date") }
    
    // Analytics specific
    var analyticsShowStreak by remember { mutableStateOf(true) }
    var analyticsShowProjectBreakdown by remember { mutableStateOf(true) }
    var analyticsShowSparklines by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configure ${widgetKind.name.replace('_', ' ')} Widget", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        
        // Appearance settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Appearance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                
                // Theme selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme", fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (key, label) ->
                            FilterChip(
                                selected = theme == key,
                                onClick = { theme = key },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
                
                // Surface opacity slider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Opacity", fontSize = 14.sp)
                        Text("${(surfaceOpacity * 100).toInt()}%", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = surfaceOpacity,
                        onValueChange = { surfaceOpacity = it },
                        valueRange = 0.2f..1f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Privacy mode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Privacy Mode", fontSize = 14.sp)
                        Text("Hide task titles on lock screen", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = privacyMode,
                        onCheckedChange = { privacyMode = it }
                    )
                }
            }
        }
        
        // Widget-specific settings
        when (widgetKind) {
            WidgetKind.TODAY -> {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Today Tasks Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Max Tasks", fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(3, 5, 8, 10).forEach { count ->
                                    FilterChip(
                                        selected = todayMaxTasks == count,
                                        onClick = { todayMaxTasks = count },
                                        label = { Text("$count", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Completed", fontSize = 14.sp)
                            Switch(checked = todayShowCompleted, onCheckedChange = { todayShowCompleted = it })
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Overdue Section", fontSize = 14.sp)
                            Switch(checked = todayShowOverdue, onCheckedChange = { todayShowOverdue = it })
                        }
                    }
                }
            }
            WidgetKind.YEAR_OVERVIEW -> {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Year Overview Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("View Style", fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("heatmap" to "Heatmap", "grid" to "Grid").forEach { (mode, label) ->
                                    FilterChip(
                                        selected = overviewViewMode == mode,
                                        onClick = { overviewViewMode = mode },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            WidgetKind.ANALYTICS -> {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Analytics Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Streak Counter", fontSize = 14.sp)
                            Switch(checked = analyticsShowStreak, onCheckedChange = { analyticsShowStreak = it })
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Project Breakdown", fontSize = 14.sp)
                            Switch(checked = analyticsShowProjectBreakdown, onCheckedChange = { analyticsShowProjectBreakdown = it })
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sparklines", fontSize = 14.sp)
                            Switch(checked = analyticsShowSparklines, onCheckedChange = { analyticsShowSparklines = it })
                        }
                    }
                }
            }
            WidgetKind.YEAR_AGENDA -> {
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Year Agenda Settings", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Max Tasks", fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(5, 8, 12, 15).forEach { count ->
                                    FilterChip(
                                        selected = agendaMaxTasks == count,
                                        onClick = { agendaMaxTasks = count },
                                        label = { Text("$count", fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Show Completed", fontSize = 14.sp)
                            Switch(checked = agendaShowCompleted, onCheckedChange = { agendaShowCompleted = it })
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Sort By", fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("date" to "Date", "project" to "Project").forEach { (order, label) ->
                                    FilterChip(
                                        selected = agendaSortOrder == order,
                                        onClick = { agendaSortOrder = order },
                                        label = { Text(label, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Save/Cancel buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = onCancel) {
                Text("Cancel")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(onClick = {
                scope.launch {
                    val prefs = context.createWidgetPreferences()
                    when (widgetKind) {
                        WidgetKind.TODAY -> {
                            prefs.setTodayMaxTasks(widgetId, todayMaxTasks)
                            prefs.setTodayShowCompleted(widgetId, todayShowCompleted)
                            prefs.setTodayShowOverdue(widgetId, todayShowOverdue)
                            prefs.setTodayViewMode(widgetId, todayViewMode)
                            prefs.setTodayTheme(widgetId, theme)
                            prefs.setTodayPrivacyMode(widgetId, privacyMode)
                        }
                        WidgetKind.YEAR_OVERVIEW -> {
                            prefs.setOverviewViewMode(widgetId, overviewViewMode)
                        }
                        WidgetKind.ANALYTICS -> {
                            prefs.setAnalyticsShowStreak(widgetId, analyticsShowStreak)
                            prefs.setAnalyticsShowProjectBreakdown(widgetId, analyticsShowProjectBreakdown)
                            prefs.setAnalyticsShowSparklines(widgetId, analyticsShowSparklines)
                        }
                        WidgetKind.YEAR_AGENDA -> {
                            prefs.setAgendaMaxTasks(widgetId, agendaMaxTasks)
                            prefs.setAgendaShowCompleted(widgetId, agendaShowCompleted)
                            prefs.setAgendaSortOrder(widgetId, agendaSortOrder)
                        }
                    }
                    prefs.setWidgetSurfaceOpacity(widgetId, surfaceOpacity)
                    WidgetUpdater.updateAllWidgets(context)
                    onSave()
                }
            }) {
                Text("Save Configuration")
            }
        }
    }
}
