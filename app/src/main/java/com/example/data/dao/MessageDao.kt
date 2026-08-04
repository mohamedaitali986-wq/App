package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("""
        SELECT * FROM messages 
        WHERE (senderId = :user1Id AND receiverId = :user2Id) 
           OR (senderId = :user2Id AND receiverId = :user1Id) 
        ORDER BY timestamp ASC
    """)
    fun getMessagesBetween(user1Id: Long, user2Id: Long): Flow<List<MessageEntity>>

    @Query("""
        SELECT * FROM messages 
        WHERE senderId = :userId OR receiverId = :userId 
        ORDER BY timestamp DESC
    """)
    fun getAllMessagesForUser(userId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE receiverId = :currentUserId AND senderId = :otherUserId")
    suspend fun markMessagesAsRead(currentUserId: Long, otherUserId: Long)
}
