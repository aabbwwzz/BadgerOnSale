package com.cs407.badgeronsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ChatDetailScreen(
    dmId: String,
    onBack: () -> Unit = {}
) {
    // Pull the unique person & thread
    val dm = remember(dmId) { MockData.getDmById(dmId) ?: MockData.dms.first() }
    var messages by remember(dmId) { mutableStateOf(MockData.getThread(dmId)) }

    val product = remember(dmId) { MockData.productByDm[dmId] } // Pair(imageRes, title) or null
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                    Text(dm.name, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(start = 4.dp))
                }
            }
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().background(Color.White).padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    placeholder = { Text("Message...") },
                    singleLine = true,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val trimmed = input.trim()
                        if (trimmed.isNotEmpty()) {
                            messages = messages + ChatMessage("local-${messages.size}", true, trimmed)
                            input = ""
                        }
                    },
                    shape = RoundedCornerShape(26.dp)
                ) { Text("Send") }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F2)).padding(padding),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Optional product card like your mock
            product?.let { (imageRes, title) ->
                item("product") {
                    Surface(
                        color = Color(0xFFD8DBFF),
                        shape = RoundedCornerShape(28.dp),
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                painter = painterResource(imageRes),
                                contentDescription = title,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.height(110.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Unique conversation bubbles
            items(messages, key = { it.id }) { msg ->
                if (msg.fromMe) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(
                            color = Color(0xFFBFC8FF),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 320.dp)
                        ) { Text(msg.text, modifier = Modifier.padding(12.dp)) }
                    }
                } else {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Image(
                            painter = painterResource(dm.avatarRes),
                            contentDescription = "${dm.name} avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(34.dp).clip(CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(22.dp),
                            tonalElevation = 2.dp,
                            modifier = Modifier.widthIn(max = 320.dp)
                        ) { Text(msg.text, modifier = Modifier.padding(12.dp), color = Color(0xFF222222)) }
                    }
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}
