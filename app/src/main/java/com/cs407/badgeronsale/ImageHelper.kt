package com.cs407.badgeronsale

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Decode base64 data URL to Bitmap
 */
suspend fun decodeBase64ToBitmap(dataUrl: String): Bitmap? {
    return try {
        withContext(Dispatchers.IO) {
            // Extract base64 string from data URL
            val base64String = if (dataUrl.contains(",")) {
                dataUrl.substringAfter(",")
            } else {
                dataUrl
            }
            
            // Decode base64 to byte array
            val imageBytes = Base64.decode(base64String, Base64.NO_WRAP)
            
            // Create bitmap from byte array
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        }
    } catch (e: Exception) {
        println("Error decoding base64 image: ${e.message}")
        e.printStackTrace()
        null
    }
}

/**
 * Composable to display image from base64 data URL
 * This is more reliable than Coil for data URLs
 */
@Composable
fun Base64Image(
    dataUrl: String?,
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = {}
) {
    var bitmap by remember(dataUrl) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(dataUrl) { mutableStateOf(true) }
    var hasError by remember(dataUrl) { mutableStateOf(false) }
    
    // Load bitmap when dataUrl changes
    androidx.compose.runtime.LaunchedEffect(dataUrl) {
        if (dataUrl.isNullOrEmpty()) {
            hasError = true
            isLoading = false
            return@LaunchedEffect
        }
        
        isLoading = true
        hasError = false
        
        try {
            val decoded = decodeBase64ToBitmap(dataUrl)
            if (decoded != null) {
                bitmap = decoded
                isLoading = false
                println("Successfully decoded base64 image: ${decoded.width}x${decoded.height}")
            } else {
                hasError = true
                isLoading = false
                println("Failed to decode base64 image")
            }
        } catch (e: Exception) {
            println("Exception decoding image: ${e.message}")
            e.printStackTrace()
            hasError = true
            isLoading = false
        }
    }
    
    Box(modifier = modifier) {
        when {
            isLoading -> {
                // Show loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            bitmap != null -> {
                // Show decoded image
                Image(
                    bitmap = bitmap!!.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            else -> {
                // Show placeholder or error
                placeholder()
            }
        }
    }
}

