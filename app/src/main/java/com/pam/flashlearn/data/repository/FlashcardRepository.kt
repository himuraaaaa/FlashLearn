package com.pam.flashlearn.data.repository

import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.pam.flashlearn.data.model.Flashcard
import com.pam.flashlearn.data.model.FlashcardSet
import com.pam.flashlearn.data.model.QuizQuestion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class FlashcardRepository(
    private val firestore: FirebaseFirestore
) {
    // Sets
    suspend fun createSet(set: FlashcardSet): Result<String> {
        return try {
            // Create a new document with auto-generated ID
            val docRef = firestore.collection("sets").document()
            val setWithId = set.copy(id = docRef.id)

            // Save to Firestore
            docRef.set(setWithId).await()

            println("Set created in Firestore with ID: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            println("Error creating set in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun updateSet(set: FlashcardSet): Result<Unit> {
        return try {
            // Update existing document
            firestore.collection("sets").document(set.id).set(set).await()

            println("Set updated in Firestore: ${set.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating set in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteSet(setId: String): Result<Unit> {
        return try {
            println("Deleting set with ID: $setId")

            // Delete set document
            firestore.collection("sets").document(setId).delete().await()

            // Delete all flashcards in the set
            val flashcards = firestore.collection("flashcards")
                .whereEqualTo("setId", setId)
                .get()
                .await()

            println("Found ${flashcards.documents.size} flashcards to delete")

            // Delete each flashcard
            for (doc in flashcards.documents) {
                doc.reference.delete().await()
            }

            println("Set and associated flashcards deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting set from Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    fun getUserSets(userId: String): Flow<List<FlashcardSet>> = flow {
        try {
            println("Getting sets for user: $userId")

            val snapshot = firestore.collection("sets")
                .whereEqualTo("userId", userId)
                .orderBy("lastAccessed", Query.Direction.DESCENDING)
                .get()
                .await()

            val sets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FlashcardSet::class.java)
            }

            println("Found ${sets.size} sets for user")
            emit(sets)
        } catch (e: Exception) {
            println("Error getting user sets from Firestore: ${e.message}")
            emit(emptyList())
        }
    }

    fun getRecentSets(userId: String, limit: Int = 3): Flow<List<FlashcardSet>> = flow {
        try {
            println("Getting recent sets for user: $userId, limit: $limit")

            val snapshot = firestore.collection("sets")
                .whereEqualTo("userId", userId)
                .orderBy("lastAccessed", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val sets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FlashcardSet::class.java)
            }

            println("Found ${sets.size} recent sets")
            emit(sets)
        } catch (e: Exception) {
            println("Error getting recent sets from Firestore: ${e.message}")
            emit(emptyList())
        }
    }

    suspend fun getSetById(setId: String): Result<FlashcardSet> {
        return try {
            println("Getting set with ID: $setId")

            val doc = firestore.collection("sets").document(setId).get().await()
            val set = doc.toObject(FlashcardSet::class.java)

            if (set != null) {
                println("Set found: ${set.title}")

                // Update last accessed timestamp
                firestore.collection("sets").document(setId)
                    .update("lastAccessed", System.currentTimeMillis())
                    .await()

                Result.success(set)
            } else {
                println("Set not found with ID: $setId")
                Result.failure(Exception("Set not found"))
            }
        } catch (e: Exception) {
            println("Error getting set from Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    // Flashcards
    suspend fun createFlashcard(flashcard: Flashcard): Result<String> {
        return try {
            println("Creating flashcard for set: ${flashcard.setId}")

            // Create a new document with auto-generated ID
            val docRef = firestore.collection("flashcards").document()
            val cardWithId = flashcard.copy(id = docRef.id)

            // Create a map of the data to ensure all fields are properly set
            val flashcardData = mapOf(
                "id" to cardWithId.id,
                "question" to cardWithId.question,
                "answer" to cardWithId.answer,
                "setId" to cardWithId.setId,
                "createdAt" to cardWithId.createdAt,
                "updatedAt" to cardWithId.updatedAt
            )

            // Save to Firestore
            docRef.set(flashcardData).await()

            println("Flashcard created with ID: ${docRef.id}")

            // Update card count in set
            val setRef = firestore.collection("sets").document(flashcard.setId)
            firestore.runTransaction { transaction ->
                val setDoc = transaction.get(setRef)
                val set = setDoc.toObject(FlashcardSet::class.java)

                if (set != null) {
                    val newCount = set.cardCount + 1
                    transaction.update(setRef, "cardCount", newCount)
                    println("Updated set card count to $newCount")
                } else {
                    println("Set not found when updating card count")
                }
            }.await()

            Result.success(docRef.id)
        } catch (e: Exception) {
            println("Error creating flashcard in Firestore: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }



    suspend fun updateFlashcard(flashcard: Flashcard): Result<Unit> {
        return try {
            println("Updating flashcard with ID: ${flashcard.id}")

            // Update existing document
            firestore.collection("flashcards").document(flashcard.id)
                .set(flashcard)
                .await()

            println("Flashcard updated successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error updating flashcard in Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun deleteFlashcard(flashcard: Flashcard): Result<Unit> {
        return try {
            println("Deleting flashcard with ID: ${flashcard.id} from set: ${flashcard.setId}")

            // Delete flashcard document
            firestore.collection("flashcards").document(flashcard.id)
                .delete()
                .await()

            println("Flashcard deleted successfully")

            // Update card count in set using a transaction
            val setRef = firestore.collection("sets").document(flashcard.setId)
            firestore.runTransaction { transaction ->
                val setDoc = transaction.get(setRef)
                val set = setDoc.toObject(FlashcardSet::class.java)

                if (set != null && set.cardCount > 0) {
                    val newCount = set.cardCount - 1
                    transaction.update(setRef, "cardCount", newCount)
                    println("Updated set card count to $newCount")
                } else {
                    println("Set not found or card count already 0")
                }
            }.await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting flashcard from Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    fun getFlashcardsBySetId(setId: String): Flow<List<Flashcard>> = flow {
        try {
            println("Getting flashcards for set: $setId")

            // Ensure we're using the correct collection name and field
            val snapshot = firestore.collection("flashcards")
                .whereEqualTo("setId", setId)
                .get()
                .await()

            println("Firestore query returned ${snapshot.documents.size} documents")

            // Debug: print raw document data
            snapshot.documents.forEach { doc ->
                println("Document ID: ${doc.id}, Data: ${doc.data}")
            }

            val flashcards = snapshot.documents.mapNotNull { doc ->
                try {
                    // Try to manually construct the Flashcard object from document data
                    val data = doc.data
                    if (data != null) {
                        Flashcard(
                            id = doc.id,
                            question = data["question"] as? String ?: "",
                            answer = data["answer"] as? String ?: "",
                            setId = data["setId"] as? String ?: "",
                            createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                            updatedAt = (data["updatedAt"] as? Long) ?: System.currentTimeMillis()
                        )
                    } else {
                        doc.toObject(Flashcard::class.java)
                    }
                } catch (e: Exception) {
                    println("Error converting document to Flashcard: ${e.message}")
                    null
                }
            }

            println("Successfully converted ${flashcards.size} documents to Flashcard objects")

            // Debug: print each flashcard
            flashcards.forEachIndexed { index, card ->
                println("Flashcard $index - ID: ${card.id}, Q: ${card.question}, A: ${card.answer}")
            }

            emit(flashcards)
        } catch (e: Exception) {
            println("Error getting flashcards from Firestore: ${e.message}")
            e.printStackTrace()
            emit(emptyList())
        }
    }


    // Search
    fun searchSets(userId: String, query: String): Flow<List<FlashcardSet>> = flow {
        try {
            println("Searching sets for user: $userId with query: $query")

            // Get all sets for the user
            val snapshot = firestore.collection("sets")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            // Filter by title or description containing the query
            val sets = snapshot.documents.mapNotNull { doc ->
                doc.toObject(FlashcardSet::class.java)
            }.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }

            println("Found ${sets.size} sets matching query")
            emit(sets)
        } catch (e: Exception) {
            println("Error searching sets in Firestore: ${e.message}")
            emit(emptyList())
        }
    }

    // Generate quiz questions from flashcards
    fun generateQuizQuestions(setId: String): Flow<List<QuizQuestion>> = flow {
        try {
            println("Generating quiz questions for set: $setId")

            // Get all flashcards for the set
            val snapshot = firestore.collection("flashcards")
                .whereEqualTo("setId", setId)
                .get()
                .await()

            val flashcards = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Flashcard::class.java)
            }

            println("Found ${flashcards.size} flashcards for quiz")

            if (flashcards.size < 4) {
                // Not enough flashcards for a good quiz
                println("Not enough flashcards for a good quiz (minimum 4 recommended)")
                // Still create questions with what we have
            }

            // Create multiple choice questions
            val questions = flashcards.map { card ->
                // Get other answers as options (excluding the current card's answer)
                val otherAnswers = flashcards
                    .filter { it.id != card.id }
                    .map { it.answer }
                    .shuffled()
                    .take(3) // Take up to 3 other answers

                // Combine correct answer with other answers and shuffle
                val options = (listOf(card.answer) + otherAnswers).shuffled()

                QuizQuestion(
                    id = card.id,
                    question = card.question,
                    options = options,
                    correctAnswer = card.answer,
                    flashcardId = card.id
                )
            }

            println("Generated ${questions.size} quiz questions")
            emit(questions)
        } catch (e: Exception) {
            println("Error generating quiz questions: ${e.message}")
            emit(emptyList())
        }
    }

    // Additional utility methods

    suspend fun getFlashcardCount(setId: String): Int {
        return try {
            val snapshot = firestore.collection("flashcards")
                .whereEqualTo("setId", setId)
                .count()
                .get(AggregateSource.SERVER)
                .await()

            snapshot.count.toInt()
        } catch (e: Exception) {
            println("Error getting flashcard count: ${e.message}")
            0
        }
    }

    suspend fun updateSetCardCount(setId: String) {
        try {
            val count = getFlashcardCount(setId)

            firestore.collection("sets").document(setId)
                .update("cardCount", count)
                .await()

            println("Updated set card count to $count")
        } catch (e: Exception) {
            println("Error updating set card count: ${e.message}")
        }
    }

    suspend fun getFlashcardById(flashcardId: String): Result<Flashcard> {
        return try {
            val doc = firestore.collection("flashcards").document(flashcardId).get().await()
            val flashcard = doc.toObject(Flashcard::class.java)

            if (flashcard != null) {
                Result.success(flashcard)
            } else {
                Result.failure(Exception("Flashcard not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
