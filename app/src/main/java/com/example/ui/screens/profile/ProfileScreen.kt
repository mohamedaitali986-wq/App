package com.example.ui.screens.profile

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.PostEntity
import com.example.ui.components.ShaghafButton
import com.example.ui.components.StatItem
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
import com.example.ui.viewmodel.ProfileViewModel

import androidx.compose.material.icons.filled.Language
import androidx.compose.ui.res.stringResource
import com.example.R

import com.example.ui.components.ProfileSkeletonLoader
import com.example.ui.components.shaghafGradientBackground

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    currentLanguage: String,
    onLanguageChanged: (String) -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val user by viewModel.profileUser.collectAsState()
    val posts by viewModel.userPosts.collectAsState()
    val followers by viewModel.followerCount.collectAsState()
    val following by viewModel.followingCount.collectAsState()
    val isEditDialogVisible by viewModel.isEditDialogVisible.collectAsState()

    if (user == null) {
        ProfileSkeletonLoader(
            modifier = modifier
                .fillMaxSize()
                .shaghafGradientBackground()
                .statusBarsPadding()
        )
        return
    }

    var editName by remember(user) { mutableStateOf(user?.displayName.orEmpty()) }
    var editBio by remember(user) { mutableStateOf(user?.bio.orEmpty()) }
    var selectedColorHex by remember(user) { mutableStateOf(user?.avatarColorHex ?: "#FF2A55") }

    val colorOptions = listOf("#FF2A55", "#8A2BE2", "#00D2FF", "#FF9900", "#34C759", "#7000FF")

    Column(
        modifier = modifier
            .fillMaxSize()
            .shaghafGradientBackground()
            .statusBarsPadding()
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Actions (Edit Profile, Admin Panel if Admin, Logout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onLogout, modifier = Modifier.testTag("btn_logout")) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = stringResource(id = R.string.logout),
                    tint = ErrorRed
                )
            }

            Text(
                text = stringResource(id = R.string.nav_profile),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Row {
                if (user?.isAdmin == true) {
                    IconButton(
                        onClick = onNavigateToAdminPanel,
                        modifier = Modifier.testTag("btn_admin_entrance")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = stringResource(id = R.string.admin_panel),
                            tint = PrimaryAccent
                        )
                    }
                }

                IconButton(
                    onClick = { viewModel.showEditDialog() },
                    modifier = Modifier.testTag("btn_edit_profile")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit_profile),
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Avatar & Info
        UserAvatar(
            name = user?.displayName ?: "مستخدم",
            colorHex = user?.avatarColorHex ?: "#FF2A55",
            size = 80.dp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = user?.displayName ?: stringResource(id = R.string.app_name),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Text(
            text = "@${user?.username ?: "user"}",
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )

        if (!user?.bio.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = user?.bio.orEmpty(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .padding(vertical = 14.dp, horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(count = posts.size, label = stringResource(id = R.string.posts))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(DarkBorder)
            )
            StatItem(count = followers, label = stringResource(id = R.string.followers))
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(30.dp)
                    .background(DarkBorder)
            )
            StatItem(count = following, label = stringResource(id = R.string.following))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Language Switcher Selector
        Row(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(14.dp))
                .background(DarkSurface)
                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(id = R.string.language_switcher),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Arabic Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentLanguage == "ar") PrimaryAccent else DarkSurfaceCard)
                        .clickable { onLanguageChanged("ar") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.lang_ar),
                        color = if (currentLanguage == "ar") Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // English Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (currentLanguage == "en") PrimaryAccent else DarkSurfaceCard)
                        .clickable { onLanguageChanged("en") }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.lang_en),
                        color = if (currentLanguage == "en") Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Posts Grid Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GridOn,
                contentDescription = null,
                tint = PrimaryAccent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.posts),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Posts Grid
        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.no_posts),
                    color = TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(posts, key = { it.id }) { post ->
                    ProfileGridItem(post = post)
                }
            }
        }
    }

    // Edit Profile Dialog
    if (isEditDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.hideEditDialog() },
            containerColor = DarkSurface,
            title = {
                Text(
                    text = stringResource(id = R.string.edit_profile),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(stringResource(id = R.string.display_name), color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = editBio,
                        onValueChange = { editBio = it },
                        label = { Text(stringResource(id = R.string.bio), color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryAccent,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (selectedColorHex == hex) 3.dp else 0.dp,
                                        color = Color.White,
                                        shape = CircleShape
                                    )
                                    .clickable { selectedColorHex = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(editName, editBio, selectedColorHex)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                ) {
                    Text(stringResource(id = R.string.save_changes), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideEditDialog() }) {
                    Text(stringResource(id = R.string.cancel), color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun ProfileGridItem(post: PostEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF221F33)),
            contentAlignment = Alignment.Center
        ) {
            if (post.type == "VIDEO") {
                Icon(
                    imageVector = Icons.Default.Movie,
                    contentDescription = "فيديو",
                    tint = PrimaryAccent,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Text(text = "📷", fontSize = 24.sp)
            }
        }
    }
}
