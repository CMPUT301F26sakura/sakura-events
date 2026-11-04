package com.example.sakuraevents


import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun entrantApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("Home") { homeScreen(navController) }
        composable("Search") { searchScreen(navController) }
        composable("Profile") { profileScreen(navController) }
    }
}
