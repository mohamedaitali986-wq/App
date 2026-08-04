package com.example.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.NotificationWithSender

import com.example.ui.components.NotificationsSkeletonLoader
import com.example.ui.components.shaghafGradientBackground

@Composable
fun NotificationsScreen(
    viewModel: NotificationViewModel,
    modifier: Modifier = Modifier
) {
    val notificationsList by viewModel.notificationsWithSenders.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
            .statusBarsPadding()
            .padding(top = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "الإشعارات",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            if (notificationsList.isNotEmpty()) {
                TextButton(onClick = { viewModel.markAllRead() }) {
                    Text(
                        text = "تحديد الكل كقروء",
                        color = PrimaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (notificationsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                    Text(
                        text = "لا توجد إشعارات جديدة حالياً",
                        style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notificationsList, key = { it.notification.id }) { item ->
                    NotificationCard(item = item)
                }
            }
        }
    }
}

@Composable
fun NotificationCard(item: NotificationWithSender) {
    val senderName = item.sender?.displayName ?: "مستخدم"

    val (actionText, icon, iconColor) = when (item.notification.type) {
        "LIKE" -> Triple("أعجب بالمحتوى الخاص بك", Icons.Default.Favorite, ErrorRed)
        "COMMENT" -> Triple("علق على منشورك", Icons.Default.ChatBubble, PrimaryAccent)
        else -> Triple("بدأ بمتابعتك الآن", Icons.Default.PersonAdd, PrimaryAccent)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = senderName,
                colorHex = item.sender?.avatarColorHex ?: "#FF2A55",
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = senderName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor
            )
        }
    }
}
