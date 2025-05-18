package com.pam.flashlearn.ui.album

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.pam.flashlearn.ui.components.BottomNavBar
import com.pam.flashlearn.ui.components.SetItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    viewModel: AlbumViewModel,
    onNavigateToSet: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToCreateSet: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    var refreshTrigger by remember { mutableStateOf(0) }

    // Force reload data when screen is shown or refresh is triggered
    LaunchedEffect(refreshTrigger) {
        println("Loading all sets (refresh: $refreshTrigger)")
        viewModel.loadAllSets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Flashcard Sets") },
                actions = {
                    // Add refresh button
                    IconButton(onClick = { refreshTrigger++ }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        bottomBar = {
            BottomNavBar(
                currentRoute = "album",
                onNavItemClick = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "album" -> { /* Already on album */ }
                        "create_set" -> onNavigateToCreateSet()
                        "profile" -> onNavigateToProfile()
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreateSet) {
                Icon(Icons.Default.Add, contentDescription = "Create New Set")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Debug info
            println("Sets in viewModel: ${viewModel.sets.size}")
            viewModel.sets.forEach { set ->
                println("Set in UI: ${set.id}, Title: ${set.title}, Cards: ${set.cardCount}")
            }

            if (viewModel.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.sets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No flashcard sets yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(onClick = onNavigateToCreateSet) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.padding(4.dp))
                            Text("Create Your First Set")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(viewModel.sets) { set ->
                        SetItem(
                            set = set,
                            onClick = { onNavigateToSet(set.id) }
                        )
                    }
                }
            }
        }
    }
}
