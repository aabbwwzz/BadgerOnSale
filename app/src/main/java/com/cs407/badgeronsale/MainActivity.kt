package com.cs407.badgeronsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.cs407.badgeronsale.ui.theme.BadgerOnSaleTheme  // use whatever your Theme.kt defines

private enum class AuthScreen {
    SIGN_IN,
    CREATE_ACCOUNT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BadgerOnSaleTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf(AuthScreen.SIGN_IN) }

                    when (currentScreen) {
                        AuthScreen.SIGN_IN -> {
                            SignInScreen(
                                onCreateAccountClick = {
                                    currentScreen = AuthScreen.CREATE_ACCOUNT
                                },
                                onSignInSuccess = {
                                    // TODO: later navigate to Home screen
                                }
                            )
                        }

                        AuthScreen.CREATE_ACCOUNT -> {
                            CreateAccountScreen(
                                onBackToSignInClick = {
                                    currentScreen = AuthScreen.SIGN_IN
                                },
                                onAccountCreated = {
                                    // After creating account, go back to Sign In
                                    currentScreen = AuthScreen.SIGN_IN
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
