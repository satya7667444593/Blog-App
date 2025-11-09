package com.example.blogapp.data.repository
import kotlinx.coroutines.channels.awaitClose
import androidx.compose.ui.geometry.isEmpty
import com.example.blogapp.data.model.BlogPost
import com.example.blogapp.data.model.User
import com.example.blogapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObjects
import com.google.firebase.firestore.ktx.snapshots
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import com.google.firebase.firestore.Query


class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore, // Changed to private val
    private val firebaseAuth: FirebaseAuth // Renamed for clarity
) {

    suspend fun getCurrentUser(): Resource<User> {
        return try {
            val userId = firebaseAuth.currentUser?.uid
                ?: throw Exception("User not authenticated")

            // Fetch user from Firestore instead of FirebaseAuth
            val user = getUser(userId).data ?: throw Exception("User not found in Firestore")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun getUser(userId: String): Resource<User> {
        return try {
            // Correctly fetch the user document from the "users" collection in Firestore
            val documentSnapshot = firestore.collection("users").document(userId).get().await()
            val user = documentSnapshot.toObject(User::class.java)
                ?: throw Exception("User not found")

            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user")
        }
    }

    suspend fun updateUser(user: User): Resource<Unit> {
        return try {
            // Ensure you have a UID in the user object to update the correct document
            val userId = user.id // Assuming your User model has an 'id' field for the UID
            if (userId.isEmpty()) {
                throw Exception("User ID is missing, cannot update user")
            }
            firestore.collection("users").document(userId).set(user).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update user")
        }
    }

    // In UserRepository.kt
    fun getUserPosts(userId: String): Flow<Resource<List<BlogPost>>> {
        return callbackFlow {
            val listener = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        trySend(Resource.Error(e.message ?: "Failed to fetch posts")).isSuccess
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val posts = snapshot.toObjects(BlogPost::class.java)
                        trySend(Resource.Success(posts)).isSuccess
                    }
                }
            // When the flow is cancelled, remove the listener
            awaitClose { listener.remove() }
        }.catch { e ->
            emit(Resource.Error(e.message ?: "An unexpected error occurred"))
        }
    }
}