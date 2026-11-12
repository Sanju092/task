package com.yearplanner.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import com.yearplanner.ui.theme.Theme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class QuickAddActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make activity appear as a dialog
        window.setBackgroundDrawableResource(android.R.color.transparent)
        
        setContent {
            val themeMode by viewModel.themeMode
            Theme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    QuickAddDialogContent(
                        onDismiss = { finish() },
                        onSave = { title, notes ->
                            viewModel.createTask(title = title, notes = notes)
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun QuickAddDialogContent(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Add Task", fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                androidx.compose.material3.TextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                androidx.compose.material3.TextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (title.isNotBlank()) {
                        onSave(title, notes)
                    }
                },
                enabled = title.isNotBlank()
            ) { Text("Save") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}