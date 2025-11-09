buildscript {
    dependencies {
        classpath(libs.google.services)
        classpath(libs.hilt.android.gradle.plugin)

    }
}

plugins {
    id("com.android.application") version "8.13.0" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
    id("com.google.dagger.hilt.android") version "2.57.2" apply false





}