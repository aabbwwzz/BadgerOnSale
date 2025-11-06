package com.cs407.badgeronsale

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.cs407.badgeronsale.ui.screens.FavoritesPage
import com.cs407.badgeronsale.ui.screens.ItemDescriptionPage
import com.cs407.badgeronsale.ui.screens.MenuPage
import com.cs407.badgeronsale.ui.theme.BadgerOnSaleTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = Color.White) {
                MenuPage()
                FavoritesPage()
                ItemDescriptionPage()
            }
        }
    }
}
