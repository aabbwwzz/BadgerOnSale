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

// All screens we can navigate to
private enum class AppScreen {
    SIGN_IN,
    CREATE_ACCOUNT,
    HOME,
    MESSAGES,
    CHAT_DETAIL,
    USER_PROFILE,
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

    // Current screen user is on

    var current by remember { mutableStateOf(AppScreen.SIGN_IN) }

    // Track which screen opened the item details (home or favorites)
    var lastListScreen by remember { mutableStateOf(AppScreen.HOME) }

    // Arguments for screens
    var selectedDmId by remember { mutableStateOf("1") }
    var selectedListing by remember { mutableStateOf<Listing?>(null) }

    // Shared favorites across app (in-memory, hardcoded for now)
    var favorites by remember { mutableStateOf(listOf<Listing>()) }

    // Temporary User Profile (hardcoded)
    var userProfile by remember {
        mutableStateOf(
            UserProfile(
                name = "Jouhara Ali",
                email = "user@wisc.edu",
                phone = "(608) 555-1234",
                graduationYear = "2026",
                address = "123 University Ave",
                avatarRes = R.drawable.avatar
            )
        )
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
        // HOME SCREEN (LIST OF ITEMS)
        // ----------------------------------------------------------
        AppScreen.HOME -> {
            HomeScreen(
                onMenuClick = { current = AppScreen.MENU },
                onMessagesClick = { current = AppScreen.MESSAGES },
                onSearch = {},
                onFilterChanged = {},
                onListingClick = { listing ->
                    selectedListing = listing
                    lastListScreen = AppScreen.HOME
                    current = AppScreen.ITEM_DETAIL
                }
            )
        }

        // ----------------------------------------------------------
        // MESSAGES LIST SCREEN
        // ----------------------------------------------------------
        AppScreen.MESSAGES -> {
            MessagesScreen(
                onHomeClick = { current = AppScreen.HOME },
                onOpenChat = { dmId ->
                    selectedDmId = dmId
                    current = AppScreen.CHAT_DETAIL
                }
            )
        }

        // ----------------------------------------------------------
        // DIRECT MESSAGE CHAT SCREEN
        // ----------------------------------------------------------
        AppScreen.CHAT_DETAIL -> {
            ChatDetailScreen(
                dmId = selectedDmId,
                onBack = { current = AppScreen.MESSAGES }
            )
        }

        // ----------------------------------------------------------
        // PROFILE SCREEN
        // ----------------------------------------------------------
        AppScreen.USER_PROFILE -> {
            UserProfileScreen(
                userName = userProfile.name,
                avatarRes = userProfile.avatarRes,
                onBack = { current = AppScreen.HOME },
                onHome = { current = AppScreen.HOME },
                onEditAccount = { current = AppScreen.EDIT_PROFILE },
                onListingDeleted = { /* TODO later */ }
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
                    userProfile = updated
                    current = AppScreen.USER_PROFILE
                }
            )
        }

        // ----------------------------------------------------------
        // MENU PAGE
        // ----------------------------------------------------------
        AppScreen.MENU -> {
            MenuPage(
                onFavoritesClick = { current = AppScreen.FAVORITES },
                onEditProfileClick = { current = AppScreen.EDIT_PROFILE },
                onViewProfileClick = { current = AppScreen.USER_PROFILE },
                onCreateListingClick = { current = AppScreen.CREATE_LISTING },
                onLogoutClick = { current = AppScreen.SIGN_IN },
                onHomeClick = { current = AppScreen.HOME }
            )
        }

        // ----------------------------------------------------------
        // FAVORITES PAGE
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
                    favorites = favorites.filterNot { it.id == listing.id }
                }
            )
        }

        // ----------------------------------------------------------
        // CREATE LISTING PAGE
        // ----------------------------------------------------------
        AppScreen.CREATE_LISTING -> {
            CreateListingScreen(
                onBackClick = { current = AppScreen.MENU },
                onListingCreated = {
                    // For now just go back to Home; later we can add it to mockListings
                    current = AppScreen.HOME
                }
            )
        }

        // ----------------------------------------------------------
        // ITEM DESCRIPTION PAGE
        // ----------------------------------------------------------
        AppScreen.ITEM_DETAIL -> {
            val listing = selectedListing
            if (listing == null) {
                // Safety fallback
                LaunchedEffect(Unit) { current = AppScreen.HOME }
            } else {
                val isFavorite = favorites.any { it.id == listing.id }

                ItemDescriptionPage(
                    itemName = listing.title,
                    sellerName = "BadgerOnSale Seller",
                    price = listing.price,
                    details = listOf(
                        "Distance: ${listing.distance}",
                        "Posted: ${listing.timeAgo}"
                    ),
                    imageRes = listing.imageRes,
                    isFavorite = isFavorite,
                    onFavoriteClick = {
                        favorites =
                            if (isFavorite)
                                favorites.filterNot { it.id == listing.id }
                            else
                                favorites + listing
                    },
                    onMessageClick = {
                        current = AppScreen.MESSAGES
                    },
                    onBackClick = {
                        current = lastListScreen
                    }
                )
            }
        }
    }
}
