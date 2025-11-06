package com.cs407.badgerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

data class ItemForSale(val name: String, val price: String, val imageRes: Int)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // --- app-level state (hardcoded defaults) ---
            var profileImage by remember { mutableStateOf(R.drawable.avatar) }
            var name by remember { mutableStateOf("Zhongzheng Zhou") }
            var email by remember { mutableStateOf("zzhou@example.com") }
            var address by remember { mutableStateOf("123 University Ave, Madison, WI") }
            var phone by remember { mutableStateOf("6085551212") }
            var gradYear by remember { mutableStateOf("2027") }

            val items = listOf(
                ItemForSale("TV", "$300", R.drawable.tv),
                ItemForSale("Apple", "$2", R.drawable.apple),
                ItemForSale("Earphone", "$50", R.drawable.earphone)
            )

            val nav = rememberNavController()

            NavHost(navController = nav, startDestination = "profile") {
                composable("profile") {
                    ProfileSimpleScreen(
                        profileImage = profileImage,
                        onProfilePicTap = {
                            // cycle through a few local images
                            val pics = listOf(R.drawable.avatar, R.drawable.tv, R.drawable.apple, R.drawable.earphone)
                            val idx = pics.indexOf(profileImage).coerceAtLeast(0)
                            profileImage = pics[(idx + 1) % pics.size]
                        },
                        name = name,
                        email = email,
                        items = items,
                        onEditClick = { nav.navigate("edit") }
                    )
                }
                composable("edit") {
                    EditProfileScreen(
                        // current values
                        currentImage = profileImage,
                        currentName = name,
                        currentEmail = email,
                        currentAddress = address,
                        currentPhone = phone,
                        currentGradYear = gradYear,
                        // actions
                        onCancel = { nav.popBackStack() },
                        onSave = { newImage, newName, newEmail, newAddress, newPhone, newGradYear ->
                            profileImage = newImage
                            name = newName
                            email = newEmail
                            address = newAddress
                            phone = newPhone
                            gradYear = newGradYear
                            nav.popBackStack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileSimpleScreen(
    profileImage: Int,
    onProfilePicTap: () -> Unit,
    name: String,
    email: String,
    items: List<ItemForSale>,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Round profile picture
        Image(
            painter = painterResource(profileImage),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .clickable { onProfilePicTap() }
        )
        Spacer(Modifier.height(12.dp))
        Text(name, style = MaterialTheme.typography.titleMedium)
        Text(email, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onEditClick) { Text("Edit Profile") }

        Spacer(Modifier.height(16.dp))
        Text("Items for Sale", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        // item cards with images “on the box”
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(items) { item ->
                ItemCard(item)
            }
        }
    }
}

@Composable
fun ItemCard(item: ItemForSale) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(item.name, fontWeight = FontWeight.SemiBold)
                Text(item.price)
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    currentImage: Int,
    currentName: String,
    currentEmail: String,
    currentAddress: String,
    currentPhone: String,
    currentGradYear: String,
    onCancel: () -> Unit,
    onSave: (newImage: Int, newName: String, newEmail: String, newAddress: String, newPhone: String, newGradYear: String) -> Unit
) {
    var selectedImage by remember { mutableStateOf(currentImage) }
    var name by remember { mutableStateOf(currentName) }
    var email by remember { mutableStateOf(currentEmail) }
    var address by remember { mutableStateOf(currentAddress) }
    var phone by remember { mutableStateOf(currentPhone) }
    var gradYear by remember { mutableStateOf(currentGradYear) }

    val imageOptions = listOf(R.drawable.avatar, R.drawable.tv, R.drawable.apple, R.drawable.earphone)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // tap to cycle profile picture
        Image(
            painter = painterResource(id = selectedImage),
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .clickable {
                    val idx = imageOptions.indexOf(selectedImage).coerceAtLeast(0)
                    selectedImage = imageOptions[(idx + 1) % imageOptions.size]
                }
        )
        Text("Tap picture to change", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))

        // the “box” with editable fields
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = address, onValueChange = { address = it },
                    label = { Text("Address") }, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phone, onValueChange = { phone = it.filter(Char::isDigit) },
                    label = { Text("Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = gradYear, onValueChange = { gradYear = it.filter(Char::isDigit).take(4) },
                    label = { Text("Graduation Year") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Cancel") }
            Button(
                onClick = { onSave(selectedImage, name, email, address, phone, gradYear) },
                modifier = Modifier.weight(1f)
            ) { Text("Save") }
        }
    }
}