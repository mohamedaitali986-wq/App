package com.example.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PostEntity
import com.example.data.entity.UserEntity
import com.example.ui.components.HashtagChip
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SearchCategory
import com.example.ui.viewmodel.SearchViewModel

import com.example.ui.components.SearchSkeletonLoader
import com.example.ui.components.shaghafGradientBackground

@Composable
fun DiscoverScreen(
    viewModel: SearchViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.query.collectAsState()
    val category by viewModel.category.collectAsState()
    val usersList by viewModel.searchUsersResult.collectAsState()
    val postsList by viewModel.searchPostsResult.collectAsState()

    val popularHashtags = listOf("#تقنية", "#تصميم", "#فنون", "#طبخ", "#سفر", "#منصة_شغف")

    Column(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
            .statusBarsPadding()
            .padding(top = 12.dp)
    ) {
        // Search Input Bar
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.onQueryChanged(it) },
            placeholder = { Text("ابحث عن مستخدمين أو وسم...", color = TextMuted) },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "بحث", tint = TextSecondary)
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onQueryChanged("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = TextSecondary)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = DarkSurfaceCard,
                unfocusedContainerColor = DarkSurface,
                focusedBorderColor = PrimaryAccent,
                unfocusedBorderColor = DarkBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("input_search_query")
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Popular Hashtag Row
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(popularHashtags) { tag ->
                HashtagChip(
                    text = tag,
                    onClick = { viewModel.selectHashtag(tag) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Category Tabs
        TabRow(
            selectedTabIndex = category.ordinal,
            containerColor = DarkSurface,
            contentColor = PrimaryAccent
        ) {
            Tab(
                selected = category == SearchCategory.USERS,
                onClick = { viewModel.selectCategory(SearchCategory.USERS) },
                text = {
                    Text(
                        text = "المستخدمون",
                        fontWeight = FontWeight.Bold,
                        color = if (category == SearchCategory.USERS) PrimaryAccent else TextMuted
                    )
                },
                modifier = Modifier.testTag("tab_search_users")
            )
            Tab(
                selected = category == SearchCategory.CONTENT,
                onClick = { viewModel.selectCategory(SearchCategory.CONTENT) },
                text = {
                    Text(
                        text = "المحتوى",
                        fontWeight = FontWeight.Bold,
                        color = if (category == SearchCategory.CONTENT) PrimaryAccent else TextMuted
                    )
                },
                modifier = Modifier.testTag("tab_search_content")
            )
        }

        // Search Results Content View
        if (category == SearchCategory.USERS) {
            if (usersList.isEmpty()) {
                if (query.isNotBlank()) {
                    SearchSkeletonLoader()
                } else {
                    EmptySearchState("ابحث عن مستخدمين باستخدام مربع البحث أعلاه")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(usersList, key = { it.id }) { user ->
                        UserSearchResultCard(user = user)
                    }
                }
            }
        } else {
            if (postsList.isEmpty()) {
                if (query.isNotBlank()) {
                    SearchSkeletonLoader()
                } else {
                    EmptySearchState("ابحث عن محتوى أو اختر وسم من الوسوم الشائعة")
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(postsList, key = { it.id }) { post ->
                        PostSearchResultCard(post = post)
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchResultCard(user: UserEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = user.displayName,
                colorHex = user.avatarColorHex,
                size = 48.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun PostSearchResultCard(post: PostEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF221F33)),
                contentAlignment = Alignment.Center
            ) {
                if (post.type == "VIDEO") {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "فيديو",
                        tint = PrimaryAccent,
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Text(text = "📷 صورة", color = TextSecondary)
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(8.dp)
            ) {
                Text(
                    text = post.caption,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptySearchState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
            )
        }
    }
}
