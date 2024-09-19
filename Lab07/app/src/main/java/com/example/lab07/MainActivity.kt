package com.example.lab07



import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.system.measureTimeMillis

const val N = 100

class Account {
    private var amount: Double = 0.0
    private val mutex = Mutex() // Use a Mutex to ensure thread-safety.

    // Synchronized deposit function
    suspend fun deposit(amount: Double) {
        mutex.withLock {
            val x = this.amount
            delay(1) // simulates processing time
            this.amount = x + amount
        }
    }

    fun saldo(): Double = amount
}

/* Approximate measurement of the given block's execution time */
fun withTimeMeasurement(title: String, isActive: Boolean = true, code: () -> Unit) {
    if (!isActive) return
    val time = measureTimeMillis { code() }
    Log.i("MSU", "operation in '$title' took ${time} ms")
}

data class Saldos(val saldo1: Double, val saldo2: Double)

fun bankProcess(account: Account): Saldos {
    var saldo1: Double = 0.0
    var saldo2: Double = 0.0

    /* Measure execution time of one deposit task */
    withTimeMeasurement("Single coroutine deposit $N times") {
        runBlocking {
            launch {
                for (i in 1..N) account.deposit(0.0)
            }
        }
        saldo1 = account.saldo()
    }

    /* Measure execution time of two simultaneous deposit tasks using coroutines */
    withTimeMeasurement("Two $N times deposit coroutines together", isActive = true) {
        runBlocking {
            launch {
                for (i in 1..N) account.deposit(1.0)
            }
            launch {
                for (i in 1..N) account.deposit(1.0)
            }
        }
        saldo2 = account.saldo()
    }

    return Saldos(saldo1, saldo2)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val results = bankProcess(Account())

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                ShowResults(saldo1 = results.saldo1, saldo2 = results.saldo2)
            }
        }
    }
}

@Composable
fun ShowResults(saldo1: Double, saldo2: Double) {
    Column {
        Text(text = "Saldo1: $saldo1")
        Text(text = "Saldo2: $saldo2")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ShowResults(saldo1 = 0.0, saldo2 = 0.0)
}
