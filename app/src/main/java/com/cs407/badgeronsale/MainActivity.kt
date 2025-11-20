package com.cs407.badgeronsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.cs407.badgeronsale.ui.screens.MenuPage
import com.cs407.badgeronsale.ui.theme.BadgerOnSaleTheme

private enum class AppScreen { SIGN_IN, CREATE_ACCOUNT, HOME }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BadgerOnSaleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var current by remember { mutableStateOf(AppScreen.SIGN_IN) }

                    when (current) {
                        AppScreen.SIGN_IN -> {
                            SignInScreen(
                                onCreateAccountClick = { current = AppScreen.CREATE_ACCOUNT },
                                onSignInSuccess = { current = AppScreen.HOME } // temp “Home”
                            )
                        }
                        AppScreen.CREATE_ACCOUNT -> {
                            CreateAccountScreen(
                                onBackToSignInClick = { current = AppScreen.SIGN_IN },
                                onAccountCreated = { current = AppScreen.SIGN_IN }
                            )
                        }
                        AppScreen.HOME -> {
                            // TEMP home until your teammate’s Home page is ready
                            MenuPage()
                        }
                    }
                }
            }
        }
    }
}
