package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val type: String, // "VIDEO" or "PHOTO"
    val mediaUrl: String,
    val thumbnailUrl: String = "",
    val caption: String = "",
    val hashtags: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val isFeatured: Boolean = false,
    val isReported: Boolean = false,
    val reportReason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
