package com.fachrirasyiq.kryptapass.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.fachrirasyiq.kryptapass.screen.AddEditAccountScreen
import com.fachrirasyiq.kryptapass.screen.HomeScreen
import com.fachrirasyiq.kryptapass.screen.LoginScreen
import com.fachrirasyiq.kryptapass.screen.ProfileScreen
import com.fachrirasyiq.kryptapass.screen.InternetErrorDialog
import com.fachrirasyiq.kryptapass.security.SessionManager
import com.fachrirasyiq.kryptapass.theme.*

/**
 * Screen adalah kelas sealed yang mendefinisikan semua rute navigasi (layar)
 * yang tersedia di dalam aplikasi.
 */
sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object AddEditAccount : Screen("add_edit_account?accountId={accountId}") {
        fun createRoute(accountId: String?) = "add_edit_account?accountId=${accountId ?: ""}"
    }
}

@Composable
fun KryptaNavGraph(
    sessionManager: SessionManager,
    networkMonitor: com.fachrirasyiq.kryptapass.security.NetworkMonitor,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Login.route
) {
    val isSessionActive by sessionManager.isSessionActiveFlow.collectAsState()
    val sessionExpired by sessionManager.sessionExpiredFlow.collectAsState()

    val isOnline by networkMonitor.isOnline.collectAsState(initial = true)
    var showOfflineDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isOnline) {
        if (!isOnline) {
            showOfflineDialog = true
        }
    }

    if (!isOnline && showOfflineDialog) {
        InternetErrorDialog(
            onDismiss = { showOfflineDialog = false }
        )
    }

    // Tangani sesi timeout — navigasi ke Login hanya jika tidak ada dialog sesi yang tampil
    LaunchedEffect(isSessionActive) {
        if (!isSessionActive && !sessionExpired) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute != null && currentRoute != Screen.Login.route) {
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    // Dialog Sesi Berakhir — ditampilkan di atas semuanya
    if (sessionExpired) {
        SessionExpiredDialog(
            onLoginClick = {
                sessionManager.consumeSessionExpired()
                navController.navigate(Screen.Login.route) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToAddAccount = {
                    navController.navigate(Screen.AddEditAccount.createRoute(null))
                },
                onNavigateToEditAccount = { accountId ->
                    navController.navigate(Screen.AddEditAccount.createRoute(accountId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(
            route = Screen.AddEditAccount.route,
            arguments = listOf(navArgument("accountId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")?.takeIf { it.isNotBlank() }
            AddEditAccountScreen(
                accountId = accountId,
                isOnline = isOnline,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}


@Composable
fun SessionExpiredDialog(onLoginClick: () -> Unit) {
    Dialog(
        onDismissRequest = { /* Tidak dapat ditutup — pengguna harus menekan tombol */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(tween(300)) + scaleIn(tween(300), initialScale = 0.9f),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceWhite)
                    .border(1.dp, Border, RoundedCornerShape(20.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Brand400, Brand500)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .border(
                                2.dp,
                                Brand100,
                                RoundedCornerShape(20.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        "Sesi Berakhir",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = (-0.3).sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        "Kamu tidak aktif selama 5 menit.\nVault dikunci otomatis untuk keamanan.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Brand100)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .background(Brand500, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Kunci enkripsi dihapus dari memori",
                            fontSize = 10.sp,
                            color = BrandText,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onLoginClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text(
                            "Login Ulang",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
