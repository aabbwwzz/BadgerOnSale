package com.cs407.badgeronsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cs407.badgeronsale.ui.screens.FavoritesPage
import com.cs407.badgeronsale.ui.screens.ItemDescriptionPage
import com.cs407.badgeronsale.ui.screens.MenuPage
import com.cs407.badgeronsale.ui.theme.BadgerOnSaleTheme
import com.cs407.badgeronsale.repository.FavoritesRepository
import com.cs407.badgeronsale.repository.MessagesRepository
import com.cs407.badgeronsale.repository.ListingRepository
import com.cs407.badgeronsale.FirebaseAuthHelper
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.runtime.rememberCoroutineScope

// All screens we can navigate to
private enum class AppScreen {
    SIGN_IN,
    CREATE_ACCOUNT,
    HOME,
    MESSAGES,
    CHAT_DETAIL,
    USER_PROFILE,
    SELLER_PROFILE,  // For viewing seller profile from chat
    EDIT_PROFILE,
    MENU,
    FAVORITES,
    CREATE_LISTING,
    ITEM_DETAIL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        setContent {
            BadgerOnSaleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

@Composable
private fun AppNavigator() {
    val coroutineScope = rememberCoroutineScope()

    // Check if user is already signed in
    var current by remember { 
        mutableStateOf(
            if (FirebaseAuthHelper.isSignedIn()) AppScreen.HOME else AppScreen.SIGN_IN
        )
    }

    // Where we came from when opening an item (Home or Favorites)
    var lastListScreen by remember { mutableStateOf(AppScreen.HOME) }
    // Where we came from when viewing seller profile (Item Detail or Chat)
    var lastScreenBeforeSellerProfile by remember { mutableStateOf<AppScreen?>(null) }

    // Arguments for screens
    var selectedDmId by remember { mutableStateOf("1") }
    var selectedListingId by remember { mutableStateOf<String?>(null) }
    var selectedListing by remember { mutableStateOf<Listing?>(null) }
    var selectedSellerId by remember { mutableStateOf<String?>(null) }
    var selectedSellerInfo by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Shared favorites across the app - loaded from Firestore
    var favorites by remember { mutableStateOf(listOf<Listing>()) }
    // Track items that have been deleted to prevent them from reappearing
    var deletedFavoriteIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    // Load favorites from Firestore when user is signed in - real-time updates
    LaunchedEffect(FirebaseAuthHelper.isSignedIn()) {
        if (FirebaseAuthHelper.isSignedIn()) {
            try {
                // Clean up any duplicate favorites on first load
                coroutineScope.launch {
                    FavoritesRepository.cleanupDuplicateFavorites().onSuccess { count ->
                        if (count > 0) {
                            println("Cleaned up $count duplicate favorites")
                        }
                    }
                }
                
                FavoritesRepository.getUserFavorites().collectLatest { favoriteListings ->
                    // Filter out any items that have been marked as deleted
                    val filtered = favoriteListings.filter { listing ->
                        !deletedFavoriteIds.contains(listing.id)
                    }
                    
                    // If an item is in the deleted set but appears in the new list,
                    // it means it was re-added, so remove it from deleted set
                    val reAddedIds = favoriteListings.map { it.id }.intersect(deletedFavoriteIds)
                    if (reAddedIds.isNotEmpty()) {
                        println("Warning: ${reAddedIds.size} deleted item(s) reappeared, re-deleting: $reAddedIds")
                        // Re-delete them
                        coroutineScope.launch {
                            reAddedIds.forEach { listingId ->
                                FavoritesRepository.removeFromFavorites(listingId)
                            }
                        }
                    }
                    
                    favorites = filtered
                }
            } catch (e: Exception) {
                println("Error collecting favorites: ${e.message}")
                e.printStackTrace()
                favorites = emptyList()
            }
        } else {
            favorites = emptyList()
            deletedFavoriteIds = emptySet()
        }
    }

    // User profile - loaded from Firestore, unique per user
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                name = "",
                email = "",
                phone = "",
                graduationYear = "",
                address = "",
                avatarRes = R.drawable.avatar
            )
        )
    }
    
    // Load user profile from Firestore when user signs in - ensures unique profile per user
    LaunchedEffect(FirebaseAuthHelper.isSignedIn()) {
        if (FirebaseAuthHelper.isSignedIn()) {
            val currentUser = FirebaseAuthHelper.getCurrentUser()
            if (currentUser != null) {
                coroutineScope.launch {
                    try {
                        val profileResult = FirebaseAuthHelper.getUserProfile(currentUser.uid)
                        if (profileResult.isSuccess) {
                            val profileData = profileResult.getOrNull()!!
                            // Map Firestore data to UserProfile
                            userProfile = UserProfile(
                                name = profileData["Name"] as? String ?: currentUser.email?.substringBefore("@") ?: "User",
                                email = profileData["Email"] as? String ?: currentUser.email ?: "",
                                phone = profileData["phone"] as? String ?: "",
                                graduationYear = profileData["graduationYear"] as? String ?: "",
                                address = profileData["address"] as? String ?: "",
                                avatarRes = R.drawable.avatar // Default avatar, can be updated later with ProfilePicURL
                            )
                        } else {
                            // Profile doesn't exist - create a default one to ensure uniqueness
                            val email = currentUser.email ?: ""
                            val defaultName = email.substringBefore("@")
                            val createResult = FirebaseAuthHelper.saveUserProfile(
                                userId = currentUser.uid,
                                name = defaultName,
                                email = email,
                                phone = ""
                            )
                            if (createResult.isSuccess) {
                                // Reload the profile
                                val reloadResult = FirebaseAuthHelper.getUserProfile(currentUser.uid)
                                if (reloadResult.isSuccess) {
                                    val profileData = reloadResult.getOrNull()!!
                                    userProfile = UserProfile(
                                        name = profileData["Name"] as? String ?: defaultName,
                                        email = profileData["Email"] as? String ?: email,
                                        phone = profileData["phone"] as? String ?: "",
                                        graduationYear = profileData["graduationYear"] as? String ?: "",
                                        address = profileData["address"] as? String ?: "",
                                        avatarRes = R.drawable.avatar
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        println("Error loading user profile: ${e.message}")
                        e.printStackTrace()
                    }
                }
            }
        } else {
            // Reset profile when signed out
            userProfile = UserProfile(
                name = "",
                email = "",
                phone = "",
                graduationYear = "",
                address = "",
                avatarRes = R.drawable.avatar
            )
        }
    }

    when (current) {

        // ----------------------------------------------------------
        // LOGIN SCREEN
        // ----------------------------------------------------------
        AppScreen.SIGN_IN -> {
            SignInScreen(
                onCreateAccountClick = { current = AppScreen.CREATE_ACCOUNT },
                onSignInSuccess = { current = AppScreen.HOME }
            )
        }

        // ----------------------------------------------------------
        // CREATE ACCOUNT SCREEN
        // ----------------------------------------------------------
        AppScreen.CREATE_ACCOUNT -> {
            CreateAccountScreen(
                onBackToSignInClick = { current = AppScreen.SIGN_IN },
                onAccountCreated = { current = AppScreen.SIGN_IN }
            )
        }

        // ----------------------------------------------------------
        // HOME SCREEN (BROWSE LISTINGS)
        // ----------------------------------------------------------
        AppScreen.HOME -> {
            HomeScreen(
                onMenuClick = { current = AppScreen.MENU },
                onMessagesClick = { current = AppScreen.MESSAGES },
                onSearch = { /* hook up later */ },
                onFilterChanged = { /* hook up later */ },
                onListingClick = { listing ->
                    selectedListing = listing
                    lastListScreen = AppScreen.HOME
                    current = AppScreen.ITEM_DETAIL
                }
            )
        }

        // ----------------------------------------------------------
        // MESSAGES SCREEN (LIST OF DMS)
        // ----------------------------------------------------------
        AppScreen.MESSAGES -> {
            MessagesScreen(
                onHomeClick = { current = AppScreen.HOME },
                onOpenChat = { userId, listingId ->
                    selectedDmId = userId
                    selectedListingId = listingId
                    current = AppScreen.CHAT_DETAIL
                }
            )
        }

        // ----------------------------------------------------------
        // CHAT DETAIL SCREEN (1:1 DM)
        // ----------------------------------------------------------
        AppScreen.CHAT_DETAIL -> {
            ChatDetailScreen(
                otherUserId = selectedDmId,
                listingId = selectedListingId,
                onBack = { current = AppScreen.MESSAGES },
                onViewSellerProfile = { sellerId ->
                    // Store where we came from (chat detail page)
                    lastScreenBeforeSellerProfile = AppScreen.CHAT_DETAIL
                    selectedSellerId = sellerId
                    // Load seller info
                    coroutineScope.launch {
                        MessagesRepository.getUserInfo(sellerId).onSuccess {
                            selectedSellerInfo = it
                            current = AppScreen.SELLER_PROFILE
                        }
                    }
                }
            )
        }

        // ----------------------------------------------------------
        // USER PROFILE SCREEN (Own Profile)
        // ----------------------------------------------------------
        AppScreen.USER_PROFILE -> {
            val currentUser = FirebaseAuthHelper.getCurrentUser()
            UserProfileScreen(
                userName = userProfile.name.ifEmpty { currentUser?.email?.substringBefore("@") ?: "User" },
                avatarRes = userProfile.avatarRes,
                isOwnProfile = true,
                userId = currentUser?.uid,  // Pass user ID to load their listings
                onBack = { current = AppScreen.HOME },
                onHome = { current = AppScreen.HOME },
                onEditAccount = { current = AppScreen.EDIT_PROFILE },
                onListingDeleted = { listing ->
                    // Listing is already deleted from Firestore, just refresh
                    println("Listing deleted: ${listing.id}")
                }
            )
        }

        // ----------------------------------------------------------
        // SELLER PROFILE SCREEN (From Chat or Item Detail)
        // ----------------------------------------------------------
        AppScreen.SELLER_PROFILE -> {
            val sellerName = selectedSellerInfo?.get("Name") as? String ?: "Seller"
            val sellerProfilePic = selectedSellerInfo?.get("ProfilePicURL") as? String
            val sellerId = selectedSellerId
            // Navigate back to where we came from (Chat Detail or Item Detail)
            val backScreen = lastScreenBeforeSellerProfile ?: AppScreen.HOME
            // TODO: Load seller's listings and rating from Firestore
            UserProfileScreen(
                userName = sellerName,
                avatarRes = R.drawable.avatar, // TODO: Load from URL if available
                isOwnProfile = false,
                userId = sellerId,  // Pass seller ID to load their listings
                rating = 5.0, // TODO: Load actual rating from Firestore
                onBack = { current = backScreen },
                onHome = { current = AppScreen.HOME },
                onEditAccount = { /* Not applicable for seller profile */ },
                onListingDeleted = { /* Not applicable for seller profile */ }
            )
        }

        // ----------------------------------------------------------
        // EDIT PROFILE SCREEN
        // ----------------------------------------------------------
        AppScreen.EDIT_PROFILE -> {
            EditProfileScreen(
                initial = userProfile,
                onBack = { current = AppScreen.USER_PROFILE },
                onCancel = { current = AppScreen.USER_PROFILE },
                onSave = { updated ->
                    // Save to Firestore to ensure unique profile per user
                    val currentUser = FirebaseAuthHelper.getCurrentUser()
                    if (currentUser != null) {
                        coroutineScope.launch {
                            try {
                                val result = FirebaseAuthHelper.saveUserProfile(
                                    userId = currentUser.uid,
                                    name = updated.name,
                                    email = updated.email,
                                    phone = updated.phone,
                                    profilePicURL = null, // Can be updated later
                                    graduationYear = updated.graduationYear,
                                    address = updated.address
                                )
                                if (result.isSuccess) {
                                    // Update local state
                                    userProfile = updated
                                    current = AppScreen.USER_PROFILE
                                } else {
                                    println("Failed to save profile: ${result.exceptionOrNull()?.message}")
                                    // Still update local state even if Firestore save fails
                                    userProfile = updated
                                    current = AppScreen.USER_PROFILE
                                }
                            } catch (e: Exception) {
                                println("Error saving profile: ${e.message}")
                                // Still update local state
                                userProfile = updated
                                current = AppScreen.USER_PROFILE
                            }
                        }
                    } else {
                        // Not signed in, just update local state
                        userProfile = updated
                        current = AppScreen.USER_PROFILE
                    }
                }
            )
        }

        // ----------------------------------------------------------
        // MAIN MENU SCREEN
        // ----------------------------------------------------------
        AppScreen.MENU -> {
            MenuPage(
                onFavoritesClick = { current = AppScreen.FAVORITES },
                onEditProfileClick = { current = AppScreen.EDIT_PROFILE },
                onViewProfileClick = { current = AppScreen.USER_PROFILE },
                onCreateListingClick = { current = AppScreen.CREATE_LISTING },
                onLogoutClick = {
                    // Sign out from Firebase
                    FirebaseAuthHelper.signOut()
                    current = AppScreen.SIGN_IN
                },
                onHomeClick = {
                    current = AppScreen.HOME
                }
            )
        }

        // ----------------------------------------------------------
        // FAVORITES SCREEN
        // ----------------------------------------------------------
        AppScreen.FAVORITES -> {
            FavoritesPage(
                favorites = favorites,
                onBackClick = { current = AppScreen.MENU },
                onItemClick = { listing ->
                    selectedListing = listing
                    lastListScreen = AppScreen.FAVORITES
                    current = AppScreen.ITEM_DETAIL
                },
                onRemoveClick = { listing ->
                    // Immediately mark as deleted to prevent reappearing
                    deletedFavoriteIds = deletedFavoriteIds + listing.id
                    
                    // Remove from Firestore - optimistic UI already updated
                    coroutineScope.launch {
                        try {
                            println("Removing favorite for listing ID: ${listing.id}, title: ${listing.title}")
                            
                            // Aggressively delete all instances
                            var deletionSuccess = false
                            var attempts = 0
                            val maxAttempts = 5
                            
                            while (!deletionSuccess && attempts < maxAttempts) {
                                attempts++
                                
                                // First, clean up any duplicates proactively
                                FavoritesRepository.cleanupDuplicateFavorites().onSuccess { count ->
                                    if (count > 0) {
                                        println("Cleaned up $count duplicate favorites before deletion (attempt $attempts)")
                                    }
                                }
                                
                                val result = FavoritesRepository.removeFromFavorites(listing.id)
                                if (result.isSuccess) {
                                    println("Successfully removed favorite: ${listing.id} (attempt $attempts)")
                                    
                                    // Verify deletion
                                    delay(300)
                                    val isStillFavorited = FavoritesRepository.isFavorited(listing.id)
                                    if (!isStillFavorited) {
                                        deletionSuccess = true
                                        println("Verified: Favorite successfully removed")
                                        // Keep it in deletedFavoriteIds to prevent reappearing
                                    } else {
                                        println("Warning: Still favorited after deletion, retrying... (attempt $attempts)")
                                        delay(200)
                                    }
                                } else {
                                    val error = result.exceptionOrNull()
                                    println("Failed to remove favorite (attempt $attempts): ${error?.message}")
                                    if (attempts < maxAttempts) {
                                        delay(300)
                                    }
                                }
                            }
                            
                            if (!deletionSuccess) {
                                println("ERROR: Failed to remove favorite after $maxAttempts attempts")
                                // Keep it in deletedFavoriteIds anyway to prevent UI from showing it
                            }
                            
                            // The favorites list will update automatically via the real-time Flow
                            // The deletedFavoriteIds filter will prevent it from reappearing
                        } catch (e: Exception) {
                            println("Error removing favorite: ${e.message}")
                            e.printStackTrace()
                            // Keep it in deletedFavoriteIds to prevent reappearing even on error
                        }
                    }
                }
            )
        }

        // ----------------------------------------------------------
        // CREATE LISTING SCREEN
        // ----------------------------------------------------------
        AppScreen.CREATE_LISTING -> {
            CreateListingScreen(
                onBackClick = { current = AppScreen.MENU },
                onListingCreated = {
                    // Later you'll also add it to real data.
                    current = AppScreen.HOME
                }
            )
        }

        // ----------------------------------------------------------
        // ITEM DETAIL / DESCRIPTION SCREEN
        // ----------------------------------------------------------
        AppScreen.ITEM_DETAIL -> {
            val listing = selectedListing
            if (listing == null) {
                // If something went wrong, just recover to Home
                LaunchedEffect(Unit) {
                    current = AppScreen.HOME
                }
            } else {
                // Calculate favorite state from the favorites list
                val isFavorite = favorites.any { it.id == listing.id }
                
                // Check if current user is the owner of this listing
                val currentUserId = FirebaseAuthHelper.getCurrentUser()?.uid
                val isOwner = currentUserId != null && listing.sellerId == currentUserId

                ItemDescriptionPage(
                    itemName = listing.title,
                    sellerName = listing.sellerName ?: "BadgerOnSale Seller",
                    price = listing.price,
                    details = listOf(
                        "Distance: ${listing.distance}",
                        "Posted: ${listing.timeAgo}",
                        if (listing.description.isNotEmpty()) listing.description else ""
                    ).filter { it.isNotEmpty() },
                    imageRes = listing.imageRes,
                    imageUrl = listing.imageUrl,
                    isFavorite = isFavorite,
                    isOwner = isOwner,
                    onFavoriteClick = {
                        // Toggle favorite - add if not favorited, remove if favorited
                        coroutineScope.launch {
                            try {
                                if (isFavorite) {
                                    // Remove from favorites
                                    val result = FavoritesRepository.removeFromFavorites(listing.id)
                                    if (result.isFailure) {
                                        println("Failed to remove favorite: ${result.exceptionOrNull()?.message}")
                                    } else {
                                        println("Successfully removed favorite: ${listing.id}")
                                    }
                                } else {
                                    // Add to favorites
                                    val result = FavoritesRepository.addToFavorites(listing.id)
                                    if (result.isFailure) {
                                        println("Failed to add favorite: ${result.exceptionOrNull()?.message}")
                                    } else {
                                        println("Successfully added favorite: ${listing.id}")
                                    }
                                }
                                // The favorites list will update automatically via the real-time Flow
                            } catch (e: Exception) {
                                println("Error with favorites: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    },
                    onMessageClick = {
                        // Get seller ID from listing
                        val sellerId = listing.sellerId
                        if (sellerId != null && FirebaseAuthHelper.isSignedIn()) {
                            val currentUserId = FirebaseAuthHelper.getCurrentUser()?.uid
                            if (currentUserId != sellerId) {
                                selectedDmId = sellerId
                                selectedListingId = listing.id
                                current = AppScreen.CHAT_DETAIL
                            } else {
                                current = AppScreen.MESSAGES
                            }
                        } else {
                            current = AppScreen.MESSAGES
                        }
                    },
                    onDeleteClick = {
                        // Delete the listing
                        if (currentUserId != null) {
                            coroutineScope.launch {
                                try {
                                    val result = ListingRepository.deleteListing(listing.id, currentUserId)
                                    if (result.isSuccess) {
                                        println("Successfully deleted listing: ${listing.id}")
                                        // Navigate back to the previous screen
                                        current = lastListScreen
                                    } else {
                                        println("Failed to delete listing: ${result.exceptionOrNull()?.message}")
                                    }
                                } catch (e: Exception) {
                                    println("Error deleting listing: ${e.message}")
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    onSellerClick = {
                        // Navigate to seller profile
                        val sellerId = listing.sellerId
                        if (sellerId != null && sellerId != currentUserId) {
                            // Store where we came from (item detail page)
                            lastScreenBeforeSellerProfile = AppScreen.ITEM_DETAIL
                            // Load seller info and navigate to seller profile
                            coroutineScope.launch {
                                MessagesRepository.getUserInfo(sellerId).onSuccess {
                                    selectedSellerInfo = it
                                    selectedSellerId = sellerId
                                    current = AppScreen.SELLER_PROFILE
                                }.onFailure {
                                    println("Failed to load seller info: ${it.message}")
                                }
                            }
                        } else if (sellerId == currentUserId) {
                            // If clicking on own name, go to own profile
                            current = AppScreen.USER_PROFILE
                        }
                    },
                    onBackClick = {
                        current = lastListScreen
                    }
                )
            }
        }
    }
}
