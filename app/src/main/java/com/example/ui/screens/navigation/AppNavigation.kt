package com.example.ui.screens.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AppRepository
import com.example.ui.components.NavigationItem
import com.example.ui.components.ShaghafBottomBar
import com.example.ui.screens.admin.AdminScreen
import com.example.ui.screens.auth.AuthScreen
import com.example.ui.screens.comments.CommentsSheet
import com.example.ui.screens.discover.DiscoverScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.messages.DirectMessagesScreen
import com.example.ui.screens.notifications.NotificationsScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.upload.UploadScreen
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.AuthState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DirectMessagesViewModel
import com.example.ui.viewmodel.FeedViewModel
import com.example.ui.viewmodel.NotificationViewModel
import com.example.ui.viewmodel.ProfileViewModel
import com.example.ui.viewmodel.SearchViewModel
import com.example.ui.viewmodel.StoriesViewModel
import com.example.ui.viewmodel.UploadViewModel
import com.example.util.SessionManager

@Composable
fun AppNavigation(
    repository: AppRepository,
    sessionManager: SessionManager,
    modifier: Modifier = Modifier
) {
    val authViewModel: AuthViewModel = remember(repository, sessionManager) {
        AuthViewModel(repository, sessionManager)
    }

    val authState by authViewModel.authState.collectAsState()

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize())
        }

        is AuthState.Unauthenticated -> {
            AuthScreen(viewModel = authViewModel)
        }

        is AuthState.Authenticated -> {
            MainAppScaffold(
                currentUser = state.user,
                repository = repository,
                sessionManager = sessionManager,
                onLogout = { authViewModel.logout() },
                modifier = modifier
            )
        }
    }
}

@Composable
fun MainAppScaffold(
    currentUser: com.example.data.entity.UserEntity,
    repository: AppRepository,
    sessionManager: SessionManager,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNavItem by remember { mutableStateOf(NavigationItem.HOME) }
    var isInAdminPanel by remember { mutableStateOf(false) }
    var isDirectMessagesOpen by remember { mutableStateOf(false) }

    val currentLanguage by sessionManager.appLanguage.collectAsState()

    val feedViewModel: FeedViewModel = remember(currentUser.id) {
        FeedViewModel(repository, currentUser.id)
    }

    val storiesViewModel: StoriesViewModel = remember(currentUser.id) {
        StoriesViewModel(repository, currentUser.id)
    }

    val directMessagesViewModel: DirectMessagesViewModel = remember(currentUser.id) {
        DirectMessagesViewModel(repository, currentUser.id)
    }

    val searchViewModel: SearchViewModel = remember {
        SearchViewModel(repository)
    }

    val uploadViewModel: UploadViewModel = remember(currentUser.id) {
        UploadViewModel(repository, currentUser.id)
    }

    val notificationViewModel: NotificationViewModel = remember(currentUser.id) {
        NotificationViewModel(repository, currentUser.id)
    }

    val profileViewModel: ProfileViewModel = remember(currentUser.id) {
        ProfileViewModel(repository, currentUser.id)
    }

    val adminViewModel: AdminViewModel? = remember(currentUser.id, currentUser.isAdmin) {
        if (currentUser.isAdmin) AdminViewModel(repository, currentUser.id) else null
    }

    val activeCommentPostId by feedViewModel.activeCommentPostId.collectAsState()

    if (isDirectMessagesOpen) {
        DirectMessagesScreen(
            viewModel = directMessagesViewModel,
            onBackClick = { isDirectMessagesOpen = false }
        )
    } else if (isInAdminPanel && adminViewModel != null) {
        AdminScreen(
            viewModel = adminViewModel,
            onBack = { isInAdminPanel = false }
        )
    } else {
        Scaffold(
            bottomBar = {
                ShaghafBottomBar(
                    selectedItem = selectedNavItem,
                    onItemSelected = { selectedNavItem = it }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedNavItem) {
                    NavigationItem.HOME -> {
                        FeedScreen(
                            viewModel = feedViewModel,
                            storiesViewModel = storiesViewModel,
                            onOpenMessages = { isDirectMessagesOpen = true }
                        )
                    }

                    NavigationItem.DISCOVER -> {
                        DiscoverScreen(viewModel = searchViewModel)
                    }

                    NavigationItem.UPLOAD -> {
                        UploadScreen(
                            viewModel = uploadViewModel,
                            onUploadSuccess = {
                                selectedNavItem = NavigationItem.HOME
                            }
                        )
                    }

                    NavigationItem.NOTIFICATIONS -> {
                        NotificationsScreen(viewModel = notificationViewModel)
                    }

                    NavigationItem.PROFILE -> {
                        ProfileScreen(
                            viewModel = profileViewModel,
                            currentLanguage = currentLanguage,
                            onLanguageChanged = { lang -> sessionManager.setLanguage(lang) },
                            onNavigateToAdminPanel = { isInAdminPanel = true },
                            onLogout = onLogout
                        )
                    }
                }

                // Modal Comments Bottom Sheet Overlay
                if (activeCommentPostId != null) {
                    CommentsSheet(
                        postId = activeCommentPostId!!,
                        currentUserId = currentUser.id,
                        isAdmin = currentUser.isAdmin,
                        repository = repository,
                        onClose = { feedViewModel.closeComments() },
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
            }
        }
    }
}
