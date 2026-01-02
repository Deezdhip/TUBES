package com.example.tubes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.example.tubes.util.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Create notification channel untuk timer notifications
        NotificationHelper.createNotificationChannel(this)
        
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
        startDestination = "splash",
        // ==================== MODERN SLIDE TRANSITIONS ====================
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
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
                onNavigateToTimer = { taskId ->
                    // Navigate dengan Task ID
                    navController.navigate("timer/$taskId")
                },
                onNavigateToRecycleBin = {
                    navController.navigate("recycle_bin")
                },
                onNavigateToCategory = { categoryName ->
                    navController.navigate("category/$categoryName")
                },
                onLogout = {
                    // Navigate ke login dan hapus seluruh back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Route untuk RecycleBinScreen
        composable("recycle_bin") {
             com.example.tubes.ui.screens.RecycleBinScreen(
                 onNavigateUp = {
                     navController.popBackStack()
                 }
             )
        }

        // Route untuk TimerScreen dengan Task ID argument
        composable(
            route = "timer/{taskId}",
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TimerScreen(
                taskId = taskId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        // Route untuk CategoryTasksScreen with category filter
        composable(
            route = "category/{categoryName}",
            arguments = listOf(
                navArgument("categoryName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "Tasks"
            com.example.tubes.ui.screens.CategoryTasksScreen(
                categoryName = categoryName,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTimer = { taskId ->
                    navController.navigate("timer/$taskId")
                }
            )
        }
    }
}