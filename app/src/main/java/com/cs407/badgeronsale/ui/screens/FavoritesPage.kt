package com.cs407.badgeronsale.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs407.badgeronsale.R

@Composable
fun FavoritesPage(
    onBackClick: () -> Unit = {},
    onItemClick: (Int) -> Unit = {}
) {
    // remember the current list of favorites
    var favorites by remember {
        mutableStateOf(
            listOf(
                R.drawable.favorite_jacket,
                R.drawable.favorite_ticket
            )
        )
    }

    // track which item is about to be deleted
    var showDialog by remember { mutableStateOf(false) }
    var itemToRemove by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Favorites",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // Favorites Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(favorites) { item ->
                FavoriteItem(
                    imageRes = item,
                    onDeleteClick = {
                        itemToRemove = item
                        showDialog = true
                    },
                    onClick = { onItemClick(item) }
                )
            }
        }

        // Confirmation dialog
        if (showDialog && itemToRemove != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Remove from Favorites") },
                text = { Text("Are you sure you want to remove this item from favorites?") },
                confirmButton = {
                    TextButton(onClick = {
                        favorites = favorites.filterNot { it == itemToRemove }
                        showDialog = false
                    }) {
                        Text("Remove", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun FavoriteItem(
    imageRes: Int,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFE8E8E8), shape = RoundedCornerShape(24.dp))
            .padding(12.dp)
            .clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Favorite item",
            modifier = Modifier
                .size(140.dp)
                .padding(bottom = 8.dp),
            contentScale = ContentScale.Fit
        )
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = Color.Black,
            modifier = Modifier
                .size(28.dp)
                .clickable { onDeleteClick() }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewFavoritesPage() {
    FavoritesPage()
}
