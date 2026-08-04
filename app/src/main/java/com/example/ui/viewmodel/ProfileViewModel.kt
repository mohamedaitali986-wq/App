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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: AppRepository,
    val profileUserId: Long
) : ViewModel() {

    val profileUser: StateFlow<UserEntity?> = repository.observeUser(profileUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val userPosts: StateFlow<List<PostEntity>> = repository.getPostsByUserId(profileUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val followerCount: StateFlow<Int> = repository.getFollowerCount(profileUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val followingCount: StateFlow<Int> = repository.getFollowingCount(profileUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _isEditDialogVisible = MutableStateFlow(false)
    val isEditDialogVisible: StateFlow<Boolean> = _isEditDialogVisible.asStateFlow()

    fun showEditDialog() {
        _isEditDialogVisible.value = true
    }

    fun hideEditDialog() {
        _isEditDialogVisible.value = false
    }

    fun updateProfile(displayName: String, bio: String, avatarColor: String) {
        viewModelScope.launch {
            repository.updateUserProfile(profileUserId, displayName, bio, avatarColor)
            hideEditDialog()
        }
    }
}
