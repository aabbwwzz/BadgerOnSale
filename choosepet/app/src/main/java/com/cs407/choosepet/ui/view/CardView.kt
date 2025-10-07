package com.cs407.choosepet.ui.view

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch


@Composable
fun CardView() {
    var leftImage by remember { mutableStateOf(R.drawable.who_am_i) }
    var rightImage by remember { mutableStateOf(R.drawable.who_am_i) }
    val scope = rememberCoroutineScope()
    var leftCountdown by remember { mutableStateOf<Int?>(null) }
    var rightCountdown by remember { mutableStateOf<Int?>(null) }
    var leftAlphaTarget by remember { mutableStateOf(1f) }
    var rightAlphaTarget by remember { mutableStateOf(1f) }
    val leftAlpha by animateFloatAsState(targetValue = leftAlphaTarget, animationSpec = tween(4000),
        label = "cat disappear"
    )
    val rightAlpha by animateFloatAsState(targetValue = rightAlphaTarget, animationSpec = tween(4000),
        label = "dog disappear"
    )
    var currentJob: Job? by remember { mutableStateOf<Job?>(null) }
    var leftImageUrl by remember { mutableStateOf<String?>(null) }
    var rightImageUrl by remember { mutableStateOf<String?>(null) }
    val catUrl = "https://media.istockphoto.com/id/1443562748/photo/cute-ginger-cat.jpg?s=1024x1024"
    val catCryingUrl = "https://media.istockphoto.com/id/1303791905/photo/crying-cat.jpg?s=1024"
    val dogUrl = "https://media.istockphoto.com/id/1482199015/photo/happy-puppy-welsh-corgi-14-"
    val dogCryingUrl = "https://cdn.pixabay.com/photo/2025/04/14/22/55/dog-9534362_1280.jpg"
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

                    currentJob?.cancel()
                    currentJob = scope.launch {
                        val jobA = launch {
                            for (i in 3 downTo 1) {
                                leftCountdown = i
                                delay(1000)
                            }
                            leftImage = R.drawable.cat
                            leftImageUrl = catUrl
                            leftCountdown = null
                        }
                        val jobB = launch {
                            rightImage = R.drawable.dog_crying
                            rightImageUrl = dogCryingUrl
                            rightAlphaTarget = 0f
                        }
                        joinAll(jobA, jobB)
                }},
                modifier = Modifier.weight(1f),
                enabled = (leftImage == R.drawable.who_am_i && rightImage == R.drawable.who_am_i),
                alpha = leftAlpha
            )
            Spacer(modifier = Modifier.width(16.dp))
            ImageCard(
                rightImage,
                countdown = rightCountdown,
                onClick = {
                    rightAlphaTarget = 1f
                    leftAlphaTarget = 0f
                    currentJob?.cancel()
                    currentJob = scope.launch {
                        val jobA = launch {
                            for (i in 3 downTo 1) {
                                rightCountdown = i
                                delay(1000)
                            }
                            rightImage = R.drawable.dog
                            rightImageUrl = dogUrl
                            rightCountdown = null
                        }
                        val jobB = launch {
                            leftImage = R.drawable.cat_crying
                            leftImageUrl = catCryingUrl
                            leftAlphaTarget = 0f
                        }
                        joinAll(jobA, jobB)
                    }
                },

                modifier = Modifier.weight(1f),
                enabled = (leftImage == R.drawable.who_am_i && rightImage == R.drawable.who_am_i),
                alpha = rightAlpha
            )

        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                currentJob?.cancel()
                leftImage = R.drawable.who_am_i
                rightImage = R.drawable.who_am_i
                leftCountdown = null
                rightCountdown = null
                leftAlphaTarget = 1f
                rightAlphaTarget = 1f
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