package com.example.blogapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.blogapp.util.MarkdownText

@Composable
fun MarkdownEditor(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Content",
    placeholder: String = "Write your post here...",
    minHeight: Int = 300
) {
    var showPreview by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = modifier) {
        // Toolbar
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onValueChange(value + "**text**") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatBold, contentDescription = "Bold",
                            modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { onValueChange(value + "*text*") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatItalic, contentDescription = "Italic",
                            modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { onValueChange(value + "\n## ") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Title, contentDescription = "Heading",
                            modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { onValueChange(value + "\n- ") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.FormatListBulleted, contentDescription = "List",
                            modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { onValueChange(value + "[text](url)") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.Link, contentDescription = "Link",
                            modifier = Modifier.size(20.dp))
                    }
                }

                IconButton(
                    onClick = { showPreview = !showPreview },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (showPreview) Icons.Default.Edit else Icons.Default.Visibility,
                        contentDescription = if (showPreview) "Edit" else "Preview",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showPreview) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "Nothing to preview",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MarkdownText(
                            markdown = value,
                            context = context,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = minHeight.dp),
                maxLines = Int.MAX_VALUE
            )
        }
    }
}