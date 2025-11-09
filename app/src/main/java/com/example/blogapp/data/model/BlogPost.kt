// In your BlogPost.kt file (e.g., app/src/main/java/com/example/blogapp/data/model/BlogPost.kt)
// File: app/src/main/java/com/example/blogapp/data/model/BlogPost.kt

package com.example.blogapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class BlogPost(
    @DocumentId
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",  // Added: Author's display name
    val title: String = "",
    val content: String = "",
    val excerpt: String = "",  // Added: Short description/preview
    val imageUrl: String = "",
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val isPublished: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Calculate reading time based on average reading speed
     * Average reading speed: 200 words per minute
     */
    fun getReadingTime(): Int {
        val wordsPerMinute = 200
        val wordCount = content.split("\\s+".toRegex()).size
        return (wordCount / wordsPerMinute).coerceAtLeast(1)
    }

    /**
     * Generate excerpt from content if not provided
     */
    fun getExcerptOrGenerate(maxLength: Int = 150): String {
        return if (excerpt.isNotEmpty()) {
            excerpt
        } else {
            val plainText = content
                .replace(Regex("#+ "), "") // Remove headers
                .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // Remove bold
                .replace(Regex("\\*([^*]+)\\*"), "$1") // Remove italic
                .replace(Regex("\\[([^]]+)\\]\\([^)]+\\)"), "$1") // Remove links
                .trim()

            if (plainText.length > maxLength) {
                plainText.take(maxLength) + "..."
            } else {
                plainText
            }
        }
    }

    /**
     * Check if post is a draft
     */
    fun isDraft(): Boolean = !isPublished

    /**
     * Format post for display
     */
    fun getFormattedDate(): String {
        return java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
            .format(createdAt.toDate())
    }
}