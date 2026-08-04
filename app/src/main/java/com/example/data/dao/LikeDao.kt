package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.LikeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLike(like: LikeEntity)

    @Query("DELETE FROM likes WHERE postId = :postId AND userId = :userId")
    suspend fun deleteLike(postId: Long, userId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE postId = :postId AND userId = :userId)")
    suspend fun isPostLikedByUser(postId: Long, userId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM likes WHERE postId = :postId AND userId = :userId)")
    fun observeIsPostLikedByUser(postId: Long, userId: Long): Flow<Boolean>

    @Query("SELECT postId FROM likes WHERE userId = :userId")
    fun getLikedPostIdsByUser(userId: Long): Flow<List<Long>>
}
