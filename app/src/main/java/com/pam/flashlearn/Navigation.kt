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
            println("Navigating to login screen")
            val viewModel = ServiceLocator.provideAuthViewModel()
            LoginScreen(
                viewModel = viewModel,
                onNavigateToRegister = {
                    println("Navigating to register screen")
                    navController.navigate("register")
                },
                onNavigateToHome = {
                    println("Navigating to home screen after login")
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("register") {
            println("Navigating to register screen")
            val viewModel = ServiceLocator.provideAuthViewModel()
            RegisterScreen(
                viewModel = viewModel,
                onNavigateToLogin = {
                    println("Navigating back to login screen")
                    navController.navigate("login")
                },
                onNavigateToHome = {
                    println("Navigating to home screen after registration")
                    navController.navigate("home") {
                        popUpTo("register") { inclusive = true }
                    }
                }
            )
        }

        // Main screens
        composable("home") {
            println("Navigating to home screen")
            val viewModel = ServiceLocator.provideHomeViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToSet = { setId ->
                    println("Navigating to set screen with ID: $setId")
                    navController.navigate("set/$setId")
                },
                onNavigateToAlbum = {
                    println("Navigating to album screen")
                    navController.navigate("album")
                },
                onNavigateToCreateSet = {
                    println("Navigating to create set screen")
                    navController.navigate("create_set")
                },
                onNavigateToProfile = {
                    println("Navigating to profile screen")
                    navController.navigate("profile")
                }
            )
        }

        composable("album") {
            println("Navigating to album screen")
            val viewModel = ServiceLocator.provideAlbumViewModel()
            AlbumScreen(
                viewModel = viewModel,
                onNavigateToSet = { setId ->
                    println("Navigating to set screen with ID: $setId")
                    navController.navigate("set/$setId")
                },
                onNavigateToHome = {
                    println("Navigating to home screen")
                    navController.navigate("home")
                },
                onNavigateToCreateSet = {
                    println("Navigating to create set screen")
                    navController.navigate("create_set")
                },
                onNavigateToProfile = {
                    println("Navigating to profile screen")
                    navController.navigate("profile")
                }
            )
        }

        composable("create_set") {
            println("Navigating to create set screen")
            val viewModel = ServiceLocator.provideFlashcardViewModel()
            CreateFlashcardSetScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    println("Navigating back from create set screen")
                    navController.popBackStack()
                },
                onNavigateToSet = { setId ->
                    println("Navigating to set screen with newly created ID: $setId")
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
            println("Navigating to set screen with ID: $setId")

            val viewModel = ServiceLocator.provideFlashcardViewModel()

            // Ensure we load the data when navigating to this screen
            viewModel.loadSet(setId)
            viewModel.loadFlashcards(setId)

            FlashcardSetScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = {
                    println("Navigating back from set screen")
                    navController.popBackStack()
                },
                onNavigateToAddFlashcard = { id ->
                    println("Navigating to add flashcard screen for set: $id")
                    navController.navigate("add_flashcard/$id")
                },
                onNavigateToEditSet = { id ->
                    println("Edit set functionality not implemented yet for set: $id")
                    // Edit set functionality
                },
                onNavigateToStudy = { id ->
                    println("Navigating to study screen for set: $id")
                    navController.navigate("study/$id")
                },
                onNavigateToQuiz = { id ->
                    println("Navigating to quiz screen for set: $id")
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
            println("Navigating to add flashcard screen for set: $setId")

            val viewModel = ServiceLocator.provideFlashcardViewModel()
            CreateFlashcardScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = {
                    println("Navigating back to set screen after adding flashcard")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "study/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            println("Navigating to study screen for set: $setId")

            val viewModel = ServiceLocator.provideFlashcardViewModel()
            FlashcardViewerScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = {
                    println("Navigating back from study screen")
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "quiz/{setId}",
            arguments = listOf(
                navArgument("setId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val setId = backStackEntry.arguments?.getString("setId") ?: ""
            println("Navigating to quiz screen for set: $setId")

            val viewModel = ServiceLocator.provideQuizViewModel()
            QuizScreen(
                viewModel = viewModel,
                setId = setId,
                onNavigateBack = {
                    println("Navigating back from quiz screen")
                    navController.popBackStack()
                }
            )
        }

        composable("profile") {
            println("Navigating to profile screen")
            val viewModel = ServiceLocator.provideProfileViewModel()
            ProfileScreen(
                viewModel = viewModel,
                onNavigateToHome = {
                    println("Navigating to home screen from profile")
                    navController.navigate("home")
                },
                onNavigateToAlbum = {
                    println("Navigating to album screen from profile")
                    navController.navigate("album")
                },
                onNavigateToCreateSet = {
                    println("Navigating to create set screen from profile")
                    navController.navigate("create_set")
                },
                onNavigateToLogin = {
                    println("Logging out and navigating to login screen")
                    navController.navigate("login") {
                        popUpTo("profile") { inclusive = true }
                    }
                }
            )
        }
    }
}
