package com.example.lab15audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.core.content.ContextCompat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*

class AudioRecorder(private val context: Context) {

    private val sampleRate = 44100
    private var isRecording = false
    private val filename = "audio_record.pcm"
    private val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!

    fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun startRecording() {
        if (!hasPermissions()) {
            Log.d("AudioRecorder", "Permissions not granted")
            return
        }
        isRecording = true

        CoroutineScope(Dispatchers.IO).launch {
            val file = File(storageDir, filename)
            val outputStream = FileOutputStream(file)
            val dataOutputStream = DataOutputStream(BufferedOutputStream(outputStream))

            val minBufferSize = AudioRecord.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
            )

            val audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                minBufferSize
            )

            val audioData = ByteArray(minBufferSize)
            audioRecord.startRecording()

            try {
                while (isRecording) {
                    val read = audioRecord.read(audioData, 0, audioData.size)
                    if (read > 0) {
                        dataOutputStream.write(audioData, 0, read)
                    }
                }
            } catch (e: IOException) {
                Log.e("AudioRecorder", "Recording failed: ${e.message}")
            } finally {
                audioRecord.stop()
                audioRecord.release()
                dataOutputStream.close()
            }
        }
    }

    fun stopRecording() {
        isRecording = false
    }

    fun playRecording() {
        CoroutineScope(Dispatchers.IO).launch {
            val file = File(storageDir, filename)
            val inputStream = FileInputStream(file)

            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT
            )

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufferSize)
                .build()

            val buffer = ByteArray(minBufferSize)
            audioTrack.play()

            try {
                var bytesRead = inputStream.read(buffer)
                while (bytesRead != -1) {
                    audioTrack.write(buffer, 0, bytesRead)
                    bytesRead = inputStream.read(buffer)
                }
            } catch (e: IOException) {
                Log.e("AudioRecorder", "Playback failed: ${e.message}")
            } finally {
                audioTrack.stop()
                audioTrack.release()
                inputStream.close()
            }
        }
    }
}
