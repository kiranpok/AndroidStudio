package com.example.lab06


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

@Composable
fun MPInfo(mp: MP, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(data = mp.imageUrl).apply {
                    placeholder(R.drawable.placeholder_image)
                    transformations(CircleCropTransformation())
                }.build()
            ),
            contentDescription = "MP Image",
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Name: ${mp.name}", style = MaterialTheme.typography.titleLarge)
        Text(text = "Constituency: ${mp.constituency}", style = MaterialTheme.typography.bodyLarge)
        Text(text = "Party: ${mp.party}", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "Next MP", color = Color.White)
        }
    }
}

@Composable
fun MPInfoScreen(modifier: Modifier = Modifier) {
    var currentMP by remember { mutableStateOf(mpList.random()) }

    MPInfo(
        mp = currentMP,
        onClick = { currentMP = mpList.random() },
        modifier = modifier
    )
}