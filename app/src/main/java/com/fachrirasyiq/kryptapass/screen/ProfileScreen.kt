package com.fachrirasyiq.kryptapass.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachrirasyiq.kryptapass.security.BiometricPromptManager
import com.fachrirasyiq.kryptapass.theme.*
import com.fachrirasyiq.kryptapass.viewmodel.AppViewModelProvider
import com.fachrirasyiq.kryptapass.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var pendingReveal by remember { mutableStateOf(false) }

    // Menangani hasil verifikasi biometrik untuk menampilkan password terenkripsi
    LaunchedEffect(biometricManager) {
        biometricManager?.promptResultsFlow?.collect { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationSuccess,
                is BiometricPromptManager.BiometricResult.FeatureUnavailable,
                is BiometricPromptManager.BiometricResult.HardwareUnavailable,
                is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                    if (pendingReveal) {
                        viewModel.revealPassword()
                        pendingReveal = false
                    }
                }
                is BiometricPromptManager.BiometricResult.AuthenticationError,
                is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    pendingReveal = false
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Keluar?", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 17.sp)
            },
            text = {
                Text(
                    "Sesi enkripsi akan dikunci. Kamu harus login ulang untuk mengakses password.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                        onNavigateToLogin()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Keluar", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLogoutDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Profil",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Kembali", tint = TextSecondary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                modifier = Modifier.border(width = 1.dp, color = Border, shape = RoundedCornerShape(0.dp))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 1.dp.toPx()
                val spacing = 24.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(color = Border, radius = dotRadius, center = Offset(x, y))
                        y += spacing
                    }
                    x += spacing
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                ProfileHeader(email = uiState.email)

                Spacer(modifier = Modifier.height(20.dp))

                // ── Stat Card: Jumlah Akun ──
                AccountStatCard(
                    count = uiState.accountCount,
                    isLoading = uiState.isLoadingCount
                )

                Spacer(modifier = Modifier.height(16.dp))

                ProfileSectionCard {
                    Text(
                        "INFORMASI AKUN",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextTertiary,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ProfileInfoRow(
                        label = "Email",
                        value = uiState.email,
                        icon = {
                            Icon(Icons.Outlined.Email, null, tint = Brand500, modifier = Modifier.size(16.dp))
                        }
                    )

                    Divider(color = Border, modifier = Modifier.padding(vertical = 12.dp))

                    PasswordInfoRow(
                        isVisible = uiState.isPasswordVisible,
                        password = uiState.password,
                        onToggle = {
                            if (uiState.isPasswordVisible) {
                                viewModel.hidePassword()
                            } else {
                                pendingReveal = true
                                biometricManager?.showBiometricPrompt(
                                    title = "Lihat Password Akun",
                                    description = "Gunakan biometrik untuk melihat password akun Firebase kamu"
                                ) ?: run {
                                    // Fallback jika biometrik tidak tersedia
                                    viewModel.revealPassword()
                                    pendingReveal = false
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                SecurityNoticeCard()

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(
                        Icons.Default.ExitToApp,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Keluar dari Aplikasi", fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeader(email: String) {
    val initial = email.firstOrNull()?.uppercaseChar() ?: 'K'
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    brush = Brush.linearGradient(listOf(Brand400, Brand500)),
                    shape = CircleShape
                )
                .border(3.dp, SurfaceWhite, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.toString(),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = email.ifBlank { "Pengguna KryptaPass" },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(SemanticSuccess, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text("Akun Aktif", fontSize = 12.sp, color = SemanticSuccess, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Stat Card: Jumlah Akun Tersimpan ──
@Composable
private fun AccountStatCard(count: Int, isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                brush = Brush.linearGradient(listOf(Brand500, Color(0xFF1E40AF)))
            )
            .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "VAULT KAMU",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "$count Akun Tersimpan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "AES-256",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
    icon: @Composable () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Brand100, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value.ifBlank { "—" }, fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Password Row dengan toggle biometrik ──
@Composable
private fun PasswordInfoRow(
    isVisible: Boolean,
    password: String,
    onToggle: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(WarningBg, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Lock, null, tint = SemanticWarning, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Password Akun", fontSize = 11.sp, color = TextTertiary, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            AnimatedContent(
                targetState = isVisible,
                transitionSpec = {
                    fadeIn(animationSpec = tween(200)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "password_visibility"
            ) { visible ->
                if (visible) {
                    Text(
                        text = password,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        "••••••••••••",
                        fontSize = 14.sp,
                        color = TextTertiary,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isVisible) Brand100 else SurfaceWarm)
                .border(1.dp, if (isVisible) Brand400 else Border, RoundedCornerShape(8.dp))
        ) {
            IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                if (isVisible) {
                    Icon(
                        Icons.Default.VisibilityOff,
                        "Sembunyikan",
                        tint = Brand500,
                        modifier = Modifier.size(16.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint,
                        "Buka dengan Biometrik",
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }

    if (!isVisible) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Fingerprint,
                null,
                tint = Brand400,
                modifier = Modifier.size(10.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Butuh biometrik untuk membuka",
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
private fun SecurityNoticeCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SuccessBg)
            .border(1.dp, SemanticSuccess.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Shield,
            null,
            tint = SemanticSuccess,
            modifier = Modifier.size(16.dp).padding(top = 1.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                "Zero-Knowledge Encryption",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = SemanticSuccess
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Password kamu dienkripsi dengan AES-256-GCM. Kami tidak pernah melihat atau menyimpan data kamu dalam bentuk terbaca.",
                fontSize = 11.sp,
                color = Color(0xFF166534),
                lineHeight = 16.sp
            )
        }
    }
}
