package com.example.ui.screens.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.CommentEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AppRepository
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CommentWithAuthor(
    val comment: CommentEntity,
    val author: UserEntity?
)

@Composable
fun CommentsSheet(
    postId: Long,
    currentUserId: Long,
    isAdmin: Boolean,
    repository: AppRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var newCommentText by remember { mutableStateOf("") }

    val commentsFlow = remember(postId) {
        combine(
            repository.getCommentsForPost(postId),
            repository.getAllUsers()
        ) { comments, users ->
            val userMap = users.associateBy { it.id }
            comments.map { c ->
                CommentWithAuthor(comment = c, author = userMap[c.userId])
            }
        }
    }

    val commentsList by commentsFlow.collectAsState(initial = emptyList())

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = DarkSurface,
        tonalElevation = 16.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التعليقات (${commentsList.size})",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Comments List
            if (commentsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تعليقات بعد، كن أول من يعلق! ✨",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(commentsList, key = { it.comment.id }) { item ->
                        CommentItemRow(
                            item = item,
                            canDelete = item.comment.userId == currentUserId || isAdmin,
                            onDelete = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    repository.deleteComment(item.comment.id, currentUserId, isAdmin)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("اكتب تعليقاً لطيفاً...", color = TextMuted) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_add_comment"),
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = DarkSurfaceCard,
                        unfocusedContainerColor = DarkCanvas,
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            val textToSubmit = newCommentText
                            newCommentText = ""
                            CoroutineScope(Dispatchers.IO).launch {
                                repository.addComment(postId, currentUserId, textToSubmit)
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(PrimaryAccent)
                        .testTag("btn_send_comment")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "إرسال",
                        tint = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
fun CommentItemRow(
    item: CommentWithAuthor,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            UserAvatar(
                name = item.author?.displayName ?: "مستخدم",
                colorHex = item.author?.avatarColorHex ?: "#FF2A55",
                size = 36.dp
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.author?.displayName ?: "مستخدم شغف",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.comment.text,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف التعليق",
                        tint = ErrorRed.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
