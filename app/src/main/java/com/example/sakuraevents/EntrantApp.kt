package com.example.sakuraevents


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun EntrantApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("Home") { HomeScreen(navController) }
        composable("Search") { SearchScreen(navController) }
        composable("Profile") { ProfileScreen(navController) }
    }
}
