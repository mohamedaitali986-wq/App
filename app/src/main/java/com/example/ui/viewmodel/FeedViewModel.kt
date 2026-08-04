package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.PostEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PostWithAuthor(
    val post: PostEntity,
    val author: UserEntity?,
    val isLikedByCurrentUser: Boolean,
    val isAuthorFollowedByCurrentUser: Boolean
)

enum class FeedTab {
    VIDEOS,
    PHOTOS
}

class FeedViewModel(
    private val repository: AppRepository,
    private val currentUserId: Long
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(FeedTab.VIDEOS)
    val selectedTab: StateFlow<FeedTab> = _selectedTab.asStateFlow()

    private val _activeCommentPostId = MutableStateFlow<Long?>(null)
    val activeCommentPostId: StateFlow<Long?> = _activeCommentPostId.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshFeed() {
        viewModelScope.launch {
            _isRefreshing.value = true
            kotlinx.coroutines.delay(1200)
            _isRefreshing.value = false
        }
    }

    val videoFeedWithDetails: StateFlow<List<PostWithAuthor>> = combine(
        repository.getVideoPosts(),
        repository.getAllUsers()
    ) { posts, users ->
        val userMap = users.associateBy { it.id }
        posts.map { post ->
            val author = userMap[post.userId]
            PostWithAuthor(
                post = post,
                author = author,
                isLikedByCurrentUser = false,
                isAuthorFollowedByCurrentUser = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val photoFeedWithDetails: StateFlow<List<PostWithAuthor>> = combine(
        repository.getPhotoPosts(),
        repository.getAllUsers()
    ) { posts, users ->
        val userMap = users.associateBy { it.id }
        posts.map { post ->
            val author = userMap[post.userId]
            PostWithAuthor(
                post = post,
                author = author,
                isLikedByCurrentUser = false,
                isAuthorFollowedByCurrentUser = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val relatedPostsWithDetails: StateFlow<List<PostWithAuthor>> = combine(
        repository.getRelatedPostsForUser(currentUserId),
        repository.getAllUsers()
    ) { posts, users ->
        val userMap = users.associateBy { it.id }
        posts.map { post ->
            val author = userMap[post.userId]
            PostWithAuthor(
                post = post,
                author = author,
                isLikedByCurrentUser = false,
                isAuthorFollowedByCurrentUser = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: FeedTab) {
        _selectedTab.value = tab
    }

    fun toggleLike(postId: Long) {
        viewModelScope.launch {
            repository.toggleLike(postId, currentUserId)
        }
    }

    fun toggleFollow(authorId: Long) {
        viewModelScope.launch {
            repository.toggleFollow(currentUserId, authorId)
        }
    }

    fun openComments(postId: Long) {
        _activeCommentPostId.value = postId
    }

    fun closeComments() {
        _activeCommentPostId.value = null
    }

    fun reportPost(postId: Long, reason: String) {
        viewModelScope.launch {
            repository.reportPost(postId, currentUserId, reason)
        }
    }
}
