package com.cs407.badgeronsale.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun MenuPage(
    onFavoritesClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {},
    onCreateListingClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onHomeClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top row: "Menu" + Home icon on the right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Menu",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.weight(1f))

            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = Color.Black,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onHomeClick() }
            )
        }

        // Menu buttons
        MenuButton(
            icon = Icons.Default.FavoriteBorder,
            text = "Favorites",
            onClick = onFavoritesClick
        )
        MenuButton(
            icon = Icons.Default.Edit,
            text = "Edit Profile",
            onClick = onEditProfileClick
        )
        MenuButton(
            icon = Icons.Default.Visibility,
            text = "View Profile",
            onClick = onViewProfileClick
        )
        MenuButton(
            icon = Icons.Default.Add,
            text = "Create Listing",
            onClick = onCreateListingClick
        )
        MenuButton(
            icon = Icons.Default.Logout,
            text = "Log Out",
            onClick = onLogoutClick
        )
    }
}

@Composable
fun MenuButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color(0xFFE8E8E8),
        shape = RoundedCornerShape(40.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .padding(vertical = 7.dp)
            .clickable { onClick() },
        shadowElevation = 2.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(26.dp)
                )

                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun PreviewMenuPage() {
    MenuPage()
}
