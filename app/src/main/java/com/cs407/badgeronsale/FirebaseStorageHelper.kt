package com.cs407.badgeronsale

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

object FirebaseStorageHelper {
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Upload listing image to Firebase Storage
     * @param imageUri Local URI of the image to upload
     * @param listingId The listing ID (can be empty string for new listings)
     * @return Result containing the download URL or error
     */
    suspend fun uploadListingImage(imageUri: Uri, listingId: String = ""): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Generate unique filename
            val timestamp = System.currentTimeMillis()
            val filename = if (listingId.isNotEmpty()) {
                "listing_${listingId}_$timestamp.jpg"
            } else {
                "listing_${userId}_$timestamp.jpg"
            }
            
            val storageRef = storage.reference.child("listings/$userId/$filename")
            
            // Upload the file
            val uploadTask = storageRef.putFile(imageUri)
            val snapshot = uploadTask.await()
            
            // Get download URL
            val downloadUrl = snapshot.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload profile picture to Firebase Storage
     * @param imageUri Local URI of the image to upload
     * @return Result containing the download URL or error
     */
    suspend fun uploadProfilePicture(imageUri: Uri): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val filename = "profile_${userId}_${System.currentTimeMillis()}.jpg"
            val storageRef = storage.reference.child("profiles/$userId/$filename")
            
            // Upload the file
            val uploadTask = storageRef.putFile(imageUri)
            val snapshot = uploadTask.await()
            
            // Get download URL
            val downloadUrl = snapshot.storage.downloadUrl.await()
            
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete listing image from Firebase Storage
     * @param imageUrl The full URL of the image to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteListingImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

