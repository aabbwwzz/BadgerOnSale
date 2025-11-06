package com.cs407.badgeronsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs407.badgeronsale.ui.theme.BadgerOnSaleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { BadgerOnSaleTheme { AppNav() } }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onMenuClick = { /* drawer */ },
                onMessagesClick = { nav.navigate("messages") },
                onSearch = { },
                onFilterChanged = { },
                onListingClick = { }
            )
        }
        composable("messages") {
            MessagesScreen(
                onHomeClick = { nav.popBackStack() },
                onOpenChat = { dmId -> nav.navigate("chat/$dmId") }
            )
        }
        composable(
            route = "chat/{dmId}",
            arguments = listOf(navArgument("dmId") { type = NavType.StringType })
        ) { backStackEntry ->
            val dmId = backStackEntry.arguments?.getString("dmId").orEmpty()
            ChatDetailScreen(
                dmId = dmId,
                onBack = { nav.popBackStack() }
            )
        }
    }
}
