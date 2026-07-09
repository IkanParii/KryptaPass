package com.fachrirasyiq.kryptapass.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachrirasyiq.kryptapass.model.AccountModel
import com.fachrirasyiq.kryptapass.security.BiometricPromptManager
import com.fachrirasyiq.kryptapass.theme.*
import com.fachrirasyiq.kryptapass.viewmodel.AppViewModelProvider
import com.fachrirasyiq.kryptapass.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAddAccount: () -> Unit,
    onNavigateToEditAccount: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricManager = remember(activity) { activity?.let { BiometricPromptManager(it) } }

    var selectedAccountId by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showReverifyDialog by remember { mutableStateOf(false) }
    var reverifyPasswordInput by remember { mutableStateOf("") }
    var reverifyError by remember { mutableStateOf<String?>(null) }
    var isReverifying by remember { mutableStateOf(false) }

    val isInternetError = uiState.error != null && (uiState.error!!.contains("internet", ignoreCase = true) || uiState.error!!.contains("koneksi", ignoreCase = true))
    var showInternetDialog by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showInternetDialog = true
        }
    }

    if (isInternetError && showInternetDialog) {
        InternetErrorDialog(
            onDismiss = { showInternetDialog = false }
        )
    }

    LaunchedEffect(biometricManager) {
        biometricManager?.promptResultsFlow?.collect { result ->
            when (result) {
                is BiometricPromptManager.BiometricResult.AuthenticationSuccess,
                is BiometricPromptManager.BiometricResult.FeatureUnavailable,
                is BiometricPromptManager.BiometricResult.HardwareUnavailable,
                is BiometricPromptManager.BiometricResult.AuthenticationNotSet -> {
                    selectedAccountId?.let { onNavigateToEditAccount(it) }
                    selectedAccountId = null
                }
                is BiometricPromptManager.BiometricResult.AuthenticationError,
                is BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                    selectedAccountId = null
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
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

    if (showReverifyDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!isReverifying) {
                    showReverifyDialog = false
                    reverifyPasswordInput = ""
                    reverifyError = null
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text("Verifikasi Master Password", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            },
            text = {
                Column {
                    Text(
                        "Masukkan master password Anda yang benar untuk membuka proteksi dan melakukan perubahan data.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = reverifyPasswordInput,
                        onValueChange = { 
                            reverifyPasswordInput = it
                            reverifyError = null
                        },
                        label = { Text("Master Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = reverifyError != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = Brand500,
                            unfocusedBorderColor = Border
                        )
                    )
                    if (reverifyError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = reverifyError!!,
                            color = SemanticError,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (reverifyPasswordInput.isBlank()) {
                            reverifyError = "Password tidak boleh kosong"
                            return@Button
                        }
                        isReverifying = true
                        viewModel.verifyAndRestoreMasterPassword(
                            password = reverifyPasswordInput,
                            onSuccess = {
                                isReverifying = false
                                showReverifyDialog = false
                                reverifyPasswordInput = ""
                                reverifyError = null
                            },
                            onFailure = { error ->
                                isReverifying = false
                                reverifyError = error
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !isReverifying
                ) {
                    if (isReverifying) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Verifikasi", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { 
                        showReverifyDialog = false
                        reverifyPasswordInput = ""
                        reverifyError = null
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border),
                    enabled = !isReverifying
                ) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            HomeTopBar(
                accountCount = uiState.accounts.size,
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.onSearchQueryChange(it) },
                onLogoutClick = { showLogoutDialog = true },
                onProfileClick = onNavigateToProfile
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (uiState.decryptionError != null) {
                        showReverifyDialog = true
                    } else {
                        onNavigateToAddAccount()
                    }
                },
                containerColor = Brand500,
                shape = RoundedCornerShape(14.dp),
                contentColor = Color.White,
                modifier = Modifier.size(56.dp),
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp, pressedElevation = 2.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Akun", modifier = Modifier.size(24.dp))
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val dotRadius = 1.dp.toPx()
                val spacing = 24.dp.toPx()
                var x = 0f
                while (x < size.width) {
                    var y = 0f
                    while (y < size.height) {
                        drawCircle(
                            color = Border,
                            radius = dotRadius,
                            center = Offset(x, y)
                        )
                        y += spacing
                    }
                    x += spacing
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Brand500,
                                modifier = Modifier.size(36.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Memuat vault dari cloud...", color = TextTertiary, fontSize = 14.sp)
                        }
                    }
                }

                uiState.error != null && !isInternetError -> {
                    FirestoreErrorState(
                        message = uiState.error!!,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                uiState.accounts.isEmpty() -> {
                    EmptyVaultState(
                        modifier = Modifier.fillMaxSize(),
                        onAddClick = {
                            if (uiState.decryptionError != null) {
                                showReverifyDialog = true
                            } else {
                                onNavigateToAddAccount()
                            }
                        }
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        contentPadding = PaddingValues(bottom = 96.dp, top = 8.dp)
                    ) {
                        val decryptionError = uiState.decryptionError
                        if (decryptionError != null) {
                            item {
                                DecryptionErrorBanner(message = decryptionError)
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                        val groupedAccounts = uiState.accounts.groupBy { it.category }
                        groupedAccounts.forEach { (category, accounts) ->
                            item {
                                CategoryHeader(category = category)
                            }
                            itemsIndexed(accounts) { _, account ->
                                Column {
                                    AccountCard(
                                        account = account,
                                        onClick = {
                                            if (uiState.decryptionError != null) {
                                                showReverifyDialog = true
                                            } else {
                                                selectedAccountId = account.id
                                                // Meminta verifikasi biometrik sebelum membuka detail akun
                                                biometricManager?.showBiometricPrompt(
                                                    title = "Buka Password",
                                                    description = "Gunakan sidik jari untuk melihat password"
                                                ) ?: run {
                                                    onNavigateToEditAccount(account.id)
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    accountCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onLogoutClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .background(SurfaceWhite)
            .border(
                width = 1.dp,
                color = Border,
                shape = RoundedCornerShape(0.dp)
            )
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                    colors = listOf(Brand400, Brand500)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Shield,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Brankas Saya",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        Text(
                            "$accountCount akun tersimpan",
                            fontSize = 12.sp,
                            color = TextTertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            actions = {
                IconButton(onClick = onProfileClick) {
                    Icon(
                        Icons.Default.Fingerprint,
                        "Profil",
                        tint = Brand500,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onLogoutClick) {
                    Icon(
                        Icons.Default.ExitToApp,
                        "Keluar",
                        tint = TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Cari platform, kategori...", fontSize = 13.sp, color = TextTertiary) },
            leadingIcon = {
                Icon(Icons.Default.Search, "Cari", tint = TextTertiary, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 12.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = SurfaceWarm,
                unfocusedContainerColor = SurfaceWarm,
                focusedBorderColor = BorderFocus,
                unfocusedBorderColor = Border,
            ),
            singleLine = true
        )
    }
}

@Composable
private fun FirestoreErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(ErrorBg, RoundedCornerShape(16.dp))
                    .border(1.dp, ErrorBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.WifiOff, null, tint = SemanticError, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Gagal Memuat Data", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                message,
                fontSize = 13.sp,
                color = TextTertiary,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyVaultState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(SurfaceWarm, RoundedCornerShape(16.dp))
                    .border(1.dp, Border, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Lock, null, tint = TextTertiary, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text("Vault Kosong", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Belum ada password yang tersimpan.\nTambah akun pertamamu sekarang.",
                fontSize = 13.sp,
                color = TextTertiary,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                modifier = Modifier.height(44.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Password", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun AccountCard(
    account: AccountModel,
    onClick: () -> Unit
) {
    // Pro-Max Category Mapping
    val categoryProps = when (account.category) {
        "Sosial Media" -> Pair(Color(0xFFE1F3FE), Color(0xFF1F6C9F)) to Icons.Outlined.Chat
        "Keuangan" -> Pair(Color(0xFFEDF3EC), Color(0xFF346538)) to Icons.Outlined.AccountBalanceWallet
        "Hiburan" -> Pair(Color(0xFFF3EBF9), Color(0xFF6B3FA0)) to Icons.Outlined.PlayCircle
        "Pekerjaan" -> Pair(Color(0xFFFBF3DB), Color(0xFF956400)) to Icons.Outlined.WorkOutline
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569)) to Icons.Outlined.Folder
    }
    
    val (colors, icon) = categoryProps
    val (bgColor, iconColor) = colors

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Border, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = account.category,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.platformName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                if (account.username.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(account.username, fontSize = 12.sp, color = TextTertiary)
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(SurfaceWarm)
                        .border(1.dp, Border, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("••••••", fontSize = 10.sp, color = TextTertiary, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Fingerprint,
                        null,
                        tint = Brand500,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("unlock", fontSize = 9.sp, color = TextTertiary, letterSpacing = 0.3.sp)
                }
            }
        }
    }
}

@Composable
fun CategoryHeader(category: String) {
    val icon = categoryIcon(category)
    Row(
        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextTertiary,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = category.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextTertiary,
            letterSpacing = 1.5.sp
        )
    }
}

private fun categoryIcon(category: String): ImageVector = when (category) {
    "Sosial Media" -> Icons.Filled.Face
    "Keuangan"     -> Icons.Filled.AccountBalance
    "Hiburan"      -> Icons.Filled.Movie
    "Pekerjaan"    -> Icons.Filled.Work
    else           -> Icons.Filled.MoreHoriz
}

@Composable
fun DecryptionErrorBanner(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(ErrorBg)
            .border(1.dp, ErrorBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(SemanticError, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = SemanticError,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.weight(1f)
        )
    }
}
