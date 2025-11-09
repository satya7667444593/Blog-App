package com.example.blogapp.di

import com.example.blogapp.data.repository.AuthRepository
import com.example.blogapp.data.repository.AuthRepositoryImpl // Import the implementation
import com.example.blogapp.data.repository.BlogRepository
import com.example.blogapp.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule { // Changed to abstract class

    // Use @Binds to tell Hilt which implementation to use for the interface
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    // The rest of your providers can remain the same
    companion object {
        @Provides
        @Singleton
        fun provideBlogRepository(
            firestore: FirebaseFirestore,
            storage: FirebaseStorage,
            auth: FirebaseAuth
        ): BlogRepository {
            // Make sure BlogRepository is a class that can be instantiated
            return BlogRepository(firestore, storage, auth)
        }

        @Provides
        @Singleton
        fun provideUserRepository(
            firestore: FirebaseFirestore,
            auth: FirebaseAuth
        ): UserRepository {
            // Make sure UserRepository is a class that can be instantiated
            return UserRepository(firestore, auth)
        }
    }
}
