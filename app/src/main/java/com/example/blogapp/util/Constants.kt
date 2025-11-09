package com.example.blogapp.util

object Constants {
    // Firestore Collections
    const val USERS_COLLECTION = "users"
    const val POSTS_COLLECTION = "posts"
    const val LIKES_COLLECTION = "likes"
    const val FAVORITES_COLLECTION = "favorites"
    const val COMMENTS_COLLECTION = "comments"

    // Firebase Storage
    const val IMAGES_FOLDER = "images"
    const val PROFILE_IMAGES_FOLDER = "profile_images"

    // Validation
    const val MIN_PASSWORD_LENGTH = 6
    const val MAX_TITLE_LENGTH = 200
    const val MAX_EXCERPT_LENGTH = 300
    const val MAX_CONTENT_LENGTH = 50000
    const val MAX_IMAGE_SIZE_MB = 5

    // UI
    const val POSTS_PER_PAGE = 20
    const val PREVIEW_EXCERPT_LENGTH = 150

    // Categories
    val DEFAULT_CATEGORIES = listOf(
        "Technology",
        "Lifestyle",
        "Travel",
        "Food",
        "Health",
        "Business",
        "Education",
        "Entertainment",
        "Sports",
        "Science"
    )

    // Date Formats
    const val DATE_FORMAT_FULL = "MMMM dd, yyyy"
    const val DATE_FORMAT_SHORT = "MMM dd, yyyy"
    const val DATE_FORMAT_TIME = "MMM dd, yyyy HH:mm"

    // Reading Speed
    const val WORDS_PER_MINUTE = 200

    // Error Messages
    const val ERROR_NETWORK = "Network error. Please check your connection."
    const val ERROR_AUTHENTICATION = "Authentication failed. Please try again."
    const val ERROR_PERMISSION = "You don't have permission to perform this action."
    const val ERROR_NOT_FOUND = "Resource not found."
    const val ERROR_UNKNOWN = "An unknown error occurred."

    // Success Messages
    const val SUCCESS_POST_CREATED = "Post created successfully!"
    const val SUCCESS_POST_UPDATED = "Post updated successfully!"
    const val SUCCESS_POST_DELETED = "Post deleted successfully!"
    const val SUCCESS_COMMENT_ADDED = "Comment added successfully!"
    const val SUCCESS_PASSWORD_RESET = "Password reset email sent!"
}