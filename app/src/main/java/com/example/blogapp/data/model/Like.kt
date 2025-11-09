package com.example.blogapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Like(
    @DocumentId
    val likeId: String = "",
    val userId: String = "",
    val postId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)