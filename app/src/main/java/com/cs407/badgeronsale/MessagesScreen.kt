package com.cs407.badgeronsale

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DMPreview(
    val id: String,
    val name: String,
    @DrawableRes val avatarRes: Int
)

data class ChatMessage(val id: String, val fromMe: Boolean, val text: String)

object MockData {
    // People
    val dms = listOf(
        DMPreview("1","Mouna Kacem",        R.drawable.avatar_mouna),
        DMPreview("2","Summer Atari",       R.drawable.avatar_summer),
        DMPreview("3","Karim Hakki",        R.drawable.avatar_karim),
        DMPreview("4","Abdi",               R.drawable.avatar_abdi),
        DMPreview("5","Jouhara Ali",        R.drawable.avatar_jouhara),
        DMPreview("6","ZhongZhengzhou46",   R.drawable.avatar_zhong),
    )

    // Each DM gets its OWN unique messages
    val threads: Map<String, List<ChatMessage>> = mapOf(
        "1" to listOf(
            ChatMessage("m1", true,  "Salam Mouna, I'm interested in your AirPods. Would you take \$30?"),
            ChatMessage("m2", false, "Salam, yes I can do \$30. When can you pick them up?")
        ),
        "2" to listOf(
            ChatMessage("m1", true,  "Hey Summer! Is the sun lamp still available?"),
            ChatMessage("m2", false, "Hi! Yep, still available. I can meet near Union South."),
            ChatMessage("m3", true,  "Perfect. Does 5pm work for you?")
        ),
        "3" to listOf(
            ChatMessage("m1", false, "Hi, the ticket price is a bit high. Can you lower it?"),
            ChatMessage("m2", true,  "I can drop \$5 if you can pick it up today."),
            ChatMessage("m3", false, "Deal. I’ll be there at 6.")
        ),
        "4" to listOf(
            ChatMessage("m1", true,  "Asalaamu Alaikum Abdi! I want the couch—does it come with delivery?"),
            ChatMessage("m2", false, "Wa’alaikum salaam! I can help load it, but no delivery."),
            ChatMessage("m3", true,  "No problem. I’ll bring a friend and a truck.")
        ),
        "5" to listOf(
            ChatMessage("m1", false, "Could you discount the couch? The seams look worn."),
            ChatMessage("m2", true,  "I can do \$70 if you decide today."),
            ChatMessage("m3", false, "Sounds fair. I’ll confirm by evening.")
        ),
        "6" to listOf(
            ChatMessage("m1", true,  "Hi Zhong, when can I pick up the TV?"),
            ChatMessage("m2", false, "Tonight after 7 works great."),
            ChatMessage("m3", true,  "Awesome—see you then!")
        )
    )

    // Optional product hero per chat (for your header card)
    val productByDm: Map<String, Pair<Int, String>> = mapOf(
        "1" to (R.drawable.simple_earbods to "Mouna's Airpods")
    )

    fun getDmById(id: String): DMPreview? = dms.find { it.id == id }
    fun getThread(id: String): List<ChatMessage> = threads[id].orEmpty()
    fun getPreviewLine(id: String): String =
        getThread(id).lastOrNull()?.text ?: "Tap to open conversation"
}

@Composable
fun MessagesScreen(
    onHomeClick: () -> Unit = {},
    onOpenChat: (dmId: String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Messages", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onHomeClick) {
                    Icon(Icons.Outlined.Home, contentDescription = "Home")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF1F1F1)),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(MockData.dms) { dm ->
                val preview = MockData.getPreviewLine(dm.id)
                DMRow(dm, preview) { onOpenChat(dm.id) }
            }
        }
    }
}

@Composable
private fun DMRow(dm: DMPreview, preview: String, onClick: () -> Unit) {
    ElevatedCard(
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.padding(14.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(dm.avatarRes),
                contentDescription = "${dm.name} avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(54.dp).clip(CircleShape)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(dm.name, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
                    color = Color(0xFF222222), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(preview, fontSize = 14.sp, color = Color(0xFF666666),
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
