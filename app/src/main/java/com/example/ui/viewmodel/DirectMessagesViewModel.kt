package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.entity.MessageEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ConversationItem(
    val otherUser: UserEntity,
    val lastMessage: MessageEntity,
    val unreadCount: Int
)

class DirectMessagesViewModel(
    private val repository: AppRepository,
    val currentUserId: Long
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = repository.observeUser(currentUserId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val conversations: StateFlow<List<ConversationItem>> = combine(
        repository.getAllUserMessages(currentUserId),
        repository.getAllUsers()
    ) { messages, users ->
        val userMap = users.associateBy { it.id }

        val groupedByOtherUser = messages.groupBy { msg ->
            if (msg.senderId == currentUserId) msg.receiverId else msg.senderId
        }

        groupedByOtherUser.mapNotNull { (otherId, msgList) ->
            val otherUser = userMap[otherId] ?: return@mapNotNull null
            val lastMsg = msgList.maxByOrNull { it.timestamp } ?: return@mapNotNull null
            val unread = msgList.count { it.receiverId == currentUserId && !it.isRead }

            ConversationItem(
                otherUser = otherUser,
                lastMessage = lastMsg,
                unreadCount = unread
            )
        }.sortedByDescending { it.lastMessage.timestamp }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val availableUsersToChat: StateFlow<List<UserEntity>> = repository.getAllUsers()
        .combine(MutableStateFlow(currentUserId)) { users, myId ->
            users.filter { it.id != myId && !it.isSuspended }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _activeChatRecipient = MutableStateFlow<UserEntity?>(null)
    val activeChatRecipient: StateFlow<UserEntity?> = _activeChatRecipient.asStateFlow()

    fun openChatWith(user: UserEntity) {
        _activeChatRecipient.value = user
        viewModelScope.launch {
            repository.markMessagesAsRead(currentUserId, user.id)
        }
    }

    fun closeChat() {
        _activeChatRecipient.value = null
    }

    fun getChatMessages(otherUserId: Long): StateFlow<List<MessageEntity>> {
        return repository.getMessagesBetween(currentUserId, otherUserId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun sendMessage(recipientId: Long, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(
                senderId = currentUserId,
                receiverId = recipientId,
                content = text
            )
        }
    }
}
