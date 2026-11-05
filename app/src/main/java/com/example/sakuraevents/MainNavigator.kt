package com.example.sakuraevents


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class COLOURS(val colour: Color){
    LIGHTYELLOW(Color(0xFFFFF9C4)),
    GREEN(Color(0xFFC8E6C9)),
    DARKGREEN(Color(0xFF4CAF50)),
    BLACK(Color(0xFF000000))
}
@Composable
fun mainNavigator(firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home"){
        composable("Home") { homeScreen(navController, firestoreDriver, modifier) }
        composable("Search") { searchScreen(navController) }
        composable("Profile") { profileScreen(navController) }
    }
}
