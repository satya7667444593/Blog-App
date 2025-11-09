// File: app/src/main/java/com/example/blogapp/data/repository/BlogRepository.kt

package com.example.blogapp.data.repository

import android.net.Uri
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.model.Comment
import com.example.blogapp.data.model.Like
import com.example.blogapp.data.model.Favorite
import com.example.blogapp.util.Resource
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class BlogRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) {

    // ==================== CREATE POST ====================
    suspend fun createPost(post: BlogPost, imageUri: Uri?): Resource<String> {
        return try {
            val imageUrl = imageUri?.let { uploadImage(it) } ?: ""

            val newPost = post.copy(
                imageUrl = imageUrl,
                authorId = auth.currentUser?.uid ?: "",
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            val postRef = firestore.collection("posts").document()
            postRef.set(newPost.copy(postId = postRef.id)).await()
            Resource.Success(postRef.id)

        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to create post")
        }
    }

    // ==================== UPLOAD IMAGE ====================
    private suspend fun uploadImage(imageUri: Uri): String {
        val storageRef = storage.reference.child("blog_images/${UUID.randomUUID()}")
        val uploadTask = storageRef.putFile(imageUri).await()
        return uploadTask.storage.downloadUrl.await().toString()
    }

    // ==================== UPDATE POST ====================
    suspend fun updatePost(postId: String, post: BlogPost, imageUri: Uri?): Resource<Unit> {
        return try {
            val imageUrl = imageUri?.let { uploadImage(it) } ?: post.imageUrl

            val postData = mapOf(
                "title" to post.title,
                "content" to post.content,
                "excerpt" to post.excerpt,
                "categories" to post.categories,
                "tags" to post.tags,
                "imageUrl" to imageUrl,
                "isPublished" to post.isPublished,
                "updatedAt" to Timestamp.now()
            )

            firestore.collection("posts").document(postId).update(postData).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update post")
        }
    }

    // ==================== DELETE POST ====================
    suspend fun deletePost(postId: String): Resource<Unit> {
        return try {
            firestore.collection("posts").document(postId).delete().await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete post")
        }
    }

    // ==================== GET POSTS FLOW ====================
    fun getPostsFlow(): Flow<Resource<List<BlogPost>>> {
        return callbackFlow {
            val listener = firestore.collection("posts")
                .whereEqualTo("isPublished", true)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(Resource.Error(e.message ?: "Failed to fetch posts"))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val posts = snapshot.toObjects(BlogPost::class.java)
                        trySend(Resource.Success(posts))
                    }
                }
            awaitClose { listener.remove() }
        }.catch { e -> emit(Resource.Error(e.message ?: "Failed to fetch posts")) }
    }

    // ==================== GET POSTS BY CATEGORY ====================
    fun getPostsByCategory(category: String): Flow<Resource<List<BlogPost>>> {
        return callbackFlow {
            val listener = firestore.collection("posts")
                .whereEqualTo("isPublished", true)
                .whereArrayContains("categories", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(Resource.Error(e.message ?: "Failed to fetch posts"))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val posts = snapshot.toObjects(BlogPost::class.java)
                        trySend(Resource.Success(posts))
                    }
                }
            awaitClose { listener.remove() }
        }.catch { e -> emit(Resource.Error(e.message ?: "Failed to fetch posts")) }
    }

    // ==================== SEARCH POSTS ====================
    suspend fun searchPosts(query: String): Resource<List<BlogPost>> {
        return try {
            val allPosts = firestore.collection("posts")
                .whereEqualTo("isPublished", true)
                .get()
                .await()
                .toObjects(BlogPost::class.java)

            val filteredPosts = allPosts.filter { post ->
                post.title.contains(query, ignoreCase = true) ||
                        post.content.contains(query, ignoreCase = true) ||
                        post.tags.any { it.contains(query, ignoreCase = true) }
            }

            Resource.Success(filteredPosts)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to search posts")
        }
    }

    // ==================== GET POST BY ID ====================
    fun getPostById(postId: String): Flow<Resource<BlogPost>> {
        return flow {
            try {
                val snapshot = firestore.collection("posts")
                    .document(postId)
                    .get()
                    .await()

                val post = snapshot.toObject(BlogPost::class.java)
                if (post != null) {
                    emit(Resource.Success(post))
                } else {
                    emit(Resource.Error("Post not found"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "Failed to fetch post"))
            }
        }
    }

    // ==================== LIKE POST ====================
    suspend fun likePost(postId: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val likeId = "${userId}_$postId"

            val like = Like(
                likeId = likeId,
                userId = userId,
                postId = postId,
                createdAt = Timestamp.now()
            )

            // Add like
            firestore.collection("likes")
                .document(likeId)
                .set(like)
                .await()

            // Increment like count
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0
                transaction.update(postRef, "likeCount", currentLikes + 1)
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to like post")
        }
    }

    // ==================== UNLIKE POST ====================
    suspend fun unlikePost(postId: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val likeId = "${userId}_$postId"

            // Remove like
            firestore.collection("likes")
                .document(likeId)
                .delete()
                .await()

            // Decrement like count
            val postRef = firestore.collection("posts").document(postId)
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val currentLikes = snapshot.getLong("likeCount") ?: 0
                transaction.update(postRef, "likeCount", (currentLikes - 1).coerceAtLeast(0))
            }.await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unlike post")
        }
    }

    // ==================== IS POST LIKED ====================
    suspend fun isPostLiked(postId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val likeId = "${userId}_$postId"

            firestore.collection("likes")
                .document(likeId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }

    // ==================== FAVORITE POST ====================
    suspend fun favoritePost(postId: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val favoriteId = "${userId}_$postId"

            val favorite = Favorite(
                favoriteId = favoriteId,
                userId = userId,
                postId = postId,
                createdAt = Timestamp.now()
            )

            firestore.collection("favorites")
                .document(favoriteId)
                .set(favorite)
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to favorite post")
        }
    }

    // ==================== UNFAVORITE POST ====================
    suspend fun unfavoritePost(postId: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")
            val favoriteId = "${userId}_$postId"

            firestore.collection("favorites")
                .document(favoriteId)
                .delete()
                .await()

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to unfavorite post")
        }
    }

    // ==================== IS POST FAVORITED ====================
    suspend fun isPostFavorited(postId: String): Boolean {
        return try {
            val userId = auth.currentUser?.uid ?: return false
            val favoriteId = "${userId}_$postId"

            firestore.collection("favorites")
                .document(favoriteId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }

    // ==================== GET FAVORITE POSTS ====================
    // ==================== GET FAVORITE POSTS ====================
    fun getFavoritePosts(): Flow<Resource<List<BlogPost>>> {
        return callbackFlow {
            val userId = auth.currentUser?.uid
            if (userId == null) {
                // If the user is not logged in, send an empty list and close the flow.
                trySend(Resource.Success(emptyList()))
                awaitClose { }
                return@callbackFlow
            }

            // Listen for changes in the user's favorites collection.
            val favoritesListener = firestore.collection("favorites")
                .whereEqualTo("userId", userId) // This line is now correct
                .addSnapshotListener { favoritesSnapshot, e ->
                    if (e != null) {
                        trySend(Resource.Error(e.message ?: "Failed to fetch favorites"))
                        return@addSnapshotListener
                    }

                    if (favoritesSnapshot == null || favoritesSnapshot.isEmpty) {
                        trySend(Resource.Success(emptyList()))
                        return@addSnapshotListener
                    }

                    val postIds = favoritesSnapshot.documents.map { it.getString("postId") }
                    if (postIds.isNotEmpty()) {
                        firestore.collection("posts")
                            .whereIn("postId", postIds)
                            .get()
                            .addOnSuccessListener { postsSnapshot ->
                                val posts = postsSnapshot.toObjects(BlogPost::class.java)
                                trySend(Resource.Success(posts))
                            }
                            .addOnFailureListener { exception ->
                                trySend(Resource.Error(exception.message ?: "Failed to fetch bookmarked posts"))
                            }
                    } else {
                        trySend(Resource.Success(emptyList()))
                    }
                }
            awaitClose { favoritesListener.remove() }
        }
    }


    // ==================== ADD COMMENT ====================
    suspend fun addComment(postId: String, content: String): Resource<Unit> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not authenticated")

            // Fetch user details
            val userDoc = firestore.collection("users").document(userId).get().await()
            val userName = userDoc.getString("displayName") ?: "Anonymous"
            val userImageUrl = userDoc.getString("profileImageUrl") ?: ""

            val commentRef = firestore.collection("comments").document()
            val comment = Comment(
                commentId = commentRef.id,
                postId = postId,
                userName = userName,
                userImageUrl = userImageUrl,
                content = content,
                authorId = TODO(),
                createdAt = TODO(),
                userId = TODO(),
            )

            commentRef.set(comment).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to add comment")
        }
    }

    // ==================== GET COMMENTS ====================
    fun getComments(postId: String): Flow<Resource<List<Comment>>> {
        return callbackFlow {
            val listener = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch comments"))
                        return@addSnapshotListener
                    }

                    val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                    trySend(Resource.Success(comments))
                }
            awaitClose { listener.remove() }
        }.catch { e -> emit(Resource.Error(e.message ?: "Failed to fetch comments")) }
    }

    // ==================== GET USER POSTS ====================
    fun getUserPosts(userId: String): Flow<Resource<List<BlogPost>>> {
        return callbackFlow {
            val listener = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(Resource.Error(error.message ?: "Failed to fetch user posts"))
                        return@addSnapshotListener
                    }

                    val posts = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                    trySend(Resource.Success(posts))
                }
            awaitClose { listener.remove() }
        }.catch { e -> emit(Resource.Error(e.message ?: "Failed to fetch user posts")) }
    }
}