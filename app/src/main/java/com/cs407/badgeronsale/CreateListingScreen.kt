package com.cs407.badgeronsale

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import com.cs407.badgeronsale.repository.ListingRepository
import com.cs407.badgeronsale.Category
import com.cs407.badgeronsale.FirebaseAuthHelper
import java.util.Date

// Same color style as other screens
private val BadgerRed = Color(0xFFC5050C)
private val LightBackground = Color(0xFFF2F2F2)
private val InputGrey = Color(0xFFE3E1E1)

@Composable
fun CreateListingScreen(
    onBackClick: () -> Unit = {},           // back arrow
    onListingCreated: () -> Unit = {}       // call this when validation passes
) {
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }

    var titleError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var detailsError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            // Top row: back arrow + title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Create Listing",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.Black,
                    textAlign = TextAlign.Start
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add photo placeholder card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            imagePickerLauncher.launch("image/*")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(selectedImageUri)
                                    .build()
                            ),
                            contentDescription = "Selected image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "+",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.Black
                            )
                            Text(
                                text = "Add photo",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // White rounded card for form
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {

                    // Title
                    Text(
                        text = "Title",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            titleError = null
                            generalError = null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(24.dp)),
                        isError = titleError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    if (titleError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = titleError!!,
                            color = BadgerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Price
                    Text(
                        text = "Price",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = {
                            price = it
                            priceError = null
                            generalError = null
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(InputGrey, RoundedCornerShape(24.dp)),
                        isError = priceError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    if (priceError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = priceError!!,
                            color = BadgerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Category selection
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Device
                        FilterChip(
                            selected = selectedCategory == Category.DEVICES,
                            onClick = {
                                selectedCategory = Category.DEVICES
                                categoryError = null
                            },
                            label = { Text("Devices") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        // Tickets
                        FilterChip(
                            selected = selectedCategory == Category.TICKETS,
                            onClick = {
                                selectedCategory = Category.TICKETS
                                categoryError = null
                            },
                            label = { Text("Tickets") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        // Furniture
                        FilterChip(
                            selected = selectedCategory == Category.FURNITURE,
                            onClick = {
                                selectedCategory = Category.FURNITURE
                                categoryError = null
                            },
                            label = { Text("Furniture") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                    if (categoryError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = categoryError!!,
                            color = BadgerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Item details (multi-line)
                    Text(
                        text = "Item Details",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = {
                            details = it
                            detailsError = null
                            generalError = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(InputGrey, RoundedCornerShape(24.dp)),
                        isError = detailsError != null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = InputGrey,
                            focusedContainerColor = InputGrey,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent
                        )
                    )
                    if (detailsError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = detailsError!!,
                            color = BadgerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    if (generalError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = generalError!!,
                            color = BadgerRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Create Listing button
                    Button(
                        onClick = {
                            var hasError = false

                            // Title required
                            if (title.isBlank()) {
                                titleError = "Title cannot be empty."
                                hasError = true
                            }

                            // Price required and must be a number
                            if (price.isBlank()) {
                                priceError = "Price cannot be empty."
                                hasError = true
                            } else {
                                val numeric = price.toDoubleOrNull()
                                if (numeric == null || numeric <= 0.0) {
                                    priceError = "Price must be a valid number."
                                    hasError = true
                                }
                            }

                            // Details required
                            if (details.isBlank()) {
                                detailsError = "Item details cannot be empty."
                                hasError = true
                            }

                            // Category required
                            if (selectedCategory == null) {
                                categoryError = "Please select a category."
                                hasError = true
                            }

                            if (!hasError && !isLoading) {
                                isLoading = true
                                generalError = null
                                
                                coroutineScope.launch {
                                    val currentUser = FirebaseAuthHelper.getCurrentUser()
                                    if (currentUser == null) {
                                        isLoading = false
                                        generalError = "You must be signed in to create a listing."
                                        return@launch
                                    }
                                    
                                    try {
                                        // Get user profile for seller name (using schema field "Name")
                                        val userProfileResult = FirebaseAuthHelper.getUserProfile(currentUser.uid)
                                        val sellerName = userProfileResult.getOrNull()?.get("Name") as? String 
                                            ?: userProfileResult.getOrNull()?.get("name") as? String // Fallback for old data
                                            ?: currentUser.email?.substringBefore("@") ?: "Seller"
                                        
                                        // Upload image if selected
                                        var imageUrl: String? = null
                                        if (selectedImageUri != null) {
                                            val uploadResult = FirebaseStorageHelper.uploadListingImage(selectedImageUri!!)
                                            if (uploadResult.isSuccess) {
                                                imageUrl = uploadResult.getOrNull()
                                            } else {
                                                isLoading = false
                                                generalError = "Failed to upload image: ${uploadResult.exceptionOrNull()?.message}"
                                                return@launch
                                            }
                                        }
                                        
                                        // Create listing object
                                        val listing = Listing(
                                            id = "", // Will be set by Firestore
                                            title = title.trim(),
                                            price = price.trim(),
                                            distance = "0.0 mi", // TODO: Add location services
                                            timeAgo = "Just now",
                                            imageRes = null,
                                            imageUrl = imageUrl,
                                            category = selectedCategory ?: Category.OTHER,
                                            sellerId = currentUser.uid,
                                            sellerName = sellerName,
                                            description = details.trim(),
                                            createdAt = Date()
                                        )
                                        
                                        // Save to Firestore
                                        val result = ListingRepository.createListing(listing)
                                        isLoading = false
                                        
                                        if (result.isSuccess) {
                                            onListingCreated()
                                        } else {
                                            generalError = "Failed to create listing: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                                        }
                                    } catch (e: Exception) {
                                        isLoading = false
                                        generalError = "Failed to create listing: ${e.message ?: "Unknown error"}"
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BadgerRed,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create Listing",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
