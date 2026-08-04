package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val type: String = "PHOTO", // "PHOTO" or "VIDEO"
    val mediaUrl: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
