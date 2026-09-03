package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.ORGANIZER_EMAIL
import com.example.data.model.UserRole
import com.example.ui.admin.AdminScreen
import com.example.ui.auth.AboutDeveloperDialog
import com.example.ui.auth.AuthDialog
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.DeveloperBottomBar
import com.example.ui.auth.NotificationDialog
import com.example.ui.components.EsportsHeader
import com.example.ui.player.PlayerScreen
import com.example.ui.theme.Slate950
import com.example.ui.viewmodel.TournamentViewModel

object Routes {
    const val PLAYER = "player"
    const val ADMIN = "admin"
}

@Composable
fun AppNavigation(
    viewModel: TournamentViewModel = viewModel()
) {
    val navController = rememberNavController()
    val currentUser by viewModel.currentUser.collectAsState()
    val tournament by viewModel.tournament.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val isDesignatedAdmin = currentUser?.email?.trim().equals(ORGANIZER_EMAIL, ignoreCase = true) == true

    var currentRoute by remember(isDesignatedAdmin) {
        mutableStateOf(if (isDesignatedAdmin) Routes.ADMIN else Routes.PLAYER)
    }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // MANDATORY AUTH SPLASH / GATEKEEPER
    // If not authenticated, immediately show AuthScreen. Users MUST NOT access dashboard or fixtures.
    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            onAuthSuccess = { isAdminUser ->
                currentRoute = if (isAdminUser) Routes.ADMIN else Routes.PLAYER
            }
        )
        return
    }

    // Role-based navigation lock: Ensure non-admins cannot stay on Admin screen
    LaunchedEffect(currentUser) {
        if (!isDesignatedAdmin && currentRoute == Routes.ADMIN) {
            currentRoute = Routes.PLAYER
            try {
                navController.navigate(Routes.PLAYER) {
                    popUpTo(Routes.ADMIN) { inclusive = true }
                    launchSingleTop = true
                }
            } catch (e: Throwable) {
                android.util.Log.w("AppNavigation", "Navigation error: ${e.message}")
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .navigationBarsPadding(),
        topBar = {
            EsportsHeader(
                title = tournament.title,
                subtitle = "Dhaka eFootball",
                isAdminUser = isDesignatedAdmin,
                currentView = currentRoute,
                onToggleView = { targetView ->
                    if (targetView == "admin" && isDesignatedAdmin) {
                        currentRoute = Routes.ADMIN
                        try {
                            navController.navigate(Routes.ADMIN) {
                                popUpTo(Routes.PLAYER) { saveState = true }
                                launchSingleTop = true
                            }
                        } catch (e: Throwable) {
                            android.util.Log.w("AppNavigation", "Navigation error: ${e.message}")
                        }
                    } else {
                        currentRoute = Routes.PLAYER
                        try {
                            navController.navigate(Routes.PLAYER) {
                                popUpTo(Routes.ADMIN) { saveState = true }
                                launchSingleTop = true
                            }
                        } catch (e: Throwable) {
                            android.util.Log.w("AppNavigation", "Navigation error: ${e.message}")
                        }
                    }
                },
                notificationCount = notifications.size,
                onOpenNotifications = { showNotificationDialog = true },
                onLogout = {
                    viewModel.logout()
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            DeveloperBottomBar(
                onOpenAbout = { showAboutDialog = true }
            )
        },
        containerColor = Slate950
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (isDesignatedAdmin) Routes.ADMIN else Routes.PLAYER,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.PLAYER) {
                    PlayerScreen(viewModel = viewModel)
                }

                composable(Routes.ADMIN) {
                    AdminScreen(
                        viewModel = viewModel,
                        onReturnToPlayer = {
                            currentRoute = Routes.PLAYER
                            try {
                                navController.navigate(Routes.PLAYER) {
                                    popUpTo(Routes.ADMIN) { inclusive = true }
                                    launchSingleTop = true
                                }
                            } catch (e: Throwable) {
                                android.util.Log.w("AppNavigation", "Navigation error: ${e.message}")
                            }
                        }
                    )
                }
            }
        }
    }

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false }
        )
    }

    if (showAboutDialog) {
        AboutDeveloperDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}
