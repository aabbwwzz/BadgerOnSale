package com.cs407.badgeronsale

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Chat
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

enum class Category { TICKETS, FURNITURE, DEVICES, OTHER }

data class Listing(
    val id: String,
    val title: String,
    val price: String,
    val distance: String,
    val timeAgo: String,
    val imageRes: Int,
    val category: Category
)

private val mockListings = listOf(
    Listing("1","Ticket", "$60","0.2 mi","2 day ago",  R.drawable.simple_ticket,  Category.TICKETS),
    Listing("2","Jacket", "$75","0.1 mi","1 hour ago", R.drawable.simple_jacket,  Category.OTHER),
    Listing("3","Table",  "$35","0.1 mi","1 day ago",  R.drawable.simple_backpack
        ,   Category.FURNITURE),
    Listing("4","Earbuds","$35","0.1 mi","2 hour ago", R.drawable.simple_earbods, Category.DEVICES),
    Listing("5","Sofa",   "$90","1.2 mi","4 days ago", R.drawable.simple_sofa,    Category.FURNITURE),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onMenuClick: () -> Unit = {},
    onMessagesClick: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onFilterChanged: (String) -> Unit = {},
    onListingClick: (Listing) -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    val chipLabels = listOf("All", "Tickets", "Furniture", "Devices")
    var selected by remember { mutableStateOf("All") }

    val filteredListings by remember(selected, query) {
        derivedStateOf {
            val base = when (selected) {
                "Tickets"   -> mockListings.filter { it.category == Category.TICKETS }
                "Furniture" -> mockListings.filter { it.category == Category.FURNITURE }
                "Devices"   -> mockListings.filter { it.category == Category.DEVICES }
                else        -> mockListings
            }
            if (query.isBlank()) base else base.filter { it.title.contains(query, true) }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF6F6F6))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, null) }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onMessagesClick) { Icon(Icons.Outlined.Chat, null) }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it; onSearch(it) },
            placeholder = { Text("Search") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth().height(56.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(Modifier.padding(horizontal = 12.dp)) {
            chipLabels.forEach { label ->
                FilterChip(
                    selected = selected == label,
                    onClick = { selected = label; onFilterChanged(label) },
                    label = { Text(label) },
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredListings) { item -> ListingCard(item) { onListingClick(item) } }
        }
    }
}

@Composable
private fun ListingCard(item: Listing, onClick: () -> Unit) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(item.imageRes),
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier.height(110.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
            )
            Spacer(Modifier.height(8.dp))
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(item.distance, color = Color(0xFF555555), fontSize = 14.sp)
            Text(item.timeAgo, color = Color(0xFF555555), fontSize = 14.sp)
        }
    }
}
