package com.example.blogapp.data.model

// In your Comment.kt model file
import com.google.firebase.Timestamp

// In Comment.kt
data class Comment(
    val commentId: String = "",
    val postId: String = "",
    val authorId: String = "", // It's likely named authorId, not userId
    val userName: String = "",
    val userImageUrl: String = "",
    val content: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val userId: String
)


