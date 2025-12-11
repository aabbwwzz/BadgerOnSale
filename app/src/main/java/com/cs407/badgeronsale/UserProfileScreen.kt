package com.cs407.badgeronsale

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Home
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cs407.badgeronsale.repository.ListingRepository
import com.cs407.badgeronsale.FirebaseAuthHelper
import com.cs407.badgeronsale.Base64Image
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// UW-Madison red
private val BadgerRed = Color(0xFFC5050C)

data class UserListing(
    val id: String,
    val title: String,
    val price: String = "",  // Price for seller profile view
    @DrawableRes val imageRes: Int? = null,  // Optional drawable resource
    val imageUrl: String? = null  // Optional Firebase Storage URL
)

@Composable
fun UserProfileScreen(
    userName: String = "Jouhara Ali",
    @DrawableRes avatarRes: Int = R.drawable.avatar,
    profilePicUrl: String? = null,  // Firebase Storage URL for profile picture
    isOwnProfile: Boolean = true,  // True if viewing own profile, false if viewing seller profile
    rating: Double = 5.0,  // Seller rating (for seller profile view)
    userId: String? = null,  // User ID to load listings for (if null, uses current user)
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onEditAccount: () -> Unit = {},
    onListingDeleted: (UserListing) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Load real listings from Firestore in real-time
    var listings by remember { mutableStateOf<List<UserListing>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Determine which user's listings to load
    val targetUserId = userId ?: FirebaseAuthHelper.getCurrentUser()?.uid
    
    // Profile picture URL - use passed parameter for own profile, fetch for seller profile
    var fetchedSellerProfilePicUrl by remember { mutableStateOf<String?>(null) }
    
    // For own profile, always use the passed profilePicUrl (which updates when profile is edited)
    // For seller profile, fetch from Firestore
    val displayProfilePicUrl = if (isOwnProfile) profilePicUrl else (fetchedSellerProfilePicUrl ?: profilePicUrl)
    
    // Fetch seller's profile picture if viewing seller profile
    LaunchedEffect(userId, isOwnProfile) {
        if (userId != null && !isOwnProfile) {
            coroutineScope.launch {
                val profileResult = FirebaseAuthHelper.getUserProfile(userId)
                if (profileResult.isSuccess) {
                    val profileData = profileResult.getOrNull()!!
                    fetchedSellerProfilePicUrl = (profileData["ProfilePicURL"] as? String)?.takeIf { it.isNotEmpty() }
                }
            }
        }
    }
    
    // Load listings in real-time
    LaunchedEffect(targetUserId) {
        if (targetUserId != null) {
            try {
                ListingRepository.getListingsByUserIDRealtime(targetUserId).collectLatest { listingList ->
                    // Convert Listing to UserListing format
                    listings = listingList.map { listing ->
                        UserListing(
                            id = listing.id,
                            title = listing.title,
                            price = listing.price,
                            imageRes = listing.imageRes,
                            imageUrl = listing.imageUrl
                        )
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                println("Error loading user listings: ${e.message}")
                isLoading = false
            }
        } else {
            listings = emptyList()
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onHome) { Icon(Icons.Outlined.Home, contentDescription = "Home") }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header card
            item {
                ElevatedCard(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Display profile picture from Firebase Storage or fallback to drawable
                        if (displayProfilePicUrl != null) {
                            // Use Base64Image for more reliable data URL loading
                            Base64Image(
                                dataUrl = displayProfilePicUrl,
                                contentDescription = "Profile picture",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = {
                                    Image(
                                        painter = painterResource(avatarRes),
                                        contentDescription = "Profile picture placeholder",
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            )
                        } else {
                            Image(
                                painter = painterResource(avatarRes),
                                contentDescription = "Profile picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = userName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = if (isOwnProfile) "Your Profile" else "Seller Profile",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )

                        Spacer(Modifier.height(12.dp))

                        // Listings count (matching wireframe - only listings, no ratings for own profile)
                        if (isOwnProfile) {
                            // For own profile: just show listings count (matching wireframe)
                            Text(
                                text = listings.size.toString(),
                                fontWeight = FontWeight.Black,
                                fontSize = 28.sp
                            )
                            Text(
                                text = "listings",
                                color = Color(0xFF666666),
                                fontSize = 14.sp
                            )
                        } else {
                            // For seller profile: show ratings and listings side by side
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Ratings (left side)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = rating.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    )
                                    Text(
                                        text = "RATINGS",
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp
                                    )
                                }
                                
                                // Listings count (right side)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = listings.size.toString(),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 28.sp
                                    )
                                    Text(
                                        text = "listings",
                                        color = Color(0xFF666666),
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Loading indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            
            // Listing rows - load actual listing data to get imageUrl
            items(listings, key = { it.id }) { item ->
                var listingData by remember(item.id) { mutableStateOf<com.cs407.badgeronsale.Listing?>(null) }
                
                // Load full listing data to get imageUrl
                LaunchedEffect(item.id) {
                    coroutineScope.launch {
                        listingData = ListingRepository.getListingById(item.id)
                    }
                }
                
                ListingRow(
                    listing = item,
                    showPrice = !isOwnProfile,  // Show price only for seller profile
                    showDelete = isOwnProfile,  // Only show delete for own listings
                    imageUrl = item.imageUrl ?: listingData?.imageUrl,
                    onDelete = {
                        if (isOwnProfile) {
                            // Delete from Firestore
                            val currentUser = FirebaseAuthHelper.getCurrentUser()
                            if (currentUser != null) {
                                coroutineScope.launch {
                                    val result = ListingRepository.deleteListing(item.id, currentUser.uid)
                                    if (result.isSuccess) {
                                        onListingDeleted(item)
                                    } else {
                                        println("Failed to delete listing: ${result.exceptionOrNull()?.message}")
                                    }
                                }
                            }
                        } else {
                            // For seller profiles, just call the callback
                            onListingDeleted(item)
                        }
                    }
                )
            }

            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ListingRow(
    listing: UserListing,
    onDelete: () -> Unit,
    showPrice: Boolean = false,  // Only show price for seller profile view
    showDelete: Boolean = true,  // Only show delete icon for own listings
    imageUrl: String? = null  // Firebase Storage image URL
) {
    val context = LocalContext.current
    
    Surface(
        color = Color(0xFFE9E9E9),  // Light grey background matching wireframe
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image on the left - support both drawable and Firebase Storage images
            when {
                !imageUrl.isNullOrEmpty() -> {
                    // Use Base64Image for data URLs
                    Base64Image(
                        dataUrl = imageUrl,
                        contentDescription = listing.title,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        placeholder = {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("...", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    )
                }
                listing.imageRes != null -> {
                    Image(
                        painter = painterResource(listing.imageRes),
                        contentDescription = listing.title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                else -> {
                    // Placeholder
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFD0D0D0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No Image", fontSize = 8.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.width(14.dp))

            // Title in the middle
            Text(
                text = listing.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222222),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Price (only for seller profile view, matching wireframe)
            if (showPrice && listing.price.isNotEmpty()) {
                Text(
                    text = listing.price,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            // Trash icon on the right (only for own listings)
            if (showDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onDelete() }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    UserProfileScreen()
}
