// Top-level Gradle file for the entire project

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
//markabo
    // KSP must match Kotlin 2.0.21
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false
// Abdi 
    // Google Services plugin for Firebase
    id("com.google.gms.google-services") version "4.4.4" apply false
}
