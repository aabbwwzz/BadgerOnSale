package com.cs407.badgeronsale.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.launch

data class Message(
    val messageID: String,
    val senderID: String,
    val receiverID: String,
    val listingID: String?,
    val messageText: String,
    val timestamp: com.google.firebase.Timestamp
)

data class Conversation(
    val otherUserID: String,
    val otherUserName: String,
    val otherUserProfilePicURL: String?,
    val lastMessage: String?,
    val lastMessageTimestamp: com.google.firebase.Timestamp?,
    val listingID: String?
)

/**
 * MessagesRepository - Manages Messages in Firestore
 * 
 * Database Relationships:
 * - Users ↔ Messages: Many-to-Many
 *   * A User can send and receive Messages related to Listings
 *   * Messages contains SenderID and ReceiverID fields (foreign keys → Users)
 * 
 * - Listings ↔ Messages: One-to-Many
 *   * A Listing can have multiple Messages
 *   * Messages contains ListingID field (foreign key → Listings) to track which listing messages are related to
 */
object MessagesRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private const val COLLECTION_MESSAGES = "messages"
    private const val COLLECTION_USERS = "users"

    // Get current user ID
    private fun getCurrentUserId(): String? = auth.currentUser?.uid

    // Send a message
    // Enforces relationships:
    // - Users-Messages (Many-to-Many): SenderID and ReceiverID link to Users
    // - Listings-Messages (One-to-Many): ListingID links to Listings (optional)
    suspend fun sendMessage(
        receiverID: String,
        messageText: String,
        listingID: String? = null
    ): Result<String> {
        val senderID = getCurrentUserId() ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val messageData = hashMapOf(
                "SenderID" to senderID,      // FK → Users (Many-to-Many relationship)
                "ReceiverID" to receiverID, // FK → Users (Many-to-Many relationship)
                "MessageText" to messageText,
                "Timestamp" to com.google.firebase.Timestamp.now()
            )
            
            if (listingID != null) {
                messageData["ListingID"] = listingID  // FK → Listings (One-to-Many relationship)
            }
            
            val docRef = db.collection(COLLECTION_MESSAGES).add(messageData).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get messages between current user and another user - REAL-TIME
    fun getMessages(otherUserID: String, listingID: String? = null): Flow<List<Message>> = callbackFlow {
        val currentUserId = getCurrentUserId() ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            // Create queries for sent and received messages
            val sentQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", currentUserId)
                .whereEqualTo("ReceiverID", otherUserID)
            
            val receivedQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", otherUserID)
                .whereEqualTo("ReceiverID", currentUserId)
            
            // Apply listing filter and ordering if needed
            // Note: We'll sort in memory to avoid Firestore index requirements
            val finalSentQuery = if (listingID != null) {
                sentQuery.whereEqualTo("ListingID", listingID)
            } else {
                sentQuery
            }
            
            val finalReceivedQuery = if (listingID != null) {
                receivedQuery.whereEqualTo("ListingID", listingID)
            } else {
                receivedQuery
            }
            
            // Store snapshots from both listeners
            var sentSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
            var receivedSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
            
            // Helper function to combine and emit messages
            fun combineAndEmit() {
                val allMessages = mutableListOf<Message>()
                
                sentSnapshot?.documents?.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    allMessages.add(
                        Message(
                            messageID = doc.id,
                            senderID = data["SenderID"] as? String ?: "",
                            receiverID = data["ReceiverID"] as? String ?: "",
                            listingID = data["ListingID"] as? String,
                            messageText = data["MessageText"] as? String ?: "",
                            timestamp = data["Timestamp"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                        )
                    )
                }
                
                receivedSnapshot?.documents?.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    // Avoid duplicates
                    if (!allMessages.any { it.messageID == doc.id }) {
                        allMessages.add(
                            Message(
                                messageID = doc.id,
                                senderID = data["SenderID"] as? String ?: "",
                                receiverID = data["ReceiverID"] as? String ?: "",
                                listingID = data["ListingID"] as? String,
                                messageText = data["MessageText"] as? String ?: "",
                                timestamp = data["Timestamp"] as? com.google.firebase.Timestamp ?: com.google.firebase.Timestamp.now()
                            )
                        )
                    }
                }
                
                // Sort by timestamp
                allMessages.sortBy { it.timestamp.seconds }
                
                try {
                    trySend(allMessages)
                } catch (e: Exception) {
                    // Channel might be closed, ignore
                }
            }
            
            // Emit empty list immediately to clear loading state
            try {
                trySend(emptyList())
            } catch (e: Exception) {
                // Channel might be closed, ignore
            }
            
            // Set up real-time listeners for both queries
            val sentListener = finalSentQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error in sent messages listener: ${error.message}")
                    // Still emit empty list on error to clear loading
                    sentSnapshot = null
                    combineAndEmit()
                    return@addSnapshotListener
                }
                
                sentSnapshot = snapshot
                combineAndEmit()
            }
            
            val receivedListener = finalReceivedQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error in received messages listener: ${error.message}")
                    // Still emit empty list on error to clear loading
                    receivedSnapshot = null
                    combineAndEmit()
                    return@addSnapshotListener
                }
                
                receivedSnapshot = snapshot
                combineAndEmit()
            }
            
            awaitClose {
                try {
                    sentListener.remove()
                    receivedListener.remove()
                } catch (e: Exception) {
                    // Ignore errors on cleanup
                }
            }
        } catch (e: Exception) {
            println("Error setting up messages listener: ${e.message}")
            try {
                trySend(emptyList())
            } catch (ex: Exception) {
                // Channel might be closed, ignore
            }
            close()
        }
    }

    // Get all conversations for current user - REAL-TIME
    fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val currentUserId = getCurrentUserId() ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        try {
            // Create queries for sent and received messages
            val sentQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", currentUserId)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
            
            val receivedQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("ReceiverID", currentUserId)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
            
            // Store snapshots from both listeners
            var sentSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
            var receivedSnapshot: com.google.firebase.firestore.QuerySnapshot? = null
            
            // Cache for user info to avoid repeated fetches
            val userInfoCache = mutableMapOf<String, Map<String, Any>>()
            
            // Helper function to build conversations from snapshots
            suspend fun buildConversations() {
                val conversationsMap = mutableMapOf<String, Conversation>()
                
                // Process sent messages
                sentSnapshot?.documents?.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val receiverID = data["ReceiverID"] as? String ?: return@forEach
                    
                    // Get user info (from cache or fetch)
                    val userData = userInfoCache[receiverID] ?: run {
                        try {
                            val userDoc = db.collection(COLLECTION_USERS).document(receiverID).get().await()
                            val userDataMap = userDoc.data ?: emptyMap()
                            userInfoCache[receiverID] = userDataMap
                            userDataMap
                        } catch (e: Exception) {
                            println("Error fetching user info for $receiverID: ${e.message}")
                            emptyMap()
                        }
                    }
                    
                    val messageTimestamp = data["Timestamp"] as? com.google.firebase.Timestamp
                    val existing = conversationsMap[receiverID]
                    
                    if (existing == null || 
                        (messageTimestamp != null && 
                         (existing.lastMessageTimestamp == null || 
                          messageTimestamp.seconds > existing.lastMessageTimestamp.seconds))) {
                        conversationsMap[receiverID] = Conversation(
                            otherUserID = receiverID,
                            otherUserName = userData["Name"] as? String ?: "Unknown",
                            otherUserProfilePicURL = userData["ProfilePicURL"] as? String,
                            lastMessage = data["MessageText"] as? String,
                            lastMessageTimestamp = messageTimestamp,
                            listingID = data["ListingID"] as? String
                        )
                    }
                }
                
                // Process received messages
                receivedSnapshot?.documents?.forEach { doc ->
                    val data = doc.data ?: return@forEach
                    val senderID = data["SenderID"] as? String ?: return@forEach
                    
                    // Get user info (from cache or fetch)
                    val userData = userInfoCache[senderID] ?: run {
                        try {
                            val userDoc = db.collection(COLLECTION_USERS).document(senderID).get().await()
                            val userDataMap = userDoc.data ?: emptyMap()
                            userInfoCache[senderID] = userDataMap
                            userDataMap
                        } catch (e: Exception) {
                            println("Error fetching user info for $senderID: ${e.message}")
                            emptyMap()
                        }
                    }
                    
                    val messageTimestamp = data["Timestamp"] as? com.google.firebase.Timestamp
                    val existing = conversationsMap[senderID]
                    
                    if (existing == null || 
                        (messageTimestamp != null && 
                         (existing.lastMessageTimestamp == null || 
                          messageTimestamp.seconds > existing.lastMessageTimestamp.seconds))) {
                        conversationsMap[senderID] = Conversation(
                            otherUserID = senderID,
                            otherUserName = userData["Name"] as? String ?: "Unknown",
                            otherUserProfilePicURL = userData["ProfilePicURL"] as? String,
                            lastMessage = data["MessageText"] as? String,
                            lastMessageTimestamp = messageTimestamp,
                            listingID = data["ListingID"] as? String
                        )
                    }
                }
                
                // Sort by last message timestamp (most recent first)
                val conversations = conversationsMap.values.sortedByDescending { 
                    it.lastMessageTimestamp?.seconds ?: 0L 
                }
                
                try {
                    trySend(conversations)
                } catch (e: Exception) {
                    // Channel might be closed, ignore
                }
            }
            
            // Set up real-time listeners
            val sentListener = sentQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error in sent conversations listener: ${error.message}")
                    return@addSnapshotListener
                }
                
                sentSnapshot = snapshot
                launch {
                    buildConversations()
                }
            }
            
            val receivedListener = receivedQuery.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Error in received conversations listener: ${error.message}")
                    return@addSnapshotListener
                }
                
                receivedSnapshot = snapshot
                launch {
                    buildConversations()
                }
            }
            
            awaitClose {
                try {
                    sentListener.remove()
                    receivedListener.remove()
                } catch (e: Exception) {
                    // Ignore errors on cleanup
                }
            }
        } catch (e: Exception) {
            println("Error setting up conversations listener: ${e.message}")
            try {
                trySend(emptyList())
            } catch (ex: Exception) {
                // Channel might be closed, ignore
            }
            close()
        }
    }

    // Get user info by ID
    suspend fun getUserInfo(userId: String): Result<Map<String, Any>> {
        return try {
            val doc = db.collection(COLLECTION_USERS).document(userId).get().await()
            if (doc.exists()) {
                Result.success(doc.data!!)
            } else {
                Result.failure(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

