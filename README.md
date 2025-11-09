# BlogApp - Android Blogging Platform

A modern Android blogging platform built with Jetpack Compose, Firebase, and MVVM architecture.

## 📋 Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/blogapp/
│   │   │   ├── BlogApplication.kt
│   │   │   ├── di/
│   │   │   │   ├── AppModule.kt
│   │   │   │   └── FirebaseModule.kt
│   │   │   ├── data/
│   │   │   │   ├── model/
│   │   │   │   │   ├── User.kt
│   │   │   │   │   ├── BlogPost.kt
│   │   │   │   │   ├── Like.kt
│   │   │   │   │   └── Favorite.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── AuthRepository.kt
│   │   │   │   │   ├── BlogRepository.kt
│   │   │   │   │   └── UserRepository.kt
│   │   │   │   └── remote/
│   │   │   │       └── FirebaseService.kt
│   │   │   ├── ui/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── navigation/
│   │   │   │   │   └── NavGraph.kt
│   │   │   │   ├── theme/
│   │   │   │   │   ├── Color.kt
│   │   │   │   │   ├── Theme.kt
│   │   │   │   │   └── Type.kt
│   │   │   │   ├── screens/
│   │   │   │   │   ├── auth/
│   │   │   │   │   │   ├── LoginScreen.kt
│   │   │   │   │   │   ├── SignUpScreen.kt
│   │   │   │   │   │   └── AuthViewModel.kt
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   │   └── HomeViewModel.kt
│   │   │   │   │   ├── create/
│   │   │   │   │   │   ├── CreatePostScreen.kt
│   │   │   │   │   │   └── CreatePostViewModel.kt
│   │   │   │   │   ├── detail/
│   │   │   │   │   │   ├── PostDetailScreen.kt
│   │   │   │   │   │   └── PostDetailViewModel.kt
│   │   │   │   │   ├── profile/
│   │   │   │   │   │   ├── ProfileScreen.kt
│   │   │   │   │   │   └── ProfileViewModel.kt
│   │   │   │   │   └── favorites/
│   │   │   │   │       ├── FavoritesScreen.kt
│   │   │   │   │       └── FavoritesViewModel.kt
│   │   │   │   └── components/
│   │   │   │       ├── BlogPostCard.kt
│   │   │   │       ├── MarkdownEditor.kt
│   │   │   │       └── LoadingIndicator.kt
│   │   │   └── util/
│   │   │       ├── Constants.kt
│   │   │       ├── Resource.kt
│   │   │       └── MarkdownRenderer.kt
│   │   └── res/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   │       └── drawable/
│   └── build.gradle.kts
├── build.gradle.kts
└── google-services.json
```

## 🚀 Features Implemented

### Core Features
✅ User Authentication (Firebase Auth)
- Email/password sign-up
- Email/password login
- Password reset
- Session persistence

✅ Blog Post Management
- Create posts with title, content, images
- Edit existing posts
- Delete posts
- Markdown support for rich text
- Draft saving
- Publish/unpublish functionality

✅ Blog Feed & Discovery
- Recent posts display
- Category filtering
- Search functionality
- Pull-to-refresh
- Post previews

✅ Blog Interaction
- View full posts
- Like/unlike posts
- Favorite/unfavorite posts
- View liked/favorited posts
- Like count display

✅ Data Management
- Firestore integration
- Real-time updates
- Offline support

✅ Architecture
- MVVM pattern
- Jetpack Compose UI
- Hilt dependency injection

### Bonus Features
🎁 Comment System
🎁 Push Notifications
🎁 Dark Mode
🎁 Share Functionality
🎁 Reading Time Estimate

## 🛠 Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM
- **Dependency Injection**: Hilt
- **Authentication**: Firebase Auth
- **Database**: Cloud Firestore
- **Storage**: Firebase Storage
- **Markdown**: Markwon
- **Image Loading**: Coil
- **Async**: Coroutines & Flow

## 📦 Dependencies

```kotlin
// Firebase
implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.firebase:firebase-firestore-ktx")
implementation("com.google.firebase:firebase-storage-ktx")

// Jetpack Compose
implementation("androidx.compose.ui:ui:1.5.4")
implementation("androidx.compose.material3:material3:1.1.2")
implementation("androidx.navigation:navigation-compose:2.7.5")

// Hilt
implementation("com.google.dagger:hilt-android:2.48")
kapt("com.google.dagger:hilt-compiler:2.48")
implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

// Markdown
implementation("io.noties.markwon:core:4.6.2")
implementation("io.noties.markwon:editor:4.6.2")

// Coil for image loading
implementation("io.coil-kt:coil-compose:2.5.0")
```

## 🔧 Setup Instructions

### 1. Prerequisites
- Android Studio Hedgehog or later
- JDK 17 or later
- Android SDK 24+

### 2. Firebase Setup
1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app to your Firebase project
3. Download `google-services.json` and place it in `app/` directory
4. Enable Email/Password authentication in Firebase Console
5. Create a Firestore database
6. Set up Firebase Storage

### 3. Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    match /posts/{postId} {
      allow read: if resource.data.isPublished == true || request.auth.uid == resource.data.authorId;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.authorId;
    }
    
    match /likes/{likeId} {
      allow read: if request.auth != null;
      allow create, delete: if request.auth.uid == request.resource.data.userId;
    }
    
    match /favorites/{favoriteId} {
      allow read: if request.auth != null;
      allow create, delete: if request.auth.uid == request.resource.data.userId;
    }
  }
}
```

### 4. Build & Run
```bash
# Clone the repository
git clone https://github.com/yourusername/blogapp.git

# Open in Android Studio
# Sync Gradle files
# Run on emulator or device
```

## 📱 App Screenshots

### Authentication Flow
- Login Screen
- Sign Up Screen
- Password Reset

### Main Features
- Home Feed with blog posts
- Create/Edit Post Screen
- Post Detail View
- User Profile
- Favorites Collection

## 🏗 Architecture Overview

### MVVM Pattern
```
View (Composables) ← ViewModel ← Repository ← Data Source (Firebase)
```

### Data Flow
1. User interacts with UI (View)
2. View calls ViewModel methods
3. ViewModel calls Repository methods
4. Repository interacts with Firebase
5. Data flows back through LiveData/StateFlow
6. UI updates automatically

## 📝 Data Models

### User
```kotlin
data class User(
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

### BlogPost
```kotlin
data class BlogPost(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val title: String = "",
    val content: String = "",
    val excerpt: String = "",
    val imageUrl: String = "",
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val likeCount: Int = 0,
    val isPublished: Boolean = false,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
)
```

## 🐛 Known Issues
- Image upload may take time on slow connections
- Markdown preview updates with slight delay
- Search is case-sensitive

## 🔮 Future Enhancements
- Offline mode with sync
- Rich media embeds (YouTube, Twitter)
- Multi-language support
- Advanced analytics
- Social sharing improvements

## 📄 License
MIT License

## 👥 Contributors
Your Name - [GitHub](https://github.com/satya7667444593)

## 📞 Support
For issues and questions, please open an issue on GitHub.
