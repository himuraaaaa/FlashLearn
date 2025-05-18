package com.pam.flashlearn

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.pam.flashlearn.data.repository.AuthRepository
import com.pam.flashlearn.data.repository.FlashcardRepository
import com.pam.flashlearn.data.repository.UserRepository
import com.pam.flashlearn.ui.album.AlbumViewModel
import com.pam.flashlearn.ui.auth.AuthViewModel
import com.pam.flashlearn.ui.flashcard.FlashcardViewModel
import com.pam.flashlearn.ui.home.HomeViewModel
import com.pam.flashlearn.ui.profile.ProfileViewModel
import com.pam.flashlearn.ui.quiz.QuizViewModel

object ServiceLocator {
    // Firebase services
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val storage: FirebaseStorage by lazy { FirebaseStorage.getInstance() }

    // Repositories
    val authRepository: AuthRepository by lazy { AuthRepository(firebaseAuth, firestore) }
    val flashcardRepository: FlashcardRepository by lazy { FlashcardRepository(firestore) }
    val userRepository: UserRepository by lazy { UserRepository(firestore) }

    // ViewModels
    fun provideAuthViewModel(): AuthViewModel {
        return AuthViewModel(authRepository)
    }

    fun provideHomeViewModel(): HomeViewModel {
        return HomeViewModel(authRepository, flashcardRepository)
    }

    fun provideAlbumViewModel(): AlbumViewModel {
        return AlbumViewModel(authRepository, flashcardRepository)
    }

    fun provideFlashcardViewModel(): FlashcardViewModel {
        return FlashcardViewModel(authRepository, flashcardRepository, userRepository)
    }

    fun provideProfileViewModel(): ProfileViewModel {
        return ProfileViewModel(authRepository, userRepository)
    }

    fun provideQuizViewModel(): QuizViewModel {
        return QuizViewModel(flashcardRepository)
    }
}
