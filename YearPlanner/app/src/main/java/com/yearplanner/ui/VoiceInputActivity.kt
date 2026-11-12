package com.yearplanner.ui

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yearplanner.data.model.Task
import com.yearplanner.data.model.TaskOccurrence
import com.yearplanner.data.repository.TaskRepository
import com.yearplanner.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Locale
import java.util.regex.Pattern

@AndroidEntryPoint
class VoiceInputActivity : ComponentActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechIntent: Intent
    private var repository: TaskRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawableResource(android.R.color.transparent)
        
        val db = com.yearplanner.data.db.YearPlannerDatabase.getInstance(this)
        repository = TaskRepository(db)

        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your task...")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onResults(results: Bundle) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let { processVoiceInput(it) }
                finish()
            }
            override fun onError(error: Int) {
                Log.e("VoiceInput", "Speech error: $error")
                finish()
            }
            override fun onReadyForSpeech(params: Bundle) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle) {}
            override fun onEvent(eventType: Int, params: Bundle) {}
        })

        setContent {
            Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    VoiceInputScreen(onDismiss = { finish() })
                }
            }
        }

        speechRecognizer.startListening(speechIntent)
    }

    override fun onDestroy() {
        if (::speechRecognizer.isInitialized) {
            speechRecognizer.destroy()
        }
        super.onDestroy()
    }

    private fun processVoiceInput(text: String) {
        val parsed = parseNaturalLanguage(text)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = Task(
                    title = parsed.title,
                    notes = "Created via voice: $text",
                    projectId = parsed.projectId,
                    priority = parsed.priority
                )
                repository?.insertTask(task)
                
                val zone = ZoneId.systemDefault()
                val plannedMillis = parsed.plannedDate?.atZone(zone)?.toInstant()?.toEpochMilli()
                    ?: LocalDate.now().atStartOfDay(zone).toInstant().toEpochMilli()
                
                val occurrence = TaskOccurrence(
                    taskId = task.id,
                    planningPrecision = com.yearplanner.data.model.PlanningPrecision.DAY,
                    plannedDate = plannedMillis,
                    deadlineDate = parsed.deadlineDate?.atZone(zone)?.toInstant()?.toEpochMilli(),
                    zoneId = zone.id
                )
                repository?.insertOccurrence(occurrence)
            } catch (e: Exception) {
                Log.e("VoiceInput", "Failed to save voice task", e)
            }
        }
    }

    private data class ParsedTask(
        val title: String,
        val projectId: String? = null,
        val priority: Int = 0,
        val plannedDate: LocalDateTime? = null,
        val deadlineDate: LocalDateTime? = null
    )

    private fun parseNaturalLanguage(input: String): ParsedTask {
        val lower = input.lowercase(Locale.getDefault())
        var title = input
        var projectId: String? = null
        var priority = 0
        var plannedDate: LocalDateTime? = null
        var deadlineDate: LocalDateTime? = null
        val now = LocalDateTime.now()
        
        // Extract project (#work, #personal, etc.)
        val projectPattern = Pattern.compile("#(\\w+)")
        val projectMatcher = projectPattern.matcher(lower)
        if (projectMatcher.find()) {
            val projectName = projectMatcher.group(1)
            projectId = projectName
            title = title.replace("#$projectName", "").trim()
        }
        
        // Extract priority (!! = high, ! = medium)
        if (lower.contains("!!") || lower.contains("urgent") || lower.contains("important")) {
            priority = 3
            title = title.replace("!!", "").replace("urgent", "").replace("important", "").trim()
        } else if (lower.contains("!")) {
            priority = 2
            title = title.replace("!", "").trim()
        }
        
        // Extract time references
        val timePatterns = listOf(
            "tomorrow" to now.plusDays(1).withHour(9).withMinute(0),
            "today" to now.withHour(9).withMinute(0),
            "next week" to now.plusWeeks(1).with(java.time.temporal.TemporalAdjusters.next(java.time.DayOfWeek.MONDAY)).withHour(9).withMinute(0),
            "monday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.MONDAY)).withHour(9).withMinute(0),
            "tuesday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.TUESDAY)).withHour(9).withMinute(0),
            "wednesday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.WEDNESDAY)).withHour(9).withMinute(0),
            "thursday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.THURSDAY)).withHour(9).withMinute(0),
            "friday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.FRIDAY)).withHour(9).withMinute(0),
            "saturday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SATURDAY)).withHour(9).withMinute(0),
            "sunday" to now.with(java.time.temporal.TemporalAdjusters.nextOrSame(java.time.DayOfWeek.SUNDAY)).withHour(9).withMinute(0),
        )
        
        for ((keyword, dateTime) in timePatterns) {
            if (lower.contains(keyword)) {
                plannedDate = dateTime
                deadlineDate = dateTime
                title = title.replace(keyword, "", true).trim()
                break
            }
        }
        
        // Extract specific time (e.g., "at 2pm", "at 14:30")
        val timePattern = Pattern.compile("at\\s+(\\d{1,2}):?(\\d{2})?\\s*(am|pm)?")
        val timeMatcher = timePattern.matcher(lower)
        if (timeMatcher.find()) {
            var hour = timeMatcher.group(1)?.toIntOrNull() ?: 9
            val minute = timeMatcher.group(2)?.toIntOrNull() ?: 0
            val ampm = timeMatcher.group(3)
            
            if (ampm != null) {
                if (ampm == "pm" && hour != 12) hour += 12
                else if (ampm == "am" && hour == 12) hour = 0
            }
            
            plannedDate = plannedDate?.withHour(hour)?.withMinute(minute) 
                ?: now.withHour(hour).withMinute(minute)
            deadlineDate = plannedDate
        }
        
        return ParsedTask(title = title.trim(), projectId = projectId, priority = priority, plannedDate = plannedDate, deadlineDate = deadlineDate)
    }
}

@Composable
fun VoiceInputScreen(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Listening...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Speak naturally, e.g.: \"Call John tomorrow at 2pm #work !!\"",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
