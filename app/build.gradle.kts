import org.jetbrains.kotlin.gradle.dsl.JvmTarget
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.21" // This version should match your Kotlin version
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.blogapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.blogapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.2.21"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }


}
dependencies {
    // Core Android & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose Bill of Materials (BoM)
    implementation(platform(libs.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Firebase Bill of Materials (BoM) - Manages versions for Firebase libraries
    implementation(platform("com.google.firebase:firebase-bom:33.1.2")) // Use a specific, recent version

    // Declare Firebase library dependencies without versions
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")

    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.ktx)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Coroutines - Play Services
    implementation(libs.kotlinx.coroutines.play.services)

    // Coil for image loading
    implementation(libs.coil.compose)

    // Markdown Rich Text
    implementation(libs.richtext.ui)
    implementation(libs.richtext.markdown)

    // Glance for App Widgets
    implementation(libs.androidx.glance.appwidget)

    // Other miscellaneous dependencies from your file
    implementation(libs.material)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
    implementation("io.noties.markwon:core:4.6.2")
    val markwonVersion = "4.6.2"
    implementation("io.noties.markwon:core:$markwonVersion")
    implementation("io.noties.markwon:html:$markwonVersion") // <-- Add this line
// build.gradle.kts (Kotlin)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3") // Or the latest version
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
}



kapt {
    correctErrorTypes = true
}