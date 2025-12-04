package com.cs407.badgeronsale

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseAuthHelper {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Get current user
    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    // Sign in with email and password
    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Create account with email and password
    suspend fun createAccount(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Save user profile to Firestore (matching Users entity schema)
    // Each user has a unique profile - uses userId as document ID to ensure uniqueness
    suspend fun saveUserProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        profilePicURL: String? = null,
        graduationYear: String? = null,
        address: String? = null
    ): Result<Unit> {
        return try {
            // Use userId as document ID to ensure each user has exactly one unique profile
            // Using document(userId).set() ensures only ONE profile exists per user
            val userData = hashMapOf(
                "UserID" to userId,
                "Name" to name,
                "Email" to email,
                "ProfilePicURL" to (profilePicURL ?: ""),
                "RegistrationDate" to com.google.firebase.Timestamp.now()
            )
            // Also keep phone for backward compatibility
            if (phone.isNotEmpty()) {
                userData["phone"] = phone
            }
            if (graduationYear != null && graduationYear.isNotEmpty()) {
                userData["graduationYear"] = graduationYear
            }
            if (address != null && address.isNotEmpty()) {
                userData["address"] = address
            }
            // Use set() (not merge) to ensure unique profile per user
            // This will overwrite any existing profile for this userId, ensuring exactly one profile per user
            db.collection("users").document(userId).set(userData).await()
            println("Successfully saved unique profile for user: $userId")
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error saving user profile: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Get user profile from Firestore
    suspend fun getUserProfile(userId: String): Result<Map<String, Any>> {
        return try {
            val document = db.collection("users").document(userId).get().await()
            if (document.exists()) {
                Result.success(document.data!!)
            } else {
                Result.failure(Exception("User profile not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sign out
    fun signOut() {
        auth.signOut()
    }

    // Check if user is signed in
    fun isSignedIn(): Boolean = auth.currentUser != null
}

