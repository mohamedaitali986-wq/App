package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.entity.StoryEntity
import com.example.data.entity.UserEntity
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.PrimaryAccentVariant
import com.example.ui.theme.SecondaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.UserStoryGroup
import kotlinx.coroutines.delay

@Composable
fun StoriesBar(
    currentUser: UserEntity?,
    storyGroups: List<UserStoryGroup>,
    onAddStoryClick: () -> Unit,
    onStoryGroupClick: (UserStoryGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Current User Story / Add Story Button
        item {
            val userGroup = storyGroups.find { it.isCurrentUser }
            val hasOwnStories = userGroup != null && userGroup.stories.isNotEmpty()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (hasOwnStories && userGroup != null) {
                        onStoryGroupClick(userGroup)
                    } else {
                        onAddStoryClick()
                    }
                }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    if (hasOwnStories) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        listOf(PrimaryAccent, SecondaryAccent, PrimaryAccent)
                                    )
                                )
                                .padding(2.5.dp)
                                .clip(CircleShape)
                                .background(DarkCanvas)
                        )
                    }

                    UserAvatar(
                        name = currentUser?.displayName ?: "أنا",
                        colorHex = currentUser?.avatarColorHex ?: "#FF2A55",
                        size = 60.dp
                    )

                    // Plus icon badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(PrimaryAccent)
                            .border(2.dp, DarkCanvas, CircleShape)
                            .clickable { onAddStoryClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "إضافة قصة",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (hasOwnStories) "قصتي" else "+ قصتي",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Other Users Stories
        items(
            items = storyGroups.filter { !it.isCurrentUser && it.stories.isNotEmpty() },
            key = { it.user.id }
        ) { group ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onStoryGroupClick(group) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(68.dp)
                ) {
                    // Animated gradient ring for stories
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PrimaryAccent, SecondaryAccent, PrimaryAccentVariant)
                                )
                            )
                            .padding(2.5.dp)
                            .clip(CircleShape)
                            .background(DarkCanvas)
                    )

                    UserAvatar(
                        name = group.user.displayName,
                        colorHex = group.user.avatarColorHex,
                        size = 60.dp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = group.user.displayName,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(68.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StoryViewerDialog(
    group: UserStoryGroup,
    currentUserId: Long,
    onDismiss: () -> Unit,
    onDeleteStory: (Long) -> Unit
) {
    if (group.stories.isEmpty()) return

    var currentIndex by remember { mutableIntStateOf(0) }
    val currentStory = group.stories.getOrNull(currentIndex) ?: group.stories.first()

    var progress by remember(currentIndex) { mutableStateOf(0f) }

    // Auto progress timer
    LaunchedEffect(currentIndex) {
        progress = 0f
        val duration = 5000L
        val interval = 50L
        val steps = (duration / interval).toInt()

        for (i in 1..steps) {
            delay(interval)
            progress = i.toFloat() / steps
        }

        if (currentIndex < group.stories.size - 1) {
            currentIndex++
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Media Content
                if (currentStory.type == "VIDEO") {
                    VideoPlayerView(
                        videoUrl = currentStory.mediaUrl,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AsyncImage(
                        model = currentStory.mediaUrl,
                        contentDescription = "قصة",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // Dark top gradient for headers
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                )

                // Top Controls & Progress Indicators
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp)
                ) {
                    // Segmented Progress Bars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        group.stories.forEachIndexed { index, _ ->
                            val segProgress = when {
                                index < currentIndex -> 1f
                                index == currentIndex -> progress
                                else -> 0f
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color.White.copy(alpha = 0.35f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(segProgress)
                                        .background(PrimaryAccent)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // User Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                name = group.user.displayName,
                                colorHex = group.user.avatarColorHex,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = group.user.displayName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "قصة مؤقتة (٢٤ ساعة)",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (group.isCurrentUser) {
                                IconButton(onClick = { onDeleteStory(currentStory.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "حذف القصة",
                                        tint = Color.White
                                    )
                                }
                            }

                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "إغلاق",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Tap zones for previous / next story
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentIndex > 0) {
                                    currentIndex--
                                }
                            }
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentIndex < group.stories.size - 1) {
                                    currentIndex++
                                } else {
                                    onDismiss()
                                }
                            }
                    )
                }

                // Bottom Caption Overlay
                if (currentStory.caption.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentStory.caption,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddStoryDialog(
    onDismiss: () -> Unit,
    onSubmit: (type: String, mediaUrl: String, caption: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("PHOTO") }
    var captionText by remember { mutableStateOf("") }

    val presetPhotoUrls = listOf(
        "sample_photo_art.jpg" to "فن ورسم",
        "sample_photo_food.jpg" to "قهوة ووجبة",
        "sample_photo_admin.jpg" to "إعلان جديد"
    )

    val presetVideoUrls = listOf(
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4" to "فيديو طبيعة",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4" to "فيديو سفر"
    )

    var selectedMediaUrl by remember { mutableStateOf(presetPhotoUrls[0].first) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "نشر قصة جديدة (تختفي بعد ٢٤ ساعة)",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Right
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Type selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            selectedType = "PHOTO"
                            selectedMediaUrl = presetPhotoUrls[0].first
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "PHOTO") PrimaryAccent else DarkSurfaceCard
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("صورة", color = Color.White)
                    }

                    Button(
                        onClick = {
                            selectedType = "VIDEO"
                            selectedMediaUrl = presetVideoUrls[0].first
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "VIDEO") PrimaryAccent else DarkSurfaceCard
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("فيديو قصير", color = Color.White)
                    }
                }

                Text(
                    text = "اختر الوسائط التجريبية للقصة:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Presets list
                val activePresets = if (selectedType == "PHOTO") presetPhotoUrls else presetVideoUrls
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    activePresets.forEach { (url, label) ->
                        Card(
                            onClick = { selectedMediaUrl = url },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedMediaUrl == url) PrimaryAccent.copy(alpha = 0.25f) else DarkSurfaceCard
                            ),
                            border = if (selectedMediaUrl == url) androidx.compose.foundation.BorderStroke(1.dp, PrimaryAccent) else null,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, color = TextPrimary, fontSize = 14.sp)
                                if (selectedMediaUrl == url) {
                                    Text("محدد ✓", color = PrimaryAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = captionText,
                    onValueChange = { captionText = it },
                    label = { Text("أضف تعليقاً على القصة (اختياري)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryAccent,
                        unfocusedBorderColor = DarkBorder,
                        focusedLabelColor = PrimaryAccent,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(selectedType, selectedMediaUrl, captionText)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("نشر القصة الآن", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
