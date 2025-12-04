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

                // Get listing IDs from favorites (using ListingID field from schema)
                // Remove duplicates and empty strings
                val listingIds = snapshot.documents
                    .mapNotNull { it.getString("ListingID") }
                    .filter { it.isNotEmpty() }
                    .distinct()

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
                        for (listingId in listingIds) {
                            try {
                                if (listingId.isNotEmpty()) {
                                    val listingDoc = db.collection(COLLECTION_LISTINGS)
                                        .document(listingId)
                                        .get()
                                        .await()
                                    
                                    if (listingDoc.exists()) {
                                        val data = listingDoc.data ?: continue
                                        val listing = documentToListing(listingId, data)
                                        if (listing != null) {
                                            listings.add(listing)
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip this listing if it fails to load
                                println("Error loading listing $listingId: ${e.message}")
                                continue
                            }
                        }
                        try {
                            trySend(listings)
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

    // Remove listing from favorites - removes ALL duplicates
    suspend fun removeFromFavorites(listingId: String): Result<Unit> {
        val userId = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            if (listingId.isEmpty()) {
                println("Cannot delete favorite: listingId is empty")
                return Result.failure(Exception("Invalid listing ID"))
            }
            
            println("Attempting to delete favorite for listing: $listingId, user: $userId")
            
            // Find ALL favorite documents for this user and listing (including duplicates)
            // Try multiple times to catch all variations
            var snapshot = db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("UserID", userId)
                .whereEqualTo("ListingID", listingId)
                .get()
                .await()
            
            // Also check for any case variations or extra spaces
            if (snapshot.isEmpty) {
                // Try with trimmed listingId
                val trimmedId = listingId.trim()
                if (trimmedId != listingId) {
                    snapshot = db.collection(COLLECTION_FAVORITES)
                        .whereEqualTo("UserID", userId)
                        .whereEqualTo("ListingID", trimmedId)
                        .get()
                        .await()
                }
            }
            
            if (snapshot.isEmpty) {
                // Check if there are any favorites for this user at all
                val allUserFavorites = db.collection(COLLECTION_FAVORITES)
                    .whereEqualTo("UserID", userId)
                    .get()
                    .await()
                
                println("No favorites found to delete for listing: $listingId")
                println("Total favorites for user: ${allUserFavorites.documents.size}")
                // Return success even if not found - it's already deleted
                return Result.success(Unit)
            }
            
            println("Found ${snapshot.documents.size} favorite document(s) to delete for listing: $listingId")
            
            // Delete ALL matching documents sequentially to ensure they're all deleted
            for (doc in snapshot.documents) {
                try {
                    println("Deleting favorite document: ${doc.id}, ListingID: ${doc.getString("ListingID")}")
                    doc.reference.delete().await()
                    println("Successfully deleted favorite document: ${doc.id}")
                } catch (e: Exception) {
                    println("Error deleting favorite document ${doc.id}: ${e.message}")
                    // Continue deleting other documents even if one fails
                }
            }
            
            // Verify deletion by checking again
            val verifySnapshot = db.collection(COLLECTION_FAVORITES)
                .whereEqualTo("UserID", userId)
                .whereEqualTo("ListingID", listingId)
                .get()
                .await()
            
            if (verifySnapshot.isEmpty) {
                println("Successfully verified: All favorites removed for listing: $listingId")
            } else {
                println("Warning: ${verifySnapshot.documents.size} favorite(s) still exist after deletion")
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

