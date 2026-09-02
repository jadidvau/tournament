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
import com.example.data.model.UserRole
import com.example.ui.admin.AdminScreen
import com.example.ui.auth.AuthDialog
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
    var showAuthDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }

    var currentRoute by remember { mutableStateOf(Routes.PLAYER) }

    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
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
                subtitle = "Dhaka eFootball Open",
                currentRole = currentUser?.role ?: UserRole.PLAYER,
                onSwitchRole = { newRole ->
                    viewModel.switchRole(newRole)
                    if (newRole == UserRole.ADMIN) {
                        currentRoute = Routes.ADMIN
                        navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.PLAYER) { saveState = true }
                            launchSingleTop = true
                        }
                    } else {
                        currentRoute = Routes.PLAYER
                        navController.navigate(Routes.PLAYER) {
                            popUpTo(Routes.ADMIN) { saveState = true }
                            launchSingleTop = true
                        }
                    }
                },
                notificationCount = notifications.size,
                onOpenNotifications = { showNotificationDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Slate950
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.PLAYER,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.PLAYER) {
                    PlayerScreen(viewModel = viewModel)
                }

                composable(Routes.ADMIN) {
                    AdminScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            onDismiss = { showAuthDialog = false },
            onLogin = { emailOrPhone, role ->
                viewModel.login(emailOrPhone, role)
                if (role == UserRole.ADMIN) {
                    navController.navigate(Routes.ADMIN)
                }
            }
        )
    }

    if (showNotificationDialog) {
        NotificationDialog(
            notifications = notifications,
            onDismiss = { showNotificationDialog = false }
        )
    }
}
