package com.example.blogapp.ui.screens.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.blogapp.data.model.Comment
import com.example.blogapp.util.MarkdownText
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onNavigateBack: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: PostDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var commentText by remember { mutableStateOf("") }
    var showComments by remember { mutableStateOf(true) }

    LaunchedEffect(postId) {
        viewModel.loadPost(postId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleLike(postId) }) {
                        Icon(
                            if (state.isLiked) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (state.isLiked) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.toggleFavorite(postId) }) {
                        Icon(
                            if (state.isFavorited) Icons.Filled.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (state.isFavorited) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.post != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = state.post!!.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = state.post!!.authorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = formatTimestamp(state.post!!.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.post!!.likeCount} likes",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.post!!.getReadingTime()} min read",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Comment,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${state.comments.size} comments",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        if (state.post!!.categories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                state.post!!.categories.forEach { category ->
                                    SuggestionChip(
                                        onClick = { },
                                        label = { Text(category) }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (state.post!!.imageUrl.isNotEmpty()) {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = state.post!!.imageUrl,
                                    contentDescription = state.post!!.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(250.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        Card(modifier = Modifier.fillMaxWidth()) {
                            Box(modifier = Modifier.padding(16.dp)) {
                                MarkdownText(
                                    markdown = state.post!!.content,
                                    context = context,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Comments (${state.comments.size})",
                                style = MaterialTheme.typography.titleLarge
                            )
                            IconButton(onClick = { showComments = !showComments }) {
                                Icon(
                                    if (showComments) Icons.Default.ExpandLess
                                    else Icons.Default.ExpandMore,
                                    contentDescription = if (showComments) "Hide" else "Show"
                                )
                            }
                        }

                        if (showComments) {
                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    placeholder = { Text("Add a comment...") },
                                    modifier = Modifier.weight(1f),
                                    maxLines = 3
                                )
                                IconButton(
                                    onClick = {
                                        if (commentText.isNotEmpty()) {
                                            viewModel.addComment(postId, commentText)
                                            commentText = ""
                                        }
                                    },
                                    enabled = commentText.isNotEmpty()
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send")
                                }
                            }
                        }
                    }
                }

                if (showComments) {
                    items(state.comments) { comment ->
                        CommentItem(comment)
                    }

                    if (state.comments.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No comments yet. Be the first!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = comment.userName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = formatTimestamp(comment.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(timestamp.toDate())
}
