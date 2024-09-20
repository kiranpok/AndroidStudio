package com.example.lotto

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.lotto.ui.theme.LottoTheme
import androidx.compose.foundation.lazy.grid.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LottoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting1(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    // Use state to make the list reactive
    val numbers = remember { mutableStateOf((1..100).toMutableList()) }

    LazyColumn {
        // Observe the current value of the state (numbers.value)
        items(numbers.value) { n ->

            Log.d("XXX", "item $n")

            // Check if the number is odd or even
            val buttonColor = if (n % 2 == 0) ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            else ButtonDefaults.buttonColors(containerColor = Color.Cyan)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Padding around each item
                verticalAlignment = Alignment.CenterVertically // Align content vertically in the row
            ) {
                // Set the button color for odd/even items
                Button(
                    onClick = {
                        Log.d("XXX", "$n clicked")
                        numbers.value = numbers.value.toMutableList().apply {
                            remove(n)
                        }
                        Log.d("XXX", numbers.value.toString())
                    },
                    colors = buttonColor, // Apply different colors for odd/even
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Value is $n", fontSize = 20.sp)
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Greeting1(name: String, modifier: Modifier = Modifier) {
    // Use state to make the list reactive
    val numbers = remember { mutableStateOf((1..100).toMutableList()) }

    // Display items in a vertical grid (3 columns in this case)
    LazyVerticalGrid(
        columns = GridCells.Fixed(4), // Fixed number of columns
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(4.dp), // Padding around the grid
        horizontalArrangement = Arrangement.spacedBy(4.dp), // Horizontal spacing
        verticalArrangement = Arrangement.spacedBy(4.dp) // Vertical spacing
    ) {
        items(numbers.value) { n ->

            Log.d("XXX", "item $n")

            // Check if the number is odd or even
            val buttonColor = if (n % 2 == 0) ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            else ButtonDefaults.buttonColors(containerColor = Color.Cyan)

            // Create each item (button) in the grid
            Button(
                onClick = {
                    Log.d("XXX", "$n clicked")
                    numbers.value = numbers.value.toMutableList().apply {
                        remove(n) // Remove the clicked number
                    }
                    Log.d("XXX", numbers.value.toString())
                },
                colors = buttonColor, // Apply different colors for odd/even
                modifier = Modifier
                    .fillMaxWidth() // Make each button take full width of its grid cell
                    .aspectRatio(1f) // Make each button square (equal width and height)
            ) {
                Text("Value is $n", fontSize = 20.sp)
            }
        }
    }
}

