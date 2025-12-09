package com.cs407.badgeronsale.repository

import com.cs407.badgeronsale.Category
import com.cs407.badgeronsale.Listing
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

/**
 * ListingRepository - Manages Listings in Firestore
 * 
 * Database Relationships:
 * - Users ↔ Listings: One-to-Many
 *   * A User can create multiple Listings
 *   * Listings contains UserID field (foreign key) to reference the creator
 *   * Use getListingsByUserID() to get all listings by a specific user
 */
object ListingRepository {
    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_LISTINGS = "listings"

    // Convert Firestore document to Listing
    private fun documentToListing(id: String, data: Map<String, Any>): Listing {
        // Support both "UserID" (schema) and "sellerId" (backward compatibility)
        val userId = data["UserID"] as? String ?: data["sellerId"] as? String
        
        // Get imageUrl - check multiple possible field names
        val imageUrl = data["imageUrl"] as? String 
            ?: data["ImageUrl"] as? String
            ?: null
        
        // Debug: Log if imageUrl exists
        if (imageUrl != null) {
            println("Listing '${data["title"]}' has imageUrl: length=${imageUrl.length}, startsWith=${imageUrl.take(20)}...")
        } else {
            println("Listing '${data["title"]}' has NO imageUrl field")
        }
        
        return Listing(
            id = id,
            title = data["title"] as? String ?: "",
            price = data["price"] as? String ?: "",
            distance = data["distance"] as? String ?: "0.0 mi",
            timeAgo = data["timeAgo"] as? String ?: "",
            imageRes = (data["imageRes"] as? Long)?.toInt(),
            category = try {
                Category.valueOf(data["category"] as? String ?: "OTHER")
            } catch (e: Exception) {
                Category.OTHER
            },
            imageUrl = imageUrl,
            sellerId = userId, // UserID foreign key (Users-Listings one-to-many relationship)
            sellerName = data["sellerName"] as? String,
            description = data["description"] as? String ?: "",
            createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)?.toDate()
        )
    }

    // Convert Listing to Firestore document
    private fun listingToMap(listing: Listing): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "title" to listing.title,
            "price" to listing.price,
            "distance" to listing.distance,
            "timeAgo" to listing.timeAgo,
            "category" to listing.category.name,
            "description" to listing.description
        )
        
        if (listing.imageRes != null) {
            map["imageRes"] = listing.imageRes.toLong()
        }
        listing.imageUrl?.let { map["imageUrl"] = it }
        // Store UserID foreign key (Users-Listings one-to-many relationship)
        listing.sellerId?.let { 
            map["UserID"] = it  // Primary field name matching schema
            map["sellerId"] = it  // Keep for backward compatibility
        }
        listing.sellerName?.let { map["sellerName"] = it }
        listing.createdAt?.let { map["createdAt"] = com.google.firebase.Timestamp(it) }
        
        return map
    }

    // Get all listings as Flow
    fun getAllListings(): Flow<List<Listing>> = flow {
        try {
            val snapshot = db.collection(COLLECTION_LISTINGS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val listings = snapshot.documents.map { doc ->
                documentToListing(doc.id, doc.data ?: emptyMap())
            }
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Get listing by ID
    suspend fun getListingById(listingId: String): Listing? {
        return try {
            val doc = db.collection(COLLECTION_LISTINGS).document(listingId).get().await()
            if (doc.exists()) {
                documentToListing(doc.id, doc.data ?: emptyMap())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    // Create a new listing
    suspend fun createListing(listing: Listing): Result<String> {
        return try {
            val data = listingToMap(listing)
            val docRef = db.collection(COLLECTION_LISTINGS).add(data).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get listings by category
    fun getListingsByCategory(category: Category): Flow<List<Listing>> = flow {
        try {
            val snapshot = db.collection(COLLECTION_LISTINGS)
                .whereEqualTo("category", category.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val listings = snapshot.documents.map { doc ->
                documentToListing(doc.id, doc.data ?: emptyMap())
            }
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Search listings by title
    fun searchListings(query: String): Flow<List<Listing>> = flow {
        try {
            val snapshot = db.collection(COLLECTION_LISTINGS)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .await()
            
            val listings = snapshot.documents.map { doc ->
                documentToListing(doc.id, doc.data ?: emptyMap())
            }
            emit(listings)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Get listings by UserID (enforce Users-Listings one-to-many relationship)
    fun getListingsByUserID(userID: String): Flow<List<Listing>> = flow {
        try {
            // Query by UserID (primary) or sellerId (backward compatibility)
            val snapshot = db.collection(COLLECTION_LISTINGS)
                .whereEqualTo("UserID", userID)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val listings = snapshot.documents.map { doc ->
                documentToListing(doc.id, doc.data ?: emptyMap())
            }
            emit(listings)
        } catch (e: Exception) {
            // Fallback to sellerId if UserID query fails
            try {
                val snapshot = db.collection(COLLECTION_LISTINGS)
                    .whereEqualTo("sellerId", userID)
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                val listings = snapshot.documents.map { doc ->
                    documentToListing(doc.id, doc.data ?: emptyMap())
                }
                emit(listings)
            } catch (e2: Exception) {
                emit(emptyList())
            }
        }
    }
    
    // Get listings by UserID with real-time updates
    fun getListingsByUserIDRealtime(userID: String): Flow<List<Listing>> = callbackFlow {
        try {
            // Set up real-time listener for user's listings
            // Note: Using whereEqualTo only (without orderBy) to avoid index requirements
            // We'll sort in memory instead
            val listenerRegistration = db.collection(COLLECTION_LISTINGS)
                .whereEqualTo("UserID", userID)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Error in listings listener: ${error.message}")
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
                    
                    // Convert documents to listings
                    launch {
                        try {
                            val listings = snapshot.documents.mapNotNull { doc ->
                                try {
                                    documentToListing(doc.id, doc.data ?: emptyMap())
                                } catch (e: Exception) {
                                    println("Error converting listing ${doc.id}: ${e.message}")
                                    null
                                }
                            }
                            // Sort by createdAt in memory (newest first)
                            val sortedListings = listings.sortedByDescending { it.createdAt?.time ?: 0L }
                            try {
                                trySend(sortedListings)
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
        } catch (e: Exception) {
            println("Error setting up listings listener: ${e.message}")
            try {
                trySend(emptyList())
            } catch (ex: Exception) {
                // Channel might be closed, ignore
            }
            close()
        }
    }
    
    // Delete a listing
    suspend fun deleteListing(listingId: String, userId: String): Result<Unit> {
        return try {
            val doc = db.collection(COLLECTION_LISTINGS).document(listingId).get().await()
            if (!doc.exists()) {
                return Result.failure(Exception("Listing not found"))
            }
            
            val data = doc.data
            val listingUserId = data?.get("UserID") as? String ?: data?.get("sellerId") as? String
            
            // Verify the user owns this listing
            if (listingUserId != userId) {
                return Result.failure(Exception("You can only delete your own listings"))
            }
            
            // Delete the listing
            doc.reference.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

