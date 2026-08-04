package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("SELECT * FROM reports WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingReports(): Flow<List<ReportEntity>>

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'PENDING'")
    fun getPendingReportCount(): Flow<Int>

    @Query("UPDATE reports SET status = :status WHERE postId = :postId")
    suspend fun setReportStatusForPost(postId: Long, status: String)
}
