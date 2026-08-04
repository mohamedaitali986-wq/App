package com.example.data.entity

import androidx.room.Entity

@Entity(
    tableName = "likes",
    primaryKeys = ["postId", "userId"]
)
data class LikeEntity(
    val postId: Long,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
