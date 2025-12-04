package com.cs407.badgeronsale.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

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

    // Get messages between current user and another user
    fun getMessages(otherUserID: String, listingID: String? = null): Flow<List<Message>> = flow {
        val currentUserId = getCurrentUserId() ?: run {
            emit(emptyList())
            return@flow
        }

        try {
            // Get messages where current user is sender and other is receiver
            val sentQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", currentUserId)
                .whereEqualTo("ReceiverID", otherUserID)
            
            // Get messages where current user is receiver and other is sender
            val receivedQuery = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", otherUserID)
                .whereEqualTo("ReceiverID", currentUserId)
            
            // If listingID is specified, filter by it
            val sentSnapshot = if (listingID != null) {
                sentQuery.whereEqualTo("ListingID", listingID).orderBy("Timestamp", Query.Direction.ASCENDING).get().await()
            } else {
                sentQuery.orderBy("Timestamp", Query.Direction.ASCENDING).get().await()
            }
            
            val receivedSnapshot = if (listingID != null) {
                receivedQuery.whereEqualTo("ListingID", listingID).orderBy("Timestamp", Query.Direction.ASCENDING).get().await()
            } else {
                receivedQuery.orderBy("Timestamp", Query.Direction.ASCENDING).get().await()
            }
            
            // Combine and sort messages
            val allMessages = mutableListOf<Message>()
            
            sentSnapshot.documents.forEach { doc ->
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
            
            receivedSnapshot.documents.forEach { doc ->
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
            
            // Sort by timestamp
            allMessages.sortBy { it.timestamp.seconds }
            
            emit(allMessages)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Get all conversations for current user
    fun getConversations(): Flow<List<Conversation>> = flow {
        val currentUserId = getCurrentUserId() ?: run {
            emit(emptyList())
            return@flow
        }

        try {
            // Get all messages where current user is sender or receiver
            val sentSnapshot = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("SenderID", currentUserId)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val receivedSnapshot = db.collection(COLLECTION_MESSAGES)
                .whereEqualTo("ReceiverID", currentUserId)
                .orderBy("Timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            // Map of other user ID to conversation info
            val conversationsMap = mutableMapOf<String, Conversation>()
            
            // Process sent messages
            sentSnapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                val receiverID = data["ReceiverID"] as? String ?: return@forEach
                
                if (!conversationsMap.containsKey(receiverID)) {
                    // Fetch user info
                    val userDoc = db.collection(COLLECTION_USERS).document(receiverID).get().await()
                    val userData = userDoc.data
                    
                    conversationsMap[receiverID] = Conversation(
                        otherUserID = receiverID,
                        otherUserName = userData?.get("Name") as? String ?: "Unknown",
                        otherUserProfilePicURL = userData?.get("ProfilePicURL") as? String,
                        lastMessage = data["MessageText"] as? String,
                        lastMessageTimestamp = data["Timestamp"] as? com.google.firebase.Timestamp,
                        listingID = data["ListingID"] as? String
                    )
                }
            }
            
            // Process received messages
            receivedSnapshot.documents.forEach { doc ->
                val data = doc.data ?: return@forEach
                val senderID = data["SenderID"] as? String ?: return@forEach
                
                if (!conversationsMap.containsKey(senderID)) {
                    // Fetch user info
                    val userDoc = db.collection(COLLECTION_USERS).document(senderID).get().await()
                    val userData = userDoc.data
                    
                    conversationsMap[senderID] = Conversation(
                        otherUserID = senderID,
                        otherUserName = userData?.get("Name") as? String ?: "Unknown",
                        otherUserProfilePicURL = userData?.get("ProfilePicURL") as? String,
                        lastMessage = data["MessageText"] as? String,
                        lastMessageTimestamp = data["Timestamp"] as? com.google.firebase.Timestamp,
                        listingID = data["ListingID"] as? String
                    )
                } else {
                    // Update if this message is more recent
                    val existing = conversationsMap[senderID]!!
                    val messageTimestamp = data["Timestamp"] as? com.google.firebase.Timestamp
                    if (messageTimestamp != null && 
                        (existing.lastMessageTimestamp == null || 
                         messageTimestamp.seconds > existing.lastMessageTimestamp.seconds)) {
                        conversationsMap[senderID] = existing.copy(
                            lastMessage = data["MessageText"] as? String,
                            lastMessageTimestamp = messageTimestamp
                        )
                    }
                }
            }
            
            // Sort by last message timestamp (most recent first)
            val conversations = conversationsMap.values.sortedByDescending { 
                it.lastMessageTimestamp?.seconds ?: 0L 
            }
            
            emit(conversations)
        } catch (e: Exception) {
            emit(emptyList())
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

