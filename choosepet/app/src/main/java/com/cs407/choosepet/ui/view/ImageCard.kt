package com.cs407.choosepet.ui.view


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
@Composable
fun ImageCard(
    imageRes: Int,
    countdown: Int?,
    onClick: ()-> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alpha: Float = 1f,
) {
    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(enabled = enabled) { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(200.dp).graphicsLayer(alpha = alpha),
            )
            if (countdown != null) {
                Text(
                    text = countdown.toString(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Red
                )
            }

        }
    }
}