package com.example.sakuraevents


import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController


val light_yellow = Color(0xFFFFF9C4)
val green = Color(0xFFC8E6C9)
val dark_green = Color(0xFF4CAF50)
val black = Color(0xFF000000)
@Composable
fun entrantApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("Home") { homeScreen(navController) }
        composable("Search") { searchScreen(navController) }
        composable( "Rating") {RatingScreen() }
        composable("Profile") { profileScreen(navController) }
    }
}
