package com.fachrirasyiq.kryptapass.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachrirasyiq.kryptapass.model.AccountModel
import com.fachrirasyiq.kryptapass.security.PasswordCheckResult
import com.fachrirasyiq.kryptapass.theme.*
import com.fachrirasyiq.kryptapass.viewmodel.AppViewModelProvider
import com.fachrirasyiq.kryptapass.viewmodel.PasswordDetailIntent
import com.fachrirasyiq.kryptapass.viewmodel.PasswordDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAccountScreen(
    accountId: String?,
    isOnline: Boolean = true,
    onNavigateBack: () -> Unit,
    viewModel: PasswordDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showGeneratorSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showDiscardDialog = true
    }

    LaunchedEffect(accountId) {
        if (accountId != null) {
            viewModel.handleIntent(PasswordDetailIntent.LoadAccount(accountId))
        }
    }

    LaunchedEffect(uiState.saveSuccess, uiState.deleteSuccess) {
        if (uiState.saveSuccess || uiState.deleteSuccess) {
            onNavigateBack()
        }
    }

    if (showGeneratorSheet) {
        PasswordGeneratorSheet(
            onDismiss = { showGeneratorSheet = false },
            onPasswordGenerated = { generatedPassword ->
                viewModel.handleIntent(PasswordDetailIntent.OnPasswordChange(generatedPassword))
                isPasswordVisible = true
                showGeneratorSheet = false
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    "Hapus Akun?",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    "Password untuk \"${uiState.platformName}\" akan dihapus secara permanen dan tidak dapat dikembalikan.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.handleIntent(PasswordDetailIntent.DeleteAccount)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                    shape = RoundedCornerShape(8.dp),
                    enabled = !uiState.isDeleting
                ) {
                    if (uiState.isDeleting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Hapus Permanen", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDeleteDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text("Batal", color = TextSecondary)
                }
            }
        )
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(12.dp),
            title = {
                Text(
                    "Batalkan Perubahan?",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                )
            },
            text = {
                Text(
                    if (accountId == null) "Anda belum menyimpan data ini. Yakin ingin batal menambahkan akun?" else "Perubahan yang Anda buat belum disimpan. Yakin ingin kembali tanpa menyimpan?",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SemanticError),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ya, Batalkan", fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showDiscardDialog = false },
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Text("Lanjut Edit", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = Canvas,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (accountId == null) "Tambah Akun" else "Detail Akun",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            letterSpacing = (-0.3).sp
                        )
                        if (accountId != null && uiState.platformName.isNotBlank()) {
                            Text(
                                text = uiState.platformName,
                                fontSize = 12.sp,
                                color = BrandText
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.hasUnsavedChanges) {
                            showDiscardDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = TextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    if (accountId != null) {
                        IconButton(
                            onClick = { showDeleteDialog = true },
                            enabled = isOnline
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Hapus",
                                tint = if (isOnline) SemanticError else Surface500,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite),
                modifier = Modifier.border(1.dp, Border)
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // ── Section: Platform Information ──
            FormSection(title = "Informasi Platform") {
                KryptaTextField(
                    value = uiState.platformName,
                    onValueChange = { viewModel.handleIntent(PasswordDetailIntent.OnPlatformNameChange(it)) },
                    label = "Nama Platform (mis. Instagram)"
                )
                Spacer(modifier = Modifier.height(16.dp))

                var expanded by remember { mutableStateOf(false) }
                val categories = listOf("Sosial Media", "Keuangan", "Hiburan", "Pekerjaan", "Lainnya")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = uiState.category,
                        onValueChange = {},
                        readOnly = true,
                        label = {
                            Text("Kategori", fontSize = 13.sp, color = TextTertiary)
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceWarm,
                            unfocusedContainerColor = SurfaceWarm,
                            focusedBorderColor = BorderFocus,
                            unfocusedBorderColor = Border,
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(SurfaceWhite).border(1.dp, Border, RoundedCornerShape(8.dp))
                    ) {
                        categories.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(option, color = TextPrimary, fontSize = 14.sp)
                                        if (option == uiState.category) {
                                            Spacer(modifier = Modifier.weight(1f))
                                            Icon(
                                                Icons.Default.Check,
                                                null,
                                                tint = Brand500,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.handleIntent(PasswordDetailIntent.OnCategoryChange(option))
                                    expanded = false
                                },
                                colors = MenuItemColors(
                                    textColor = TextPrimary,
                                    leadingIconColor = TextSecondary,
                                    trailingIconColor = TextSecondary,
                                    disabledTextColor = TextTertiary,
                                    disabledLeadingIconColor = TextTertiary,
                                    disabledTrailingIconColor = TextTertiary
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormSection(title = "Kredensial") {
                KryptaTextField(
                    value = uiState.username,
                    onValueChange = { viewModel.handleIntent(PasswordDetailIntent.OnUsernameChange(it)) },
                    label = "Username / Email"
                )
                Spacer(modifier = Modifier.height(16.dp))
                KryptaTextField(
                    value = uiState.url,
                    onValueChange = { viewModel.handleIntent(PasswordDetailIntent.OnUrlChange(it)) },
                    label = "URL Website (Opsional)"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormSection(title = "Kata Sandi") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        KryptaTextField(
                            value = uiState.passwordInput,
                            onValueChange = { viewModel.handleIntent(PasswordDetailIntent.OnPasswordChange(it)) },
                            label = "Kata Sandi",
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        null,
                                        tint = TextTertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brand500)
                            .clickable { showGeneratorSheet = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "Buat Password",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                StrongPasswordChecker(result = uiState.checkResult)
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormSection(title = "Catatan") {
                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = { viewModel.handleIntent(PasswordDetailIntent.OnNotesChange(it)) },
                    label = {
                        Text("Catatan (Opsional)", fontSize = 13.sp, color = TextTertiary)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = SurfaceWarm,
                        unfocusedContainerColor = SurfaceWarm,
                        focusedBorderColor = BorderFocus,
                        unfocusedBorderColor = Border,
                        cursorColor = Brand500,
                        focusedLabelColor = BrandText,
                        unfocusedLabelColor = TextTertiary
                    )
                )
            }

            // Error message
            val hasInternetError = uiState.error != null && (uiState.error!!.contains("internet", ignoreCase = true) || uiState.error!!.contains("koneksi", ignoreCase = true))
            if (hasInternetError) {
                InternetErrorDialog(
                    onDismiss = { viewModel.handleIntent(PasswordDetailIntent.ClearError) }
                )
            } else if (uiState.error != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorBg)
                        .border(1.dp, ErrorBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Info,
                        null,
                        tint = SemanticError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = uiState.error!!,
                        color = SemanticError,
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )
                }
            }
            if (!isOnline) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ErrorBg)
                        .border(1.dp, ErrorBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WifiOff,
                        null,
                        tint = SemanticError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Offline - Penyimpanan Dinonaktifkan",
                        color = SemanticError,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Tombol Simpan ──
            Button(
                onClick = { viewModel.handleIntent(PasswordDetailIntent.SaveAccount) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !uiState.isSaving && !uiState.isDeleting && isOnline,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Brand500, // Changed from TextPrimary to Blue Accent
                    disabledContainerColor = Surface500
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Simpan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite)
            .border(1.dp, Border, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Border)
        )

        Column(
            modifier = Modifier.padding(20.dp),
            content = content
        )
    }
}

@Composable
fun StrongPasswordChecker(
    result: PasswordCheckResult?,
    modifier: Modifier = Modifier
) {
    if (result == null || result.checks.isEmpty()) return

    val strengthColor = when (result.score) {
        1 -> SemanticError
        2 -> SemanticWarning
        3 -> SemanticSuccess
        else -> TextTertiary
    }
    val strengthLabel = when (result.score) {
        1 -> "Lemah"
        2 -> "Cukup Kuat"
        3 -> "Sangat Kuat"
        else -> ""
    }
    val bgColor = when (result.score) {
        1 -> ErrorBg
        2 -> WarningBg
        3 -> SuccessBg
        else -> SurfaceWarm
    }
    val borderColor = when (result.score) {
        1 -> ErrorBorder
        2 -> Color(0xFFFDE68A)
        3 -> Color(0xFFBBF7D0)
        else -> Border
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Kekuatan Sandi",
                fontSize = 13.sp,
                color = strengthColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = strengthLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = strengthColor
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (i <= result.score) strengthColor else strengthColor.copy(alpha = 0.2f))
                )
            }
        }

        if (result.crackTimeDisplay.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "⏱ Estimasi waktu crack: ${result.crackTimeDisplay}",
                fontSize = 12.sp,
                color = strengthColor.copy(alpha = 0.8f),
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        var showDetails by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier
                .clickable { showDetails = !showDetails }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Info,
                null,
                tint = strengthColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (showDetails) "Sembunyikan Detail" else "Lihat Detail Keamanan",
                fontSize = 12.sp,
                color = strengthColor,
                fontWeight = FontWeight.SemiBold
            )
        }

        AnimatedVisibility(
            visible = showDetails,
            enter = expandVertically(tween(200)),
            exit = shrinkVertically(tween(200))
        ) {
            Column(modifier = Modifier.padding(top = 12.dp)) {
                result.checks.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = if (item.passed) "✓" else "✗",
                            fontSize = 12.sp,
                            color = if (item.passed) SemanticSuccess else SemanticError,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(20.dp)
                        )
                        Text(
                            text = item.label,
                            fontSize = 12.sp,
                            color = if (item.passed) TextPrimary else TextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                        if (item.detail != null) {
                            Text(
                                text = item.detail,
                                fontSize = 11.sp,
                                color = SemanticError
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = strengthColor.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Entropi",
                        fontSize = 12.sp,
                        color = strengthColor
                    )
                    Text(
                        text = "${result.entropy.toInt()} bit",
                        fontSize = 12.sp,
                        color = strengthColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorSheet(
    onDismiss: () -> Unit,
    onPasswordGenerated: (String) -> Unit
) {
    var length by remember { mutableStateOf(16f) }
    var useUppercase by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Border, RoundedCornerShape(2.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Brand100, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        null,
                        tint = BrandText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        "Generator Kata Sandi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        "Buat password yang kuat",
                        fontSize = 13.sp,
                        color = TextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceWarm)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Panjang Kata Sandi",
                            fontSize = 14.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Box(
                            modifier = Modifier
                                .background(SurfaceWhite, RoundedCornerShape(6.dp))
                                .border(1.dp, Border, RoundedCornerShape(6.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "${length.toInt()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Slider(
                        value = length,
                        onValueChange = { length = it },
                        valueRange = 8f..32f,
                        steps = 23,
                        colors = SliderDefaults.colors(
                            thumbColor = Brand500,
                            activeTrackColor = Brand500,
                            inactiveTrackColor = Border
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("8", fontSize = 12.sp, color = TextTertiary)
                        Text("32", fontSize = 12.sp, color = TextTertiary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceWarm)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
            ) {
                Column {
                    listOf(
                        Triple("Huruf Besar (A-Z)", useUppercase, { v: Boolean -> useUppercase = v }),
                        Triple("Angka (0-9)", useNumbers, { v: Boolean -> useNumbers = v }),
                        Triple("Simbol (!@#$)", useSymbols, { v: Boolean -> useSymbols = v })
                    ).forEachIndexed { index, (label, checked, onCheck) ->
                        if (index > 0) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = Border
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onCheck(!checked) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                label,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Switch(
                                checked = checked,
                                onCheckedChange = onCheck,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Brand500,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Border
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val generated = generatePassword(length.toInt(), useUppercase, useNumbers, useSymbols)
                    onPasswordGenerated(generated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Buat Password",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}

fun generatePassword(length: Int, useUppercase: Boolean, useNumbers: Boolean, useSymbols: Boolean): String {
    val lowercase = "abcdefghijklmnopqrstuvwxyz"
    val uppercase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    val numbers = "0123456789"
    val symbols = "!@#\$%^&*()-_=+[]{}|;:,.<>?"

    var charPool = lowercase
    if (useUppercase) charPool += uppercase
    if (useNumbers) charPool += numbers
    if (useSymbols) charPool += symbols

    if (charPool.isEmpty()) charPool = lowercase

    return (1..length)
        .map { charPool.random() }
        .joinToString("")
}
