package com.example.blogapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.blogapp.ui.screens.auth.LoginScreen
import com.example.blogapp.ui.screens.auth.SignUpScreen
import com.example.blogapp.ui.screens.create.CreatePostScreen
import com.example.blogapp.ui.screens.detail.PostDetailScreen
import com.example.blogapp.ui.screens.favorites.FavoritesScreen
import com.example.blogapp.ui.screens.home.HomeScreen
import com.example.blogapp.ui.screens.profile.ProfileScreen

/**
 * Defines the sealed class for all screens in the app, providing a type-safe way
 * to handle navigation routes.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object Home : Screen("home")
    object CreatePost : Screen("create_post")
    object PostDetail : Screen("post_detail/{postId}") {
        fun createRoute(postId: String) = "post_detail/$postId"
    }
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
}

/**
 * The main navigation graph for the application.
 *
 * @param navController The NavHostController that manages app navigation.
 * @param startDestination The route for the first screen to be displayed.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // --- Authentication Flow ---

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = {
                    navController.navigate(Screen.SignUp.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onSignUpSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // --- Main App Flow ---

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onNavigateToCreatePost = {
                    navController.navigate(Screen.CreatePost.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToFavorites = {
                    navController.navigate(Screen.Favorites.route)
                }
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.PostDetail.route,
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: return@composable
            PostDetailScreen(
                postId = postId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPostDetail = { postId ->
                    navController.navigate(Screen.PostDetail.createRoute(postId))
                }
            )
        }
    }
}