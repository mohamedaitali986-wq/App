package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class UserStoryGroup(
    val user: UserEntity,
    val stories: List<StoryEntity>,
    val isCurrentUser: Boolean
)

class StoriesViewModel(
    private val repository: AppRepository,
    val currentUserId: Long
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = repository.observeUser(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val storyGroups: StateFlow<List<UserStoryGroup>> = combine(
        repository.getActiveStories(),
        repository.getAllUsers()
    ) { stories, users ->
        val userMap = users.associateBy { it.id }
        val groupedByUserId = stories.groupBy { it.userId }

        val result = mutableListOf<UserStoryGroup>()

        groupedByUserId.forEach { (userId, storyList) ->
            val u = userMap[userId]
            if (u != null) {
                result.add(
                    UserStoryGroup(
                        user = u,
                        stories = storyList,
                        isCurrentUser = (userId == currentUserId)
                    )
                )
            }
        }

        result.sortedWith(compareByDescending { it.isCurrentUser })
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedStoryGroup = MutableStateFlow<UserStoryGroup?>(null)
    val selectedStoryGroup: StateFlow<UserStoryGroup?> = _selectedStoryGroup.asStateFlow()

    private val _isAddStoryDialogVisible = MutableStateFlow(false)
    val isAddStoryDialogVisible: StateFlow<Boolean> = _isAddStoryDialogVisible.asStateFlow()

    init {
        viewModelScope.launch {
            repository.cleanupExpiredStories()
        }
    }

    fun openStoryGroup(group: UserStoryGroup) {
        _selectedStoryGroup.value = group
    }

    fun closeStoryViewer() {
        _selectedStoryGroup.value = null
    }

    fun openAddStoryDialog() {
        _isAddStoryDialogVisible.value = true
    }

    fun closeAddStoryDialog() {
        _isAddStoryDialogVisible.value = false
    }

    fun createStory(type: String, mediaUrl: String, caption: String) {
        if (mediaUrl.isBlank()) return
        viewModelScope.launch {
            repository.createStory(
                userId = currentUserId,
                type = type,
                mediaUrl = mediaUrl,
                caption = caption
            )
            closeAddStoryDialog()
        }
    }

    fun deleteStory(storyId: Long) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
            closeStoryViewer()
        }
    }
}
