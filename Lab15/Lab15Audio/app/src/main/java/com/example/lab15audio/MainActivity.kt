package com.example.lab15audio.kt

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.lab15audio.ui.theme.Lab15AudioTheme
import com.example.lab15audio.AudioRecorder

class MainActivity : ComponentActivity() {
    private lateinit var audioRecorder: AudioRecorder
    private var recRunning by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize AudioRecorder
        audioRecorder = AudioRecorder(this)

        // Set content view with composables
        setContent {
            Lab15AudioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(this, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    fun startRecording() {
        audioRecorder.startRecording()
        recRunning = true
    }

    fun stopRecording() {
        audioRecorder.stopRecording()
        recRunning = false
    }

    fun playRecording() {
        audioRecorder.playRecording()
    }

    fun hasPermissions() = audioRecorder.hasPermissions()
}

@Composable
fun MainScreen(activity: MainActivity, modifier: Modifier = Modifier) {
    var isRecording by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (!isRecording) {
                    isRecording = true
                    activity.startRecording()
                } else {
                    activity.stopRecording()
                    isRecording = false
                }
            }
        ) {
            Text(text = if (isRecording) "Stop recording" else "Start recording")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (!isPlaying) {
                    isPlaying = true
                    activity.playRecording()
                    isPlaying = false
                }
            }
        ) {
            Text(text = "Play Recording")
        }
    }

    // Request permissions at runtime
    LaunchedEffect(Unit) {
        if (!activity.hasPermissions()) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
        }
    }
}
