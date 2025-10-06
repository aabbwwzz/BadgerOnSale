package com.cs407.choosepet.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cs407.choosepet.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Composable
fun CardView() {
    var leftImage by remember { mutableStateOf(R.drawable.cat_crying) }
    var rightImage by remember { mutableStateOf(R.drawable.dog) }
    val scope = rememberCoroutineScope()
    var leftCountdown by remember { mutableStateOf<Int?>(null) }
    var rightCountdown by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Choose Your Pet",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {

            ImageCard(
                leftImage,
                countdown = leftCountdown,
                onClick = {
                    scope.launch {
                    launch {
                        for (i in 3 downTo 1) {
                            leftCountdown = i
                            delay(1000)
                        }
                        leftCountdown = null
                    }
                }},
                modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(16.dp))
            ImageCard(
                rightImage,
                countdown = rightCountdown,
                onClick = {
                    scope.launch {
                        launch {
                            for (i in 3 downTo 1) {
                                rightCountdown = i
                                delay(1000)
                            }
                            rightCountdown = null
                        }
                    }
                },

                modifier = Modifier.weight(1f),
            )

        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp)
        ) {
            Text("Choose Again")
        }
    }
}