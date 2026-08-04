package com.example.data.entity

import androidx.room.Entity

@Entity(
    tableName = "follows",
    primaryKeys = ["followerId", "followingId"]
)
data class FollowEntity(
    val followerId: Long,
    val followingId: Long,
    val createdAt: Long = System.currentTimeMillis()
)
