package com.example.blogapp.ui.screens.create

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.blogapp.ui.components.MarkdownEditor
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    postId: String? = null,
    viewModel: CreatePostViewModel = hiltViewModel()
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var excerpt by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isPublished by remember { mutableStateOf(false) }
    var showMarkdownHelp by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            onNavigateBack()
            viewModel.resetState()
        }
    }

    if (showMarkdownHelp) {
        AlertDialog(
            onDismissRequest = { showMarkdownHelp = false },
            title = { Text("Markdown Guide") },
            text = {
                Column {
                    Text("**Bold** → Bold text")
                    Text("*Italic* → Italic text")
                    Text("# Heading → Large heading")
                    Text("## Subheading → Medium heading")
                    Text("- Item → Bullet point")
                    Text("1. Item → Numbered list")
                    Text("[Link](url) → Hyperlink")
                    Text("`code` → Inline code")
                }
            },
            confirmButton = {
                TextButton(onClick = { showMarkdownHelp = false }) {
                    Text("Got it")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (postId == null) "Create Post" else "Edit Post") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showMarkdownHelp = true }) {
                        Icon(Icons.Default.Help, contentDescription = "Help")
                    }

                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp).padding(end = 16.dp)
                        )
                    } else {
                        TextButton(
                            onClick = {
                                val categoryList = categories.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }
                                val tagList = tags.split(",")
                                    .map { it.trim() }
                                    .filter { it.isNotEmpty() }

                                if (postId == null) {
                                    viewModel.createPost(
                                        title = title,
                                        content = content,
                                        excerpt = excerpt,
                                        authorName = currentUser?.displayName ?: "Anonymous",
                                        categories = categoryList,
                                        tags = tagList,
                                        imageUri = imageUri,
                                        isPublished = isPublished
                                    )
                                } else {
                                    viewModel.updatePost(
                                        postId = postId,
                                        title = title,
                                        content = content,
                                        excerpt = excerpt,
                                        authorName = currentUser?.displayName ?: "Anonymous",
                                        categories = categoryList,
                                        tags = tagList,
                                        imageUri = imageUri,
                                        currentImageUrl = "",
                                        isPublished = isPublished
                                    )
                                }
                            },
                            enabled = title.isNotEmpty() && content.isNotEmpty()
                        ) {
                            Text(if (isPublished) "Publish" else "Save Draft")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                placeholder = { Text("Enter post title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = title.isEmpty()
            )

            OutlinedTextField(
                value = excerpt,
                onValueChange = { excerpt = it },
                label = { Text("Excerpt (Optional)") },
                placeholder = { Text("Short description") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            MarkdownEditor(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth(),
                minHeight = 300
            )

            OutlinedTextField(
                value = categories,
                onValueChange = { categories = it },
                label = { Text("Categories") },
                placeholder = { Text("Technology, Lifestyle (comma-separated)") },
                leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags") },
                placeholder = { Text("android, kotlin (comma-separated)") },
                leadingIcon = { Icon(Icons.Default.Tag, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Featured Image", style = MaterialTheme.typography.titleMedium)
                        if (imageUri != null) {
                            IconButton(
                                onClick = { imageUri = null },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove")
                            }
                        }
                    }

                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Selected image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (imageUri == null) "Select Image" else "Change Image")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Publish immediately", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            if (isPublished) "Post will be public" else "Save as draft",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPublished,
                        onCheckedChange = { isPublished = it }
                    )
                }
            }

            state.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}