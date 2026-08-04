package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.NotificationEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationWithSender(
    val notification: NotificationEntity,
    val sender: UserEntity?
)

class NotificationViewModel(
    private val repository: AppRepository,
    private val currentUserId: Long
) : ViewModel() {

    val notificationsWithSenders: StateFlow<List<NotificationWithSender>> = combine(
        repository.getNotificationsForUser(currentUserId),
        repository.getAllUsers()
    ) { notifications, users ->
        val userMap = users.associateBy { it.id }
        notifications.map { notif ->
            NotificationWithSender(
                notification = notif,
                sender = userMap[notif.senderId]
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun markAllRead() {
        viewModelScope.launch {
            repository.markNotificationsRead(currentUserId)
        }
    }
}
