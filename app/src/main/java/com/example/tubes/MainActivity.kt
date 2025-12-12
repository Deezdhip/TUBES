package com.example.tubes

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
import com.example.tubes.ui.screens.HomeScreen
import com.example.tubes.ui.screens.LoginScreen
import com.example.tubes.ui.screens.RegisterScreen
import com.example.tubes.ui.screens.SplashScreen
import com.example.tubes.ui.screens.TimerScreen
import com.example.tubes.ui.theme.TUBESTheme
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TUBESTheme {
                TaskManagerApp()
            }
        }
    }
}

/**
 * Main app composable dengan Navigation setup dan Splash Screen
 */
@Composable
fun TaskManagerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        // Splash Screen - Always start here
        composable("splash") {
            SplashScreen(
                onNavigateToNext = {
                    // Check auth state after splash
                    val currentUser = FirebaseAuth.getInstance().currentUser
                    val destination = if (currentUser != null) "home" else "login"
                    
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        // Route untuk LoginScreen
        composable("login") {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate("register")
                },
                onLoginSuccess = {
                    // Navigate ke home dan hapus login dari back stack
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        // Route untuk RegisterScreen
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    // Navigate ke home dan hapus register dari back stack
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Route untuk HomeScreen (Dashboard)
        composable("home") {
            HomeScreen(
                onNavigateToTimer = { taskTitle ->
                    // Encode taskTitle untuk menghindari masalah dengan special characters
                    val encodedTitle = java.net.URLEncoder.encode(taskTitle, StandardCharsets.UTF_8.toString())
                    navController.navigate("timer/$encodedTitle")
                },
                onLogout = {
                    // Navigate ke login dan hapus seluruh back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Route untuk TimerScreen dengan argument
        composable(
            route = "timer/{taskTitle}",
            arguments = listOf(
                navArgument("taskTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val encodedTitle = backStackEntry.arguments?.getString("taskTitle") ?: ""
            val taskTitle = URLDecoder.decode(encodedTitle, StandardCharsets.UTF_8.toString())
            TimerScreen(
                taskTitle = taskTitle,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}