package com.example.blogapp.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class User(
    val userId: String = "",
    val id: String = "",
    val username: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)