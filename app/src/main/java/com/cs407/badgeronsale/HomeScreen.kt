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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import com.cs407.badgeronsale.repository.ListingRepository
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cs407.badgeronsale.Base64Image

enum class Category { TICKETS, FURNITURE, DEVICES, OTHER }

data class Listing(
    val id: String,
    val title: String,
    val price: String,
    val distance: String,
    val timeAgo: String,
    val imageRes: Int? = null,  // For local drawable resources
    val category: Category,
    val imageUrl: String? = null,  // For Firebase Storage URLs
    val sellerId: String? = null,
    val sellerName: String? = null,
    val description: String = "",
    val createdAt: java.util.Date? = null
)

// Fallback mock listings (used if Firestore is empty or fails)
private val mockListings = listOf(
    Listing("1","Ticket", "$60","0.2 mi","2 day ago",  R.drawable.simple_ticket,  Category.TICKETS),
    Listing("2","Jacket", "$75","0.1 mi","1 hour ago", R.drawable.simple_jacket,  Category.OTHER),
    Listing("3","Table",  "$35","0.1 mi","1 day ago",  R.drawable.simple_backpack, Category.FURNITURE),
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
    var listings by remember { mutableStateOf<List<Listing>>(mockListings) }
    var isLoading by remember { mutableStateOf(true) }

    // Load listings from Firestore
    LaunchedEffect(Unit) {
        ListingRepository.getAllListings().collectLatest { firestoreListings ->
            listings = if (firestoreListings.isEmpty()) {
                // Fallback to mock data if Firestore is empty
                mockListings
            } else {
                firestoreListings
            }
            isLoading = false
        }
    }

    // Filter listings based on category and search query
    val filteredListings by remember(selected, query, listings) {
        derivedStateOf {
            val base = when (selected) {
                "Tickets"   -> listings.filter { it.category == Category.TICKETS }
                "Furniture" -> listings.filter { it.category == Category.FURNITURE }
                "Devices"   -> listings.filter { it.category == Category.DEVICES }
                else        -> listings
            }
            if (query.isBlank()) base else base.filter { it.title.contains(query, true) }
        }
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF6F6F6))
    ) {
        // Top Navigation Bar: Menu (left), Search (center), Messages (right)
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hamburger menu icon on the left
            IconButton(onClick = onMenuClick) { 
                Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = Color.Black) 
            }
            
            // Search bar in the center
            Spacer(Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; onSearch(it) },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.weight(1f).height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )
            
            // Message icon on the right
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onMessagesClick) { 
                Icon(Icons.Outlined.Chat, contentDescription = "Messages", tint = Color.Black) 
            }
        }

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
    val context = LocalContext.current
    
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 220.dp)
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            // Show image from URL, drawable resource, or placeholder
            when {
                !item.imageUrl.isNullOrEmpty() -> {
                    // Use Base64Image helper for more reliable data URL loading
                    Base64Image(
                        dataUrl = item.imageUrl,
                        contentDescription = item.title,
                        modifier = Modifier
                            .height(110.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = {
                            // Show placeholder while loading or on error
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.avatar),
                                    contentDescription = "Loading...",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                    )
                }
                item.imageRes != null -> {
                    // Load image from drawable resource
                    Image(
                        painter = painterResource(item.imageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.height(110.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    )
                }
                else -> {
                    // Placeholder when no image is available
                    Box(
                        modifier = Modifier
                            .height(110.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE0E0E0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Text(item.price, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text(item.distance, color = Color(0xFF555555), fontSize = 14.sp)
            Text(item.timeAgo, color = Color(0xFF555555), fontSize = 14.sp)
        }
    }
}
