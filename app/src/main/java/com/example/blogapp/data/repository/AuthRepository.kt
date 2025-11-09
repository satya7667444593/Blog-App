package com.example.blogapp.data.repository

import com.example.blogapp.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

// It's best practice to code to an interface for testability and flexibility.
interface AuthRepository {
    val currentUser: FirebaseUser?
    suspend fun signIn(email: String, password: String): Flow<Resource<FirebaseUser>>
    suspend fun signUp(email: String, password: String, displayName: String): Flow<Resource<FirebaseUser>>
    suspend fun resetPassword(email: String): Flow<Resource<Unit>>
    fun signOut()
}

// This is the implementation of the repository that Hilt will use.
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override suspend fun signIn(email: String, password: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Sign in failed: User is null"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun signUp(email: String, password: String, displayName: String): Flow<Resource<FirebaseUser>> = flow {
        emit(Resource.Loading())
        try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user
            if (user != null) {
                // Here you would typically also save the displayName to Firestore or the user's profile.
                emit(Resource.Success(user))
            } else {
                emit(Resource.Error("Sign up failed: User is null"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override suspend fun resetPassword(email: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    }
