package com.example.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["username"], unique = true)]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val displayName: String,
    val bio: String = "",
    val avatarUrl: String = "",
    val avatarColorHex: String = "#FF2A55",
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
