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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.foundation.clickable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.cs407.badgeronsale.repository.MessagesRepository
import com.cs407.badgeronsale.repository.ListingRepository
import com.cs407.badgeronsale.FirebaseAuthHelper

@Composable
fun ChatDetailScreen(
    otherUserId: String,
    listingId: String? = null,
    onBack: () -> Unit = {},
    onViewSellerProfile: (String) -> Unit = {}
) {
    var messages by remember { mutableStateOf<List<com.cs407.badgeronsale.repository.Message>>(emptyList()) }
    var otherUserInfo by remember { mutableStateOf<Map<String, Any>?>(null) }
    var listingInfo by remember { mutableStateOf<com.cs407.badgeronsale.Listing?>(null) }
    var input by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Load other user info
    LaunchedEffect(otherUserId) {
        MessagesRepository.getUserInfo(otherUserId).onSuccess {
            otherUserInfo = it
        }
    }

    // Load listing info if listingId is provided
    LaunchedEffect(listingId) {
        if (listingId != null) {
            listingInfo = ListingRepository.getListingById(listingId)
        }
    }

    // Load messages
    LaunchedEffect(otherUserId, listingId) {
        MessagesRepository.getMessages(otherUserId, listingId).collectLatest { msgs ->
            messages = msgs
            isLoading = false
        }
    }

    val otherUserName = otherUserInfo?.get("Name") as? String ?: "Unknown User"
    val otherUserProfilePic = otherUserInfo?.get("ProfilePicURL") as? String

    Scaffold(
        topBar = {
            Surface(shadowElevation = 2.dp) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                    Text(
                        otherUserName, 
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .weight(1f)
                            .clickable { onViewSellerProfile(otherUserId) }
                    )
                    // Profile icon (clickable to view seller profile)
                    IconButton(onClick = { onViewSellerProfile(otherUserId) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "View Profile"
                        )
                    }
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
                        if (trimmed.isNotEmpty() && !isLoading) {
                            coroutineScope.launch {
                                val result = MessagesRepository.sendMessage(
                                    receiverID = otherUserId,
                                    messageText = trimmed,
                                    listingID = listingId
                                )
                                if (result.isSuccess) {
                                    input = ""
                                }
                            }
                        }
                    },
                    shape = RoundedCornerShape(26.dp),
                    enabled = !isLoading
                ) { Text("Send") }
            }
        }
        ) { padding ->
        if (isLoading && messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F2)).padding(padding),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Product card as first message (from seller) if listing is associated
                listingInfo?.let { listing ->
                    item("product") {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Profile picture for seller
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "$otherUserName avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            // Product card as message bubble (light purple)
                            Surface(
                                color = Color(0xFFD8DBFF), // Light purple
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) {
                                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    if (listing.imageRes != null) {
                                        Image(
                                            painter = painterResource(listing.imageRes),
                                            contentDescription = listing.title,
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier
                                                .height(100.dp)
                                                .width(120.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "${otherUserName}'s ${listing.title}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF222222)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }

                // Messages
                items(messages, key = { it.messageID }) { msg ->
                    val currentUserId = FirebaseAuthHelper.getCurrentUser()?.uid
                    val isFromMe = msg.senderID == currentUserId
                    
                    if (isFromMe) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Surface(
                                color = Color(0xFFBFC8FF),
                                shape = RoundedCornerShape(22.dp),
                                modifier = Modifier.padding(horizontal = 16.dp).widthIn(max = 320.dp)
                            ) { Text(msg.messageText, modifier = Modifier.padding(12.dp)) }
                        }
                    } else {
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // TODO: Load profile picture from URL if available
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "$otherUserName avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(34.dp).clip(CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(22.dp),
                                tonalElevation = 2.dp,
                                modifier = Modifier.widthIn(max = 320.dp)
                            ) { Text(msg.messageText, modifier = Modifier.padding(12.dp), color = Color(0xFF222222)) }
                        }
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }
    }
}
