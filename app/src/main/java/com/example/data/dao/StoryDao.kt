package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories WHERE createdAt >= :cutoffTime ORDER BY createdAt ASC")
    fun getActiveStories(cutoffTime: Long): Flow<List<StoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity): Long

    @Query("DELETE FROM stories WHERE id = :storyId")
    suspend fun deleteStory(storyId: Long)

    @Query("DELETE FROM stories WHERE createdAt < :cutoffTime")
    suspend fun deleteExpiredStories(cutoffTime: Long)
}
