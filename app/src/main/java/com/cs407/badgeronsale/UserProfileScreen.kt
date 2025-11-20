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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// UW-Madison red
private val BadgerRed = Color(0xFFC5050C)

data class UserListing(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int
)

@Composable
fun UserProfileScreen(
    userName: String = "Jouhara Ali",
    @DrawableRes avatarRes: Int = R.drawable.avatar, // from Joe’s assets
    onBack: () -> Unit = {},
    onHome: () -> Unit = {},
    onEditAccount: () -> Unit = {},                 // <-- NEW callback for the button
    onListingDeleted: (UserListing) -> Unit = {}    // for future hookup
) {
    // Hardcoded sample listings (replace with real data later)
    var listings by remember {
        mutableStateOf(
            listOf(
                UserListing("1", "Airpods", R.drawable.earphone),
                UserListing("2", "Shoes",   R.drawable.apple), // placeholder art
                UserListing("3", "TV",      R.drawable.tv)
            )
        )
    }

    Scaffold(
        topBar = {
            Row(
                Modifier
                    .fillMaxWidth()
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
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(avatarRes),
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = userName,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp
                        )
                        Text(
                            text = "Your Profile",
                            color = Color(0xFF666666),
                            fontSize = 14.sp
                        )

                        // ---- NEW: Edit Account button ----
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onEditAccount,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BadgerRed,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Edit Account", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                        // ----------------------------------

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = listings.size.toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        )
                        Text(text = "listings", color = Color(0xFF666666))
                    }
                }
            }

            // Listing rows
            items(listings, key = { it.id }) { item ->
                ListingRow(
                    listing = item,
                    onDelete = {
                        listings = listings.filterNot { it.id == item.id }
                        onListingDeleted(item)
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
    onDelete: () -> Unit
) {
    Surface(
        color = Color(0xFFE9E9E9),
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
            Image(
                painter = painterResource(listing.imageRes),
                contentDescription = listing.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(Modifier.width(14.dp))

            Text(
                text = listing.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF222222),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

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

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun UserProfileScreenPreview() {
    UserProfileScreen()
}
