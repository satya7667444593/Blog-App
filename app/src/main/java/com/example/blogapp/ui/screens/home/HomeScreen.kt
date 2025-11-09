package com.example.blogapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.blogapp.ui.components.BlogPostCard
// Removed accompanist dependency - using pull-to-refresh with state

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToCreatePost: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToFavorites: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (showSearchBar) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchPosts(it)
                            },
                            placeholder = { Text("Search posts...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            showSearchBar = false
                            searchQuery = ""
                            viewModel.refresh()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Close search")
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("BlogApp") },
                    actions = {
                        IconButton(onClick = { showSearchBar = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = onNavigateToFavorites) {
                            Icon(Icons.Default.Favorite, contentDescription = "Favorites")
                        }
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(Icons.Default.Person, contentDescription = "Profile")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreatePost) {
                Icon(Icons.Default.Add, contentDescription = "Create Post")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.isLoading && uiState.posts.isEmpty()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.error != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = uiState.error ?: "An error occurred")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.refresh() }) {
                        Text("Retry")
                    }
                }
            } else if (uiState.posts.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No posts found")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.posts, key = { it.postId }) { post ->
                        BlogPostCard(
                            post = post,
                            onClick = { onNavigateToPostDetail(post.postId) }
                        )
                    }
                }
            }
        }
    }
}