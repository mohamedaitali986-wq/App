package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val postId: Long,
    val reporterUserId: Long,
    val reason: String,
    val status: String = "PENDING", // "PENDING", "RESOLVED", "REJECTED"
    val createdAt: Long = System.currentTimeMillis()
)
