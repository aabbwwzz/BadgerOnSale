package com.cs407.badgeronsale.repository

import com.cs407.badgeronsale.Listing
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.Deferred

/**
 * FavoritesRepository - Manages Favorites in Firestore
 * 
 * Database Relationships:
 * - Users ↔ Favorites ↔ Listings: Many-to-Many
 *   * A User can favorite multiple Listings
 *   * Each Listing can be favorited by multiple Users
 *   * The Favorites entity acts as a junction table
 *   * Favorites contains: FavoriteID (PK), UserID (FK → Users), ListingID (FK → Listings)
 */
object FavoritesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val COLLECTION_FAVORITES = "favorites"
    private const val COLLECTION_LISTINGS = "listings"

    // Get user's favorites as Flow with real-time updates (matching Favorites entity schema: FavoriteID, UserID, ListingID)
    fun getUserFavorites(): Flow<List<Listing>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Set up real-time listener for favorites
        val listenerRegistration = db.collection(COLLECTION_FAVORITES)
            .whereEqualTo("UserID", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error in favorites listener: ${error.message}")
                    try {
                        trySend(emptyList())
                    } catch (e: Exception) {
                        // Channel might be closed, ignore
                    }
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    try {
                        trySend(emptyList())
                    } catch (e: Exception) {
                        // Channel might be closed, ignore
                    }
                    return@addSnapshotListener
                }

                // Check for document changes - if documents were deleted, handle them
                if (!snapshot.documentChanges.isEmpty()) {
                    val deletedIds = snapshot.documentChanges
                        .filter { it.type == com.google.firebase.firestore.DocumentChange.Type.REMOVED }
                        .mapNotNull { it.document.getString("ListingID")?.trim() }
                        .filter { it.isNotEmpty() }
                    
                    if (deletedIds.isNotEmpty()) {
                        println("Detected ${deletedIds.size} favorite deletion(s): $deletedIds")
                    }
                }

                // Get listing IDs from favorites (using ListingID field from schema)
                // Only use documents that actually exist (not deleted)
                // Remove duplicates and empty strings - use a Set to ensure uniqueness
                val listingIds = snapshot.documents
                    .mapNotNull { doc ->
                        // Double-check the document still exists
                        if (doc.exists()) {
                            doc.getString("ListingID")?.trim()
                        } else {
                            null
                        }
                    }
                    .filter { it.isNotEmpty() }
                    .toSet() // Use Set to ensure no duplicates

                if (listingIds.isEmpty()) {
                    try {
                        trySend(emptyList())
                    } catch (e: Exception) {
                        // Channel might be closed, ignore
                    }
                    return@addSnapshotListener
                }

                // Fetch actual listings asynchronously using the callbackFlow's scope
                launch {
                    try {
                        val listings = mutableListOf<Listing>()
                        val processedIds = mutableSetOf<String>() // Track processed IDs to avoid duplicates
                        
                        for (listingId in listingIds) {
                            try {
                                if (listingId.isNotEmpty() && !processedIds.contains(listingId)) {
                                    processedIds.add(listingId)
                                    
                                    val listingDoc = db.collection(COLLECTION_LISTINGS)
                                        .document(listingId)
                                        .get()
                                        .await()
                                    
                                    if (listingDoc.exists()) {
                                        val data = listingDoc.data ?: continue
                                        val listing = documentToListing(listingId, data)
                                        if (listing != null) {
                                            // Only add if not already in list (extra safety check)
                                            if (!listings.any { it.id == listingId }) {
                                                listings.add(listing)
                                            }
                                        }
                                    } else {
                                        // Listing doesn't exist - clean up the favorite entry
                                        println("Listing $listingId doesn't exist, cleaning up favorite entry")
                                        try {
                                            val favoriteDocs = db.collection(COLLECTION_FAVORITES)
                                                .whereEqualTo("UserID", userId)
                                                .whereEqualTo("ListingID", listingId)
                                                .get()
                                                .await()
                                            
                                            for (favDoc in favoriteDocs.documents) {
                                                favDoc.reference.delete().await()
                                            }
                                        } catch (cleanupError: Exception) {
                                            println("Error cleaning up favorite for deleted listing: ${cleanupError.message}")
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip this listing if it fails to load
                                println("Error loading listing $listingId: ${e.message}")
                                continue
                            }
                        }
                        
                        // Final check: ensure no duplicates by ID
                        val uniqueListings = listings.distinctBy { it.id }
                        
                        try {
                            trySend(uniqueListings)
                        } catch (e: Exception) {
                            // Channel might be closed, ignore
                        }
                    } catch (e: Exception) {
                        try {
                            trySend(emptyList())
                        } catch (ex: Exception) {
                            // Channel might be closed, ignore
                        }
                    }
                }
            }

        awaitClose { 
            try {
                listenerRegistration.remove()
            } catch (e: Exception) {
                // Ignore errors on cleanup
            }
        }
    }

    // Add listing to favorites (matching Favorites entity schema)
    suspend fun addToFavorites(listingId: String): Result<String> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Check if already favorited - also clean up any duplicates
            val existing = db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("UserID", userId)
                .whereEqualTo("ListingID", listingId)
                .get()
                .await()
            
            if (existing.isEmpty) {
                // Create favorite with schema fields: FavoriteID (auto-generated), UserID, ListingID
                val favoriteData = hashMapOf(
                    "UserID" to userId,
                    "ListingID" to listingId
                )
                val docRef = db.collection(COLLECTION_FAVORITES).add(favoriteData).await()
                Result.success(docRef.id) // Return FavoriteID
            } else {
                // Already favorited - if there are duplicates, keep only the first one and delete the rest
                val docs = existing.documents
                if (docs.size > 1) {
                    // Delete duplicates, keep only the first one
                    for (i in 1 until docs.size) {
                        docs[i].reference.delete().await()
                    }
                }
                Result.success(docs.first().id)
            }
        } catch (e: Exception) {
            println("Error adding to favorites: ${e.message}")
            Result.failure(e)
        }
    }

    // Remove listing from favorites - removes ALL duplicates with retry logic
    suspend fun removeFromFavorites(listingId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            if (listingId.isEmpty()) {
                println("Cannot delete favorite: listingId is empty")
                return Result.failure(Exception("Invalid listing ID"))
            }
            
            println("Attempting to delete favorite for listing: $listingId, user: $userId")
            
            // Try multiple deletion attempts to ensure all duplicates are removed
            var maxRetries = 3
            var allDeleted = false
            
            while (maxRetries > 0 && !allDeleted) {
                // Find ALL favorite documents for this user and listing
                val snapshot = db.collection(COLLECTION_FAVORITES)
                    .whereEqualTo("UserID", userId)
                    .whereEqualTo("ListingID", listingId)
                    .get()
                    .await()
                
                if (snapshot.isEmpty) {
                    // Also try with trimmed listingId in case of whitespace issues
                    val trimmedId = listingId.trim()
                    if (trimmedId != listingId) {
                        val trimmedSnapshot = db.collection(COLLECTION_FAVORITES)
                            .whereEqualTo("UserID", userId)
                            .whereEqualTo("ListingID", trimmedId)
                            .get()
                            .await()
                        
                        if (trimmedSnapshot.isEmpty) {
                            println("No favorites found to delete for listing: $listingId (already deleted)")
                            allDeleted = true
                            break
                        } else {
                            // Delete trimmed version
                            for (doc in trimmedSnapshot.documents) {
                                try {
                                    println("Deleting favorite document (trimmed): ${doc.id}, ListingID: ${doc.getString("ListingID")}")
                                    doc.reference.delete().await()
                                } catch (e: Exception) {
                                    println("Error deleting favorite document ${doc.id}: ${e.message}")
                                }
                            }
                        }
                    } else {
                        println("No favorites found to delete for listing: $listingId (already deleted)")
                        allDeleted = true
                        break
                    }
                } else {
                    println("Found ${snapshot.documents.size} favorite document(s) to delete for listing: $listingId (attempt ${4 - maxRetries})")
                    
                    // Delete ALL matching documents using batch delete for better performance
                    val batch = db.batch()
                    snapshot.documents.forEach { doc ->
                        try {
                            println("Marking favorite document for deletion: ${doc.id}, ListingID: ${doc.getString("ListingID")}")
                            batch.delete(doc.reference)
                        } catch (e: Exception) {
                            println("Error adding to batch delete: ${e.message}")
                        }
                    }
                    
                    // Commit batch deletion
                    try {
                        batch.commit().await()
                        println("Batch deletion committed successfully")
                    } catch (e: Exception) {
                        println("Error committing batch deletion: ${e.message}")
                        // Fallback to individual deletions
                        for (doc in snapshot.documents) {
                            try {
                                doc.reference.delete().await()
                                println("Successfully deleted favorite document: ${doc.id}")
                            } catch (ex: Exception) {
                                println("Error deleting favorite document ${doc.id}: ${ex.message}")
                            }
                        }
                    }
                }
                
                // Verify deletion with a small delay to allow Firestore to process
                kotlinx.coroutines.delay(100)
                val verifySnapshot = db.collection(COLLECTION_FAVORITES)
                    .whereEqualTo("UserID", userId)
                    .whereEqualTo("ListingID", listingId)
                    .get()
                    .await()
                
                if (verifySnapshot.isEmpty) {
                    println("Successfully verified: All favorites removed for listing: $listingId")
                    allDeleted = true
                } else {
                    println("Warning: ${verifySnapshot.documents.size} favorite(s) still exist after deletion, retrying...")
                    maxRetries--
                }
            }
            
            if (!allDeleted && maxRetries == 0) {
                println("ERROR: Failed to delete all favorites after 3 attempts")
                // Try one more time with a different approach - get all favorites and filter
                val allFavorites = db.collection(COLLECTION_FAVORITES)
                    .whereEqualTo("UserID", userId)
                    .get()
                    .await()
                
                val remaining = allFavorites.documents.filter { 
                    it.getString("ListingID") == listingId || it.getString("ListingID")?.trim() == listingId.trim()
                }
                
                if (remaining.isNotEmpty()) {
                    println("Found ${remaining.size} remaining favorites, deleting individually...")
                    for (doc in remaining) {
                        try {
                            doc.reference.delete().await()
                        } catch (e: Exception) {
                            println("Final attempt error: ${e.message}")
                        }
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            println("Error removing favorite: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // Check if listing is favorited
    suspend fun isFavorited(listingId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        
        return try {
            val snapshot = db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("UserID", userId)
                .whereEqualTo("ListingID", listingId)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    // Clean up duplicate favorites for current user (utility function)
    suspend fun cleanupDuplicateFavorites(): Result<Int> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            // Get all favorites for this user
            val allFavorites = db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("UserID", userId)
                .get()
                .await()
            
            // Group by ListingID to find duplicates
            val favoritesByListing = allFavorites.documents.groupBy { it.getString("ListingID") }
            
            var duplicatesRemoved = 0
            for ((listingId, docs) in favoritesByListing) {
                if (docs.size > 1 && listingId != null) {
                    // Keep the first one, delete the rest
                    for (i in 1 until docs.size) {
                        docs[i].reference.delete().await()
                        duplicatesRemoved++
                    }
                }
            }
            
            Result.success(duplicatesRemoved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper to convert Firestore document to Listing
    private fun documentToListing(id: String, data: Map<String, Any>): Listing? {
        return try {
            Listing(
                id = id,
                title = data["title"] as? String ?: data["Title"] as? String ?: "",
                price = data["price"] as? String ?: data["Price"] as? String ?: "",
                distance = data["distance"] as? String ?: data["Distance"] as? String ?: "0.0 mi",
                timeAgo = data["timeAgo"] as? String ?: data["TimeAgo"] as? String ?: "",
                imageRes = try {
                    (data["imageRes"] as? Long)?.toInt() ?: (data["imageRes"] as? Int)
                } catch (e: Exception) {
                    null
                },
                category = try {
                    val categoryStr = data["category"] as? String ?: data["Category"] as? String ?: "OTHER"
                    com.cs407.badgeronsale.Category.valueOf(categoryStr.uppercase())
                } catch (e: Exception) {
                    com.cs407.badgeronsale.Category.OTHER
                },
                imageUrl = data["imageUrl"] as? String ?: data["ImageUrl"] as? String,
                sellerId = data["sellerId"] as? String ?: data["SellerID"] as? String,
                sellerName = data["sellerName"] as? String ?: data["SellerName"] as? String,
                description = data["description"] as? String ?: data["Description"] as? String ?: "",
                createdAt = try {
                    (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate() 
                        ?: (data["CreatedAt"] as? com.google.firebase.Timestamp)?.toDate()
                } catch (e: Exception) {
                    null
                }
            )
        } catch (e: Exception) {
            println("Error converting document to listing: ${e.message}")
            null
        }
    }
}

