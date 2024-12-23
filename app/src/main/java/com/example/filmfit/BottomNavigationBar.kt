package com.example.filmfit


import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun BottomNavigationBar(navController: NavController) {
    BottomNavigation(
        modifier = Modifier.fillMaxWidth()
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Головна") },
            label = { Text("Головна") },
            selected = false,
            onClick = {
                val context = navController.context
                context.startActivity(
                    Intent(context, MainScreenActivity::class.java)
                )
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.Search, contentDescription = "Пошук") },
            label = { Text("Пошук") },
            selected = false,
            onClick = {
                val context = navController.context
                context.startActivity(
                    Intent(context, SearchActivity::class.java)
                )
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.List, contentDescription = "Ідеї") },
            label = { Text("Ідеї") },
            selected = false,
            onClick = { val context = navController.context
                context.startActivity(
                    Intent(context, CastomeFilmIdeaActivity::class.java)) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Акаунт") },
            label = { Text("Акаунт") },
            selected = false,
            onClick = {
                val context = navController.context
                context.startActivity(
                    Intent(context, AccountActivity::class.java)
                )
            }
        )

    }
}
