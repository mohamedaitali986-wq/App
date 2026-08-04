package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.PostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    fun observePostById(id: Long): Flow<PostEntity?>

    @Query("SELECT * FROM posts ORDER BY isFeatured DESC, createdAt DESC")
    fun getAllPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE type = 'VIDEO'")
    suspend fun getAllVideoPostsOnce(): List<PostEntity>

    @Query("SELECT * FROM posts WHERE type = 'VIDEO' ORDER BY isFeatured DESC, createdAt DESC")
    fun getVideoPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE type = 'PHOTO' ORDER BY isFeatured DESC, createdAt DESC")
    fun getPhotoPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPostsByUserId(userId: Long): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE userId = :userId AND type = :type ORDER BY createdAt DESC")
    fun getPostsByUserIdAndType(userId: Long, type: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE hashtags LIKE '%' || :query || '%' OR caption LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchPosts(query: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE isReported = 1 ORDER BY createdAt DESC")
    fun getReportedPosts(): Flow<List<PostEntity>>

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun deletePostById(id: Long)

    @Query("UPDATE posts SET likeCount = likeCount + :delta WHERE id = :postId")
    suspend fun updateLikeCount(postId: Long, delta: Int)

    @Query("UPDATE posts SET commentCount = commentCount + :delta WHERE id = :postId")
    suspend fun updateCommentCount(postId: Long, delta: Int)

    @Query("UPDATE posts SET isReported = :isReported, reportReason = :reason WHERE id = :postId")
    suspend fun setReportedStatus(postId: Long, isReported: Boolean, reason: String = "")

    @Query("SELECT COUNT(*) FROM posts")
    fun getTotalPostsCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM posts WHERE type = 'VIDEO'")
    fun getTotalVideosCount(): Flow<Int>
}
