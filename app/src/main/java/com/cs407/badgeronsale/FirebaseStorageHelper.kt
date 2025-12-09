package com.cs407.badgeronsale

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import android.util.Base64

object FirebaseStorageHelper {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Copy content URI to a temporary file that Firebase Storage can access
     * This is more reliable than trying to read from content URIs directly
     */
    private suspend fun copyUriToTempFile(context: Context, uri: Uri): File {
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
        var inputStream: InputStream? = null
        var outputStream: FileOutputStream? = null
        
        // Retry logic - sometimes the file isn't immediately available after camera saves it
        var lastException: Exception? = null
        for (attempt in 1..3) {
            try {
                // Small delay on retry to allow file system to catch up
                if (attempt > 1) {
                    kotlinx.coroutines.delay(100 * attempt.toLong()) // 100ms, 200ms delays
                }
                
                // Try to open the input stream (FileProvider URIs should already have permissions)
                inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open input stream for URI: $uri (attempt $attempt/3). The file may not exist or may not be accessible.")
                
                outputStream = FileOutputStream(tempFile)
                
                // Copy the file
                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                }
                
                outputStream.flush()
                
                // Verify the file was copied successfully
                if (totalBytes == 0L) {
                    throw Exception("File is empty or could not be read from URI: $uri")
                }
                
                if (!tempFile.exists() || tempFile.length() == 0L) {
                    throw Exception("Failed to create temp file or file is empty")
                }
                
                // Success - return the file
                return tempFile
            } catch (e: Exception) {
                lastException = e
                // Clean up on error
                try {
                    inputStream?.close()
                    inputStream = null
                } catch (closeError: Exception) {
                    // Ignore close errors
                }
                try {
                    outputStream?.close()
                    outputStream = null
                } catch (closeError: Exception) {
                    // Ignore close errors
                }
                try {
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                } catch (deleteError: Exception) {
                    // Ignore delete errors
                }
                
                // If this is the last attempt, throw the exception
                if (attempt == 3) {
                    break
                }
            }
        }
        
        // All retries failed
        throw Exception("Failed to copy file from URI $uri after 3 attempts: ${lastException?.message}", lastException)
    }
    
    /**
     * Get file from URI - handles both content:// and file:// URIs
     */
    private suspend fun getFileFromUri(context: Context, uri: Uri): File {
        return when (uri.scheme) {
            "content" -> {
                // Content URI (from FileProvider or MediaStore) - copy to temp file
                // This is the most reliable way to handle FileProvider URIs
                copyUriToTempFile(context, uri)
            }
            "file" -> {
                // File URI - use directly
                val filePath = uri.path ?: throw Exception("Invalid file path in URI: $uri")
                val file = File(filePath)
                if (!file.exists()) {
                    throw Exception("File does not exist: $filePath")
                }
                if (!file.canRead()) {
                    throw Exception("Cannot read file: $filePath")
                }
                if (file.length() == 0L) {
                    throw Exception("File is empty: $filePath")
                }
                file
            }
            else -> {
                // Try to handle as content URI anyway (some FileProvider URIs might not have "content" scheme)
                try {
                    copyUriToTempFile(context, uri)
                } catch (e: Exception) {
                    throw Exception("Unsupported URI scheme: ${uri.scheme}. Original error: ${e.message}")
                }
            }
        }
    }

    /**
     * Convert image file to base64 string
     */
    private suspend fun imageToBase64(file: File): String {
        val inputStream = file.inputStream()
        val bytes = inputStream.readBytes()
        inputStream.close()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
    
    /**
     * Upload listing image to Firestore as base64 string
     * @param context Application context for reading content URIs
     * @param imageUri Local URI of the image to upload (can be content:// or file://)
     * @param listingId The listing ID (can be empty string for new listings)
     * @return Result containing a data URL (data:image/jpeg;base64,...) or error
     */
    suspend fun uploadListingImage(context: Context, imageUri: Uri, listingId: String = ""): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        var tempFile: File? = null
        return try {
            // Get file from URI (handles content:// by copying to temp file)
            val file = getFileFromUri(context, imageUri)
            
            // Verify file exists and is readable before upload
            if (!file.exists()) {
                throw Exception("File does not exist: ${file.absolutePath}. The image may not have been saved correctly.")
            }
            if (!file.canRead()) {
                throw Exception("Cannot read file: ${file.absolutePath}. Check file permissions.")
            }
            if (file.length() == 0L) {
                throw Exception("File is empty: ${file.absolutePath}. The image may not have been saved correctly.")
            }
            
            // Check file size (Firestore document limit is 1MB, but base64 increases size by ~33%)
            // Limit to ~700KB original file size to be safe
            val maxSize = 700 * 1024 // 700KB
            if (file.length() > maxSize) {
                throw Exception("Image is too large (${file.length() / 1024}KB). Maximum size is 700KB for Firestore storage.")
            }
            
            // Mark temp files for cleanup (from cache directory)
            tempFile = if (file.parentFile?.absolutePath?.contains("cache") == true) {
                file
            } else {
                null
            }
            
            // Convert image to base64
            println("Converting image to base64...")
            val base64String = imageToBase64(file)
            println("Base64 conversion complete. Length: ${base64String.length}")
            
            // Generate unique document ID
            val timestamp = System.currentTimeMillis()
            val imageDocId = if (listingId.isNotEmpty()) {
                "listing_${listingId}_$timestamp"
            } else {
                "listing_${userId}_$timestamp"
            }
            
            // Store in Firestore under images collection
            val imageData = hashMapOf(
                "userId" to userId,
                "listingId" to (listingId.ifEmpty { "" }),
                "base64Data" to base64String,
                "contentType" to "image/jpeg",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "size" to file.length()
            )
            
            println("Saving image to Firestore: $imageDocId")
            db.collection("images").document(imageDocId).set(imageData).await()
            println("Image saved to Firestore successfully")
            
            // Return data URL format that can be used directly in Image components
            val dataUrl = "data:image/jpeg;base64,$base64String"
            println("Returning data URL (length: ${dataUrl.length})")
            Result.success(dataUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}", e))
        } finally {
            // Clean up temp file
            try {
                tempFile?.delete()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Upload profile picture to Firestore as base64 string
     * @param context Application context for reading content URIs
     * @param imageUri Local URI of the image to upload (can be content:// or file://)
     * @return Result containing a data URL (data:image/jpeg;base64,...) or error
     */
    suspend fun uploadProfilePicture(context: Context, imageUri: Uri): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        var tempFile: File? = null
        return try {
            // Get file from URI (handles content:// by copying to temp file)
            val file = getFileFromUri(context, imageUri)
            
            // Verify file exists and is readable before upload
            if (!file.exists()) {
                throw Exception("File does not exist: ${file.absolutePath}. The image may not have been saved correctly.")
            }
            if (!file.canRead()) {
                throw Exception("Cannot read file: ${file.absolutePath}. Check file permissions.")
            }
            if (file.length() == 0L) {
                throw Exception("File is empty: ${file.absolutePath}. The image may not have been saved correctly.")
            }
            
            // Check file size (Firestore document limit is 1MB, but base64 increases size by ~33%)
            // Limit to ~700KB original file size to be safe
            val maxSize = 700 * 1024 // 700KB
            val fileSizeKB = file.length() / 1024
            if (file.length() > maxSize) {
                throw Exception("Image is too large (${fileSizeKB}KB). Maximum size is 700KB. Please choose a smaller image or compress it.")
            }
            
            // Log file info for debugging
            println("Uploading profile picture: size=${fileSizeKB}KB, path=${file.absolutePath}")
            
            // Mark temp files for cleanup (from cache directory)
            tempFile = if (file.parentFile?.absolutePath?.contains("cache") == true) {
                file
            } else {
                null
            }
            
            // Convert image to base64
            println("Converting profile picture to base64...")
            val base64String = imageToBase64(file)
            println("Base64 conversion complete. Length: ${base64String.length}")
            
            // Generate unique document ID
            val timestamp = System.currentTimeMillis()
            val imageDocId = "profile_${userId}_$timestamp"
            
            // Store in Firestore under images collection
            val imageData = hashMapOf(
                "userId" to userId,
                "listingId" to "",
                "base64Data" to base64String,
                "contentType" to "image/jpeg",
                "createdAt" to com.google.firebase.Timestamp.now(),
                "size" to file.length(),
                "isProfilePicture" to true
            )
            
            db.collection("images").document(imageDocId).set(imageData).await()
            
            // Also update user profile with the data URL
            val dataUrl = "data:image/jpeg;base64,$base64String"
            db.collection("users").document(userId)
                .update("ProfilePicURL", dataUrl)
                .await()
            
            Result.success(dataUrl)
        } catch (e: Exception) {
            Result.failure(Exception("Upload failed: ${e.message}", e))
        } finally {
            // Clean up temp file
            try {
                tempFile?.delete()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }

    /**
     * Delete listing image from Firestore
     * @param imageUrl The data URL of the image to delete (data:image/jpeg;base64,...)
     * @return Result indicating success or failure
     */
    suspend fun deleteListingImage(imageUrl: String): Result<Unit> {
        return try {
            // Extract base64 data from data URL
            if (!imageUrl.startsWith("data:image")) {
                // If it's not a data URL, it might be an old Storage URL - just return success
                return Result.success(Unit)
            }
            
            val base64Data = imageUrl.substringAfter("base64,")
            
            // Find and delete the image document
            val query = db.collection("images")
                .whereEqualTo("base64Data", base64Data)
                .limit(1)
                .get()
                .await()
            
            if (!query.isEmpty) {
                val doc = query.documents[0]
                doc.reference.delete().await()
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

