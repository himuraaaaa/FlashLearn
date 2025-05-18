package com.pam.flashlearn.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.pam.flashlearn.data.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore
) {
    fun getUserProfile(userId: String): Flow<User?> = flow {
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            val user = doc.toObject(User::class.java)
            emit(user)
        } catch (e: Exception) {
            emit(null)
        }
    }

    suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            firestore.collection("users").document(user.id).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRecentSets(userId: String, setId: String): Result<Unit> {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val user = userDoc.toObject(User::class.java)

            if (user != null) {
                // Add setId to the front of the list and remove duplicates
                val recentSets = (listOf(setId) + user.recentSets)
                    .distinct()
                    .take(5) // Keep only the 5 most recent sets

                firestore.collection("users").document(userId)
                    .update("recentSets", recentSets)
                    .await()

                Result.success(Unit)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
