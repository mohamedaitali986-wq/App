package com.example.ui.screens.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.UserEntity
import com.example.ui.components.ShaghafButton
import com.example.ui.components.UserAvatar
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCanvas
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.PrimaryAccent
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.ReportedPostItem

@Composable
fun AdminScreen(
    viewModel: AdminViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeUsers by viewModel.activeUserCount.collectAsState()
    val totalPosts by viewModel.totalPostsCount.collectAsState()
    val totalVideos by viewModel.totalVideosCount.collectAsState()
    val pendingReportsCount by viewModel.pendingReportCount.collectAsState()

    val usersList by viewModel.allUsers.collectAsState()
    val reportItemsList by viewModel.pendingReportItems.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Reports, 1 = Users, 2 = Featured

    var officialCaption by remember { mutableStateOf("") }
    var officialHashtags by remember { mutableStateOf("") }
    var officialType by remember { mutableStateOf("VIDEO") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkCanvas)
            .statusBarsPadding()
            .padding(top = 12.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("btn_admin_back")) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "رجوع",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "لوحة تحكم المسؤول (Admin)",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminStatCard(
                title = "المستخدمون",
                count = activeUsers.toString(),
                icon = Icons.Default.People,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "المحتوى",
                count = totalPosts.toString(),
                icon = Icons.Default.Movie,
                modifier = Modifier.weight(1f)
            )
            AdminStatCard(
                title = "البلاغات",
                count = pendingReportsCount.toString(),
                icon = Icons.Default.Report,
                color = ErrorRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tab Navigation Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurface,
            contentColor = PrimaryAccent
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "البلاغات ($pendingReportsCount)",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 0) PrimaryAccent else TextMuted
                    )
                },
                modifier = Modifier.testTag("admin_tab_reports")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "المستخدمون",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 1) PrimaryAccent else TextMuted
                    )
                },
                modifier = Modifier.testTag("admin_tab_users")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "محتوى رسمي",
                        fontWeight = FontWeight.Bold,
                        color = if (selectedTab == 2) PrimaryAccent else TextMuted
                    )
                },
                modifier = Modifier.testTag("admin_tab_official")
            )
        }

        // Tab Content
        when (selectedTab) {
            0 -> {
                // REPORTS MANAGEMENT
                if (reportItemsList.isEmpty()) {
                    AdminEmptyState("لا توجد بلاغات معلقة حالياً 🎉")
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportItemsList, key = { it.report.id }) { item ->
                            ReportItemCard(
                                item = item,
                                onDeletePost = { item.post?.let { viewModel.removeReportedPost(it.id) } },
                                onDismissReport = { item.post?.let { viewModel.dismissReport(it.id) } }
                            )
                        }
                    }
                }
            }
            1 -> {
                // USERS MANAGEMENT
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(usersList, key = { it.id }) { user ->
                        AdminUserCard(
                            user = user,
                            onToggleSuspend = {
                                viewModel.toggleUserSuspension(user.id, user.isSuspended)
                            },
                            onDeleteUser = {
                                viewModel.deleteUser(user.id)
                            }
                        )
                    }
                }
            }
            2 -> {
                // PUBLISH OFFICIAL/FEATURED CONTENT
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "نشر محتوى مميز باسم الإدارة الرسمية",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = officialCaption,
                        onValueChange = { officialCaption = it },
                        label = { Text("عنوان أو وصف الإعلان الرسمي", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = officialHashtags,
                        onValueChange = { officialHashtags = it },
                        label = { Text("الوسوم (مثال: #إعلان #رسمي)", color = TextSecondary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    ShaghafButton(
                        text = "نشر منشور رسمي مميز ★",
                        onClick = {
                            if (officialCaption.isNotBlank()) {
                                viewModel.postFeaturedOfficialContent(
                                    type = officialType,
                                    caption = officialCaption,
                                    hashtags = officialHashtags,
                                    mediaUrl = "sample_official_media"
                                )
                                officialCaption = ""
                                officialHashtags = ""
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color = PrimaryAccent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun ReportItemCard(
    item: ReportedPostItem,
    onDeletePost: () -> Unit,
    onDismissReport: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "سبب البلاغ: ${item.report.reason}",
                color = ErrorRed,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "محتوى المنشور: ${item.post?.caption ?: "غير معروف"}",
                color = TextPrimary,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onDeletePost,
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف المنشور", color = Color.White)
                }

                OutlinedButton(
                    onClick = onDismissReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("رفض البلاغ", color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun AdminUserCard(
    user: UserEntity,
    onToggleSuspend: () -> Unit,
    onDeleteUser: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                UserAvatar(
                    name = user.displayName,
                    colorHex = user.avatarColorHex,
                    size = 40.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.displayName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "@${user.username}${if (user.isAdmin) " (مسؤول)" else ""}",
                        color = if (user.isAdmin) PrimaryAccent else TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (!user.isAdmin) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onToggleSuspend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (user.isSuspended) SuccessGreen else ErrorRed
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (user.isSuspended) "تفعيل" else "تعليق",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDeleteUser) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "حذف الحساب",
                            tint = ErrorRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminEmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextSecondary, style = MaterialTheme.typography.titleMedium)
    }
}
