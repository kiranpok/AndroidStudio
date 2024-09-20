package com.example.lab10



import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.lab10.ui.theme.Lab10Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Lab10Theme() {
                // Call the GameScreen composable function
                GameScreen()
            }
        }
    }
}

@Composable
fun GameScreen() {
    val game = remember { NumberGame(1..10) }
    var guess by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<GuessResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Hello! Guess a number in 1..10", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = guess,
            onValueChange = { guess = it },
            label = { Text("Your guess") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userGuess = guess.toIntOrNull()
                if (userGuess != null) {
                    result = game.makeGuess(userGuess)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Make guess!")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (result) {
                GuessResult.HIT -> "Congratulations! You guessed right!"
                GuessResult.HIGH -> "HIGH [${game.range.first}, ${game.range.last}]"
                GuessResult.LOW -> "LOW [${game.range.first}, ${game.range.last}]"
                else -> ""
            },
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
