package com.example.ui.screens.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.example.ui.components.AddStoryDialog
import com.example.ui.components.PhotoFeedSkeletonList
import com.example.ui.components.ShaghafLogo
import com.example.ui.components.StoriesBar
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VideoFeedSkeleton
import com.example.ui.components.VideoPlayerView
import com.example.ui.components.shaghafGradientBackground
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FeedTab
import com.example.ui.viewmodel.FeedViewModel
import com.example.ui.viewmodel.PostWithAuthor
import com.example.ui.viewmodel.StoriesViewModel
import androidx.compose.ui.res.stringResource
import com.example.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    storiesViewModel: StoriesViewModel? = null,
    onOpenMessages: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val videoFeed by viewModel.videoFeedWithDetails.collectAsState()
    val photoFeed by viewModel.photoFeedWithDetails.collectAsState()
    val relatedPosts by viewModel.relatedPostsWithDetails.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val storyGroups = storiesViewModel?.storyGroups?.collectAsState()?.value ?: emptyList()
    val currentUser = storiesViewModel?.currentUser?.collectAsState()?.value
    val selectedStoryGroup = storiesViewModel?.selectedStoryGroup?.collectAsState()?.value
    val isAddStoryVisible = storiesViewModel?.isAddStoryDialogVisible?.collectAsState()?.value ?: false

    Box(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header: Logo, Tabs, DM Messages Icon Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShaghafLogo(size = 32.dp, showText = true)

                // Tab Switcher
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    Row {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTab == FeedTab.VIDEOS) PrimaryAccent else Color.Transparent)
                                .clickable { viewModel.selectTab(FeedTab.VIDEOS) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("tab_feed_videos")
                        ) {
                            Text(
                                text = stringResource(id = R.string.short_video),
                                color = if (selectedTab == FeedTab.VIDEOS) Color.White else TextMuted,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (selectedTab == FeedTab.PHOTOS) PrimaryAccent else Color.Transparent)
                                .clickable { viewModel.selectTab(FeedTab.PHOTOS) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("tab_feed_photos")
                        ) {
                            Text(
                                text = stringResource(id = R.string.photo),
                                color = if (selectedTab == FeedTab.PHOTOS) Color.White else TextMuted,
                                style = androidx.compose.material3.MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // DM Messages Button
                IconButton(
                    onClick = { onOpenMessages?.invoke() },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubble,
                        contentDescription = "الرسائل المباشرة",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Stories Row (Visible in feed top)
            if (storiesViewModel != null) {
                StoriesBar(
                    currentUser = currentUser,
                    storyGroups = storyGroups,
                    onAddStoryClick = { storiesViewModel.openAddStoryDialog() },
                    onStoryGroupClick = { storiesViewModel.openStoryGroup(it) }
                )
            }

            // Feed Content View with Pull to Refresh
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshFeed() },
                modifier = Modifier.weight(1f)
            ) {
                if (selectedTab == FeedTab.VIDEOS) {
                    VideoFeedPager(
                        posts = videoFeed,
                        onLikeClick = { viewModel.toggleLike(it) },
                        onCommentClick = { viewModel.openComments(it) },
                        onFollowClick = { viewModel.toggleFollow(it) },
                        onReportClick = { postId, reason -> viewModel.reportPost(postId, reason) }
                    )
                } else {
                    PhotoFeedList(
                        posts = photoFeed,
                        relatedPosts = relatedPosts,
                        onLikeClick = { viewModel.toggleLike(it) },
                        onCommentClick = { viewModel.openComments(it) },
                        onReportClick = { postId, reason -> viewModel.reportPost(postId, reason) }
                    )
                }
            }
        }

        // Story Viewer Overlay
        if (selectedStoryGroup != null && storiesViewModel != null) {
            StoryViewerDialog(
                group = selectedStoryGroup,
                currentUserId = storiesViewModel.currentUserId,
                onDismiss = { storiesViewModel.closeStoryViewer() },
                onDeleteStory = { storiesViewModel.deleteStory(it) }
            )
        }

        // Add Story Dialog Overlay
        if (isAddStoryVisible && storiesViewModel != null) {
            AddStoryDialog(
                onDismiss = { storiesViewModel.closeAddStoryDialog() },
                onSubmit = { type, url, caption -> storiesViewModel.createStory(type, url, caption) }
            )
        }
    }
}

@Composable
fun VideoFeedPager(
    posts: List<PostWithAuthor>,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onFollowClick: (Long) -> Unit,
    onReportClick: (Long, String) -> Unit
) {
    if (posts.isEmpty()) {
        VideoFeedSkeleton()
        return
    }

    val pagerState = rememberPagerState(pageCount = { posts.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        val item = posts[page]
        VideoPostPage(
            item = item,
            isCurrentPage = pagerState.currentPage == page,
            onLikeClick = { onLikeClick(item.post.id) },
            onCommentClick = { onCommentClick(item.post.id) },
            onFollowClick = { item.author?.id?.let { onFollowClick(it) } },
            onReportClick = { reason -> onReportClick(item.post.id, reason) }
        )
    }
}

@Composable
fun VideoPostPage(
    item: PostWithAuthor,
    isCurrentPage: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onFollowClick: () -> Unit,
    onReportClick: (String) -> Unit
) {
    var isLikedLocally by remember { mutableStateOf(item.isLikedByCurrentUser) }
    var localLikeCount by remember { mutableStateOf(item.post.likeCount) }
    var showReportMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video Surface
        VideoPlayerView(
            videoUrl = item.post.mediaUrl,
            thumbnailUrl = item.post.thumbnailUrl,
            isPlaying = isCurrentPage
        )

        // Bottom & Side Gradient Overlays
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                    )
                )
        )

        // Featured Badge (if official/featured content)
        if (item.post.isFeatured) {
            Box(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 12.dp, start = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryAccent)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "رسمي ★",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // Right Action Overlay Column
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
        ) {
            // Author Avatar with Follow Button Badge
            Box(contentAlignment = Alignment.BottomCenter) {
                UserAvatar(
                    name = item.author?.displayName ?: "مستخدم",
                    colorHex = item.author?.avatarColorHex ?: "#FF2A55",
                    size = 50.dp
                )
                Box(
                    modifier = Modifier
                        .padding(top = 38.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(PrimaryAccent)
                        .clickable { onFollowClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Like Action Button
            val scale by animateFloatAsState(
                targetValue = if (isLikedLocally) 1.25f else 1.0f,
                animationSpec = spring(stiffness = 400f),
                label = "like_scale"
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    isLikedLocally = !isLikedLocally
                    localLikeCount += if (isLikedLocally) 1 else -1
                    onLikeClick()
                }
            ) {
                Icon(
                    imageVector = if (isLikedLocally) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "إعجاب",
                    tint = if (isLikedLocally) ErrorRed else Color.White,
                    modifier = Modifier
                        .scale(scale)
                        .size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = localLikeCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Comment Action Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onCommentClick() }
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "تعليق",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.post.commentCount.toString(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Report / Options Menu
            Box {
                IconButton(onClick = { showReportMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "خيارات",
                        tint = Color.White
                    )
                }

                DropdownMenu(
                    expanded = showReportMenu,
                    onDismissRequest = { showReportMenu = false },
                    modifier = Modifier.background(DarkSurfaceCard)
                ) {
                    DropdownMenuItem(
                        text = { Text("إبلاغ عن محتوى غير لائق", color = ErrorRed) },
                        onClick = {
                            showReportMenu = false
                            onReportClick("محتوى غير لائق")
                        }
                    )
                }
            }
        }

        // Bottom Left Caption & Audio Info Area
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.82f)
                .padding(bottom = 90.dp, start = 16.dp)
        ) {
            Text(
                text = "@${item.author?.username ?: "user"}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.post.caption,
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            if (item.post.hashtags.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.post.hashtags,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = PrimaryAccent,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Music / Audio Ticker
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الصوت الأصلي - ${item.author?.displayName ?: "شغف"}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun PhotoFeedList(
    posts: List<PostWithAuthor>,
    relatedPosts: List<PostWithAuthor>,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit,
    onReportClick: (Long, String) -> Unit
) {
    if (posts.isEmpty()) {
        PhotoFeedSkeletonList()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(posts, key = { it.post.id }) { item ->
            PhotoPostCard(
                item = item,
                onLikeClick = { onLikeClick(item.post.id) },
                onCommentClick = { onCommentClick(item.post.id) },
                onReportClick = { reason -> onReportClick(item.post.id, reason) }
            )
        }

        if (relatedPosts.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                RelatedContentSection(
                    relatedPosts = relatedPosts,
                    onLikeClick = onLikeClick,
                    onCommentClick = onCommentClick
                )
            }
        }
    }
}

@Composable
fun RelatedContentSection(
    relatedPosts: List<PostWithAuthor>,
    onLikeClick: (Long) -> Unit,
    onCommentClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurface)
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "محتوى مرتبط قد يعجبك 🌟",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "بناءً على اهتماماتك والوسوم",
                color = PrimaryAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(relatedPosts, key = { it.post.id }) { item ->
                Card(
                    modifier = Modifier.width(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                name = item.author?.displayName ?: "صانع محتوى",
                                colorHex = item.author?.avatarColorHex ?: "#FF2A55",
                                size = 32.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.author?.displayName ?: "",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.post.caption,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.post.hashtags,
                            color = PrimaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PhotoPostCard(
    item: PostWithAuthor,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onReportClick: (String) -> Unit
) {
    var isLiked by remember { mutableStateOf(item.isLikedByCurrentUser) }
    var likeCount by remember { mutableStateOf(item.post.likeCount) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface)
    ) {
        Column {
            // Header: Avatar, Name, Options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        name = item.author?.displayName ?: "مستخدم",
                        colorHex = item.author?.avatarColorHex ?: "#FF2A55",
                        size = 40.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.author?.displayName ?: "مستخدم شغف",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "@${item.author?.username ?: "user"}",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            // Photo Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(DarkSurfaceCard, Color(0xFF2C223E))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "📷 ${item.post.caption.take(20)}...",
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    isLiked = !isLiked
                    likeCount += if (isLiked) 1 else -1
                    onLikeClick()
                }) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (isLiked) ErrorRed else TextPrimary
                    )
                }

                Text(
                    text = likeCount.toString(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { onCommentClick() }) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "تعليق",
                        tint = TextPrimary
                    )
                }

                Text(
                    text = item.post.commentCount.toString(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            // Caption
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp)) {
                Text(
                    text = item.post.caption,
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary)
                )

                if (item.post.hashtags.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.post.hashtags,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PrimaryAccent,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}
