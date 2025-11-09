package com.example.blogapp.data.remote

import android.net.Uri
import androidx.room.util.copy
import com.example.blogapp.data.model.*
import com.example.blogapp.util.Constants
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.example.blogapp.data.model.Comment // <-- ADD THIS LINE
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {

    // ==================== Authentication Operations ====================

    suspend fun signUpWithEmail(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Failed to create user")
    }

    suspend fun signInWithEmail(email: String, password: String): String {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw Exception("Failed to sign in")
    }

    suspend fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    // ==================== User Operations ====================

    suspend fun createUser(user: User) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(user.userId)
            .set(user)
            .await()
    }

    suspend fun getUser(userId: String): User? {
        return firestore.collection(Constants.USERS_COLLECTION)
            .document(userId)
            .get()
            .await()
            .toObject(User::class.java)
    }

    suspend fun updateUser(user: User) {
        firestore.collection(Constants.USERS_COLLECTION)
            .document(user.userId)
            .set(user)
            .await()
    }

    // ==================== Blog Post Operations ====================

    suspend fun createPost(post: BlogPost): String {
        val docRef = firestore.collection(Constants.POSTS_COLLECTION).document()
        val postWithId = post.copy(postId = docRef.id)
        docRef.set(postWithId).await()
        return docRef.id
    }

    suspend fun updatePost(post: BlogPost) {
        firestore.collection(Constants.POSTS_COLLECTION)
            .document(post.postId)
            .set(post)
            .await()
    }

    suspend fun deletePost(postId: String) {
        firestore.collection(Constants.POSTS_COLLECTION)
            .document(postId)
            .delete()
            .await()
    }

    suspend fun getPost(postId: String): BlogPost? {
        return firestore.collection(Constants.POSTS_COLLECTION)
            .document(postId)
            .get()
            .await()
            .toObject(BlogPost::class.java)
    }

    fun getPostsFlow(): Flow<List<BlogPost>> = callbackFlow {
        val listener = firestore.collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("isPublished", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    fun getPostsByCategory(category: String): Flow<List<BlogPost>> = callbackFlow {
        val listener = firestore.collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("isPublished", true)
            .whereArrayContains("categories", category)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun searchPosts(query: String): List<BlogPost> {
        val allPosts = firestore.collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("isPublished", true)
            .get()
            .await()
            .toObjects(BlogPost::class.java)

        return allPosts.filter { post ->
            post.title.contains(query, ignoreCase = true) ||
                    post.content.contains(query, ignoreCase = true) ||
                    post.tags.any { it.contains(query, ignoreCase = true) }
        }
    }

    fun getUserPosts(userId: String): Flow<List<BlogPost>> = callbackFlow {
        val listener = firestore.collection(Constants.POSTS_COLLECTION)
            .whereEqualTo("authorId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val posts = snapshot?.toObjects(BlogPost::class.java) ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    // ==================== Like Operations ====================

    suspend fun likePost(postId: String) {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val likeId = "${userId}_$postId"

        val like = Like(
            likeId = likeId,
            userId = userId,
            postId = postId,
            createdAt = Timestamp.now()
        )

        firestore.collection(Constants.LIKES_COLLECTION)
            .document(likeId)
            .set(like)
            .await()

        // Increment like count
        firestore.collection(Constants.POSTS_COLLECTION)
            .document(postId)
            .get()
            .await()
            .toObject(BlogPost::class.java)?.let { post ->
                val updatedPost = post.copy(likeCount = post.likeCount + 1)
                updatePost(updatedPost)
            }
    }

    suspend fun unlikePost(postId: String) {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val likeId = "${userId}_$postId"

        firestore.collection(Constants.LIKES_COLLECTION)
            .document(likeId)
            .delete()
            .await()

        // Decrement like count
        firestore.collection(Constants.POSTS_COLLECTION)
            .document(postId)
            .get()
            .await()
            .toObject(BlogPost::class.java)?.let { post ->
                val updatedPost = post.copy(likeCount = (post.likeCount - 1).coerceAtLeast(0))
                updatePost(updatedPost)
            }
    }

    suspend fun isPostLiked(postId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val likeId = "${userId}_$postId"

        return firestore.collection(Constants.LIKES_COLLECTION)
            .document(likeId)
            .get()
            .await()
            .exists()
    }

    // ==================== Favorite Operations ====================

    suspend fun favoritePost(postId: String) {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val favoriteId = "${userId}_$postId"

        val favorite = Favorite(
            favoriteId = favoriteId,
            userId = userId,
            postId = postId,
            createdAt = Timestamp.now()
        )

        firestore.collection(Constants.FAVORITES_COLLECTION)
            .document(favoriteId)
            .set(favorite)
            .await()
    }

    suspend fun unfavoritePost(postId: String) {
        val userId = getCurrentUserId() ?: throw Exception("User not authenticated")
        val favoriteId = "${userId}_$postId"

        firestore.collection(Constants.FAVORITES_COLLECTION)
            .document(favoriteId)
            .delete()
            .await()
    }

    suspend fun isPostFavorited(postId: String): Boolean {
        val userId = getCurrentUserId() ?: return false
        val favoriteId = "${userId}_$postId"

        return firestore.collection(Constants.FAVORITES_COLLECTION)
            .document(favoriteId)
            .get()
            .await()
            .exists()
    }

    fun getFavoritePosts(): Flow<List<BlogPost>> = callbackFlow {
        val userId = getCurrentUserId()
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection(Constants.FAVORITES_COLLECTION)
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val postIds = snapshot?.toObjects(Favorite::class.java)
                    ?.map { it.postId } ?: emptyList()

                if (postIds.isEmpty()) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                firestore.collection(Constants.POSTS_COLLECTION)
                    .whereIn("postId", postIds.take(10))
                    .get()
                    .addOnSuccessListener { postsSnapshot ->
                        val posts = postsSnapshot.toObjects(BlogPost::class.java)
                        trySend(posts)
                    }
                    .addOnFailureListener { e ->
                        close(e)
                    }
            }
        awaitClose { listener.remove() }
    }

    // ==================== Comment Operations ====================

    suspend fun addComment(comment: Comment): String {
        val docRef = firestore.collection(Constants.COMMENTS_COLLECTION).document()
        val commentWithId = comment.copy(commentId = docRef.id)
        docRef.set(commentWithId).await()
        return docRef.id
    }

    suspend fun deleteComment(commentId: String) {
        firestore.collection(Constants.COMMENTS_COLLECTION)
            .document(commentId)
            .delete()
            .await()
    }

    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = firestore.collection(Constants.COMMENTS_COLLECTION)
            .whereEqualTo("postId", postId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    // ==================== Storage Operations ====================

    suspend fun uploadImage(uri: Uri, folder: String = Constants.IMAGES_FOLDER): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val ref = storage.reference.child("$folder/$fileName")

        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun deleteImage(imageUrl: String) {
        try {
            val ref = storage.getReferenceFromUrl(imageUrl)
            ref.delete().await()
        } catch (e: Exception) {
            // Image might not exist, ignore
        }
    }
}
