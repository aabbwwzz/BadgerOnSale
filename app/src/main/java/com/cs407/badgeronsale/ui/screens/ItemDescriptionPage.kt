package com.cs407.badgeronsale.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.cs407.badgeronsale.R

@Composable
fun ItemDescriptionPage(
    itemName: String = "Varsity Jacket",
    sellerName: String = "Karim Hakki",
    price: String = "$45",
    details: List<String> = listOf(
        "UW–Madison Red and White Varsity Jacket",
        "Lightweight Nylon Material",
        "Snap Button Front",
        "Perfect for Game Days and Campus Events"
    ),
    imageRes: Int? = R.drawable.favorite_jacket,
    imageUrl: String? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: () -> Unit = {},
    onMessageClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // back arrow
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.Black,
            modifier = Modifier
                .size(28.dp)
                .clickable { onBackClick() }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // product image
        when {
            imageUrl != null -> {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(imageUrl)
                            .build()
                    ),
                    contentDescription = itemName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Crop
                )
            }
            imageRes != null -> {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = itemName,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .align(Alignment.CenterHorizontally),
                    contentScale = ContentScale.Fit
                )
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .background(Color(0xFFE0E0E0), shape = RoundedCornerShape(20.dp))
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No Image", color = Color.Gray, fontSize = 16.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // info card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(30.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = itemName,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Listing by: $sellerName",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )

            Text(
                text = "Price: $price",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Details:",
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE8E8E8), shape = RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                details.forEach { detail ->
                    Text(
                        text = detail,
                        fontSize = 15.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // favorite + chat icons
            Row(
                horizontalArrangement = Arrangement.spacedBy(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                val heartIcon =
                    if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
                val heartTint =
                    if (isFavorite) Color(0xFFC5050C) else Color.Black

                Icon(
                    imageVector = heartIcon,
                    contentDescription = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    tint = heartTint,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onFavoriteClick() }
                )

                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Message Seller",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onMessageClick() }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewItemDescriptionPage() {
    ItemDescriptionPage()
}
