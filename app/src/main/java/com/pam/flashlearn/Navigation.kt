package com.pam.flashlearn

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.pam.flashlearn.ui.album.AlbumScreen
import com.pam.flashlearn.ui.auth.LoginScreen
import com.pam.flashlearn.ui.auth.RegisterScreen
import com.pam.flashlearn.ui.flashcard.CreateFlashcardScreen
import com.pam.flashlearn.ui.flashcard.CreateFlashcardSetScreen
import com.pam.flashlearn.ui.flashcard.FlashcardSetScreen
import com.pam.flashlearn.ui.flashcard.FlashcardViewerScreen
import com.pam.flashlearn.ui.home.HomeScreen
import com.pam.flashlearn.ui.profile.ProfileScreen
import com.pam.flashlearn.ui.quiz.QuizScreen

@Composable
fun Navigation(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable("login") {
            val viewModel = ServiceLocator.provideAuthViewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = { navController.navigate("register") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            val viewModel = ServiceLocator.provideAuthViewModel()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = { navController.navigate("login") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Main screens
        composable("home") {
            val viewModel = ServiceLocator.provideHomeViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSet = { setId ->
                    navController.navigate("set/$setId")
                },
                onNavigateToAlbum = { navController.navigate("album") },
                onNavigateToCreateSet = { navController.navigate("create_set") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("album") {
            val viewModel = ServiceLocator.provideAlbumViewModel()
            AlbumScreen(
                viewModel = viewModel,
                onNavigateToSet = { setId ->
                    navController.navigate("set/$setId")
                },
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToCreateSet = { navController.navigate("create_set") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }

        composable("create_set") {
            val viewModel = ServiceLocator.provideFlashcardViewModel()
            CreateFlashcardSetScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToSet = { setId ->
                    navController.navigate("set/$setId") {
                        popUpTo("create_set") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "set/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            val viewModel = ServiceLocator.provideFlashcardViewModel()
            FlashcardSetScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToAddFlashcard = { id ->
                    navController.navigate("add_flashcard/$id")
                },
                onNavigateToEditSet = { id ->
                    // Edit set functionality
                },
                onNavigateToStudy = { id ->
                    navController.navigate("study/$id")
                },
                onNavigateToQuiz = { id ->
                    navController.navigate("quiz/$id")
                }
            )
        }

        composable(
            route = "add_flashcard/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            val viewModel = ServiceLocator.provideFlashcardViewModel()
            CreateFlashcardScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "study/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            val viewModel = ServiceLocator.provideFlashcardViewModel()
            FlashcardViewerScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "quiz/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            val viewModel = ServiceLocator.provideQuizViewModel()
            QuizScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            val viewModel = ServiceLocator.provideProfileViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToHome = { navController.navigate("home") },
                onNavigateToAlbum = { navController.navigate("album") },
                onNavigateToCreateSet = { navController.navigate("create_set") },
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }
    }
}
