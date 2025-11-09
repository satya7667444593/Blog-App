// In file: app/src/main/java/com/example/blogapp/data/model/Favorite.kt

package com.example.blogapp.data.model

import androidx.annotation.Keep // <-- Make sure to import this
import com.google.firebase.Timestamp

@Keep // <-- ADD THIS ANNOTATION
data class Favorite(
    val favoriteId: String = "",
    val userId: String = "",
    val postId: String = "",
    val createdAt: Timestamp = Timestamp.now()
)
