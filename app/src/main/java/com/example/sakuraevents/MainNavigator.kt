package com.example.sakuraevents


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

enum class COLOURS(val colour: Color){
    LIGHTYELLOW(Color(0xFFEEEEB3)),
    DARKYELLOW(Color(0xFFD8D870)),
    GREEN(Color(0xFF62B266)),
    DARKGREEN(Color(0xFF419874)),
    BLACK(Color(0xFF000000)),
    WHITE(Color(0xFFFFFFFF))
}
@Composable
fun mainNavigator(firestoreDriver: FirestoreDriver, modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val searchScreen = SearchScreen(navController, firestoreDriver, modifier)

    NavHost(navController = navController, startDestination = "home"){
        composable("Home") { homeScreen(navController, firestoreDriver, modifier) }
        composable("Search") { searchScreen.searchScreen() }
        composable("Profile") { profileScreen(navController) }
    }
}
