package com.pam.flashlearn.ui.quiz

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pam.flashlearn.data.model.QuizQuestion
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    setId: String,
    onNavigateBack: () -> Unit
) {
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var isAnswerSubmitted by remember { mutableStateOf(false) }
    var isQuizCompleted by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(setId) {
        viewModel.loadSet(setId)
        viewModel.generateQuiz(setId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Mode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.questions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No flashcards available for quiz",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Go Back")
                        }
                    }
                }
            } else if (isQuizCompleted) {
                // Quiz results screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Quiz Completed!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Your Score: $score/${viewModel.questions.size}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${(score.toFloat() / viewModel.questions.size * 100).roundToInt()}%",
                        style = MaterialTheme.typography.titleLarge,
                        color = when {
                            score.toFloat() / viewModel.questions.size >= 0.8 -> Color.Green
                            score.toFloat() / viewModel.questions.size >= 0.6 -> Color(0xFFFFA500) // Orange
                            else -> Color.Red
                        }
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Review Answers",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn {
                        items(viewModel.answeredQuestions) { answered ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (answered.isCorrect)
                                        Color(0xFFE8F5E9) // Light green
                                    else
                                        Color(0xFFFFEBEE) // Light red
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (answered.isCorrect)
                                                Icons.Default.Check
                                            else
                                                Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (answered.isCorrect) Color.Green else Color.Red
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        Text(
                                            text = answered.question.question,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Your answer: ${answered.selectedAnswer}",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (answered.isCorrect)
                                            Color.Green
                                        else
                                            Color.Red
                                    )

                                    if (!answered.isCorrect) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Correct answer: ${answered.question.correctAnswer}",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = Color.Green
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                // Reset the quiz
                                currentQuestionIndex = 0
                                selectedAnswer = null
                                isAnswerSubmitted = false
                                isQuizCompleted = false
                                score = 0
                                viewModel.resetQuiz()
                                viewModel.generateQuiz(setId)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Try Again")
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Finish")
                        }
                    }
                }
            } else {
                // Quiz questions screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Progress indicator
                    Text(
                        text = "Question ${currentQuestionIndex + 1} of ${viewModel.questions.size}",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = (currentQuestionIndex + 1).toFloat() / viewModel.questions.size,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Current question
                    val question = viewModel.questions[currentQuestionIndex]

                    Text(
                        text = question.question,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Answer options
                    val options = question.options.shuffled()

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        options.forEach { option ->
                            val isSelected = selectedAnswer == option
                            val isCorrect = isAnswerSubmitted && option == question.correctAnswer
                            val isWrong = isAnswerSubmitted && isSelected && option != question.correctAnswer

                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(
                                    width = 2.dp,
                                    color = when {
                                        isCorrect -> Color.Green
                                        isWrong -> Color.Red
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.outline
                                    }
                                ),
                                colors = CardDefaults.outlinedCardColors(
                                    containerColor = when {
                                        isCorrect -> Color(0xFFE8F5E9) // Light green
                                        isWrong -> Color(0xFFFFEBEE) // Light red
                                        isSelected -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.surface
                                    }
                                ),
                                onClick = {
                                    if (!isAnswerSubmitted) {
                                        selectedAnswer = option
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (isCorrect) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Correct",
                                            tint = Color.Green
                                        )
                                    } else if (isWrong) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Wrong",
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Navigation buttons
                    Button(
                        onClick = {
                            if (!isAnswerSubmitted) {
                                // Submit answer
                                isAnswerSubmitted = true

                                // Check if answer is correct
                                if (selectedAnswer == question.correctAnswer) {
                                    score++
                                }

                                // Record the answer
                                viewModel.recordAnswer(
                                    question = question,
                                    selectedAnswer = selectedAnswer ?: "",
                                    isCorrect = selectedAnswer == question.correctAnswer
                                )
                            } else {
                                // Move to next question or complete quiz
                                if (currentQuestionIndex < viewModel.questions.size - 1) {
                                    currentQuestionIndex++
                                    selectedAnswer = null
                                    isAnswerSubmitted = false
                                } else {
                                    isQuizCompleted = true
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isAnswerSubmitted || selectedAnswer != null
                    ) {
                        Text(
                            text = if (!isAnswerSubmitted) {
                                "Submit Answer"
                            } else if (currentQuestionIndex < viewModel.questions.size - 1) {
                                "Next Question"
                            } else {
                                "Finish Quiz"
                            }
                        )
                    }
                }
            }
        }
    }
}


