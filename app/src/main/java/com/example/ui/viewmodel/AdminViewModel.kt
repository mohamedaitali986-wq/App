package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.PostEntity
import com.example.data.entity.ReportEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ReportedPostItem(
    val report: ReportEntity,
    val post: PostEntity?
)

class AdminViewModel(
    private val repository: AppRepository,
    private val adminUserId: Long
) : ViewModel() {

    val activeUserCount: StateFlow<Int> = repository.getActiveUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalPostsCount: StateFlow<Int> = repository.getTotalPostsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalVideosCount: StateFlow<Int> = repository.getTotalVideosCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pendingReportCount: StateFlow<Int> = repository.getPendingReportCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingReportItems: StateFlow<List<ReportedPostItem>> = combine(
        repository.getPendingReports(),
        repository.getAllPosts()
    ) { reports, posts ->
        val postMap = posts.associateBy { it.id }
        reports.map { rep ->
            ReportedPostItem(report = rep, post = postMap[rep.postId])
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun toggleUserSuspension(userId: Long, currentIsSuspended: Boolean) {
        viewModelScope.launch {
            repository.setUserSuspendedStatus(userId, !currentIsSuspended)
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun removeReportedPost(postId: Long) {
        viewModelScope.launch {
            repository.resolveReportAndDeletePost(postId)
        }
    }

    fun dismissReport(postId: Long) {
        viewModelScope.launch {
            repository.dismissReport(postId)
        }
    }

    fun postFeaturedOfficialContent(
        type: String,
        caption: String,
        hashtags: String,
        mediaUrl: String
    ) {
        viewModelScope.launch {
            repository.createPost(
                userId = adminUserId,
                type = type,
                mediaUrl = mediaUrl.ifBlank { "official_featured_media" },
                thumbnailUrl = "",
                caption = caption,
                hashtags = hashtags.ifBlank { "#رسمي #إعلان #منصة_شغف" },
                isFeatured = true
            )
        }
    }
}
