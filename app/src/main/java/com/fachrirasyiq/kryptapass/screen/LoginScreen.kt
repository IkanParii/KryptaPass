package com.fachrirasyiq.kryptapass.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fachrirasyiq.kryptapass.theme.*
import com.fachrirasyiq.kryptapass.viewmodel.AppViewModelProvider
import com.fachrirasyiq.kryptapass.viewmodel.AuthIntent
import com.fachrirasyiq.kryptapass.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isMasterPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onNavigateToHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Canvas)
    ) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(64.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(Brand100, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = Brand500,
                modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "KryptaPass",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (uiState.isRegisterMode) "Buat akun baru untuk memulai" else "Selamat datang kembali",
            fontSize = 14.sp,
            color = TextTertiary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceWhite)
                .border(1.dp, Border, RoundedCornerShape(12.dp))
                .padding(20.dp)
        ) {
            Text(
                text = if (uiState.isRegisterMode) "BUAT AKUN" else "MASUK",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextTertiary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            KryptaTextField(
                value = uiState.emailInput,
                onValueChange = { viewModel.handleIntent(AuthIntent.OnEmailChange(it)) },
                label = "Email",
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(12.dp))

            KryptaTextField(
                value = uiState.passwordInput,
                onValueChange = { viewModel.handleIntent(AuthIntent.OnPasswordChange(it)) },
                label = "Kata Sandi Cloud",
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            null,
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardType = KeyboardType.Password
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(WarningBg)
                    .border(1.dp, Border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Shield,
                        null,
                        tint = SemanticWarning,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        "KUNCI ENKRIPSI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SemanticWarning,
                        letterSpacing = 1.2.sp
                    )
                }
                KryptaTextField(
                    value = uiState.masterPasswordInput,
                    onValueChange = { viewModel.handleIntent(AuthIntent.OnMasterPasswordChange(it)) },
                    label = "Master Password",
                    trailingIcon = {
                        IconButton(onClick = { isMasterPasswordVisible = !isMasterPasswordVisible }) {
                            Icon(
                                if (isMasterPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                null,
                                tint = TextTertiary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    visualTransformation = if (isMasterPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardType = KeyboardType.Password
                )
            }

            val hasInternetError = uiState.error != null && (uiState.error!!.contains("internet", ignoreCase = true) || uiState.error!!.contains("koneksi", ignoreCase = true))
            if (hasInternetError) {
                InternetErrorDialog(
                    onDismiss = { viewModel.handleIntent(AuthIntent.ClearError) }
                )
            } else if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
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
                        text = uiState.error!!,
                        color = SemanticError,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.handleIntent(AuthIntent.Submit) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = !uiState.isLoading,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Brand500,
                disabledContainerColor = Surface500
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 0.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (uiState.isRegisterMode) "Daftar Sekarang" else "Masuk ke Vault",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    letterSpacing = 0.2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { viewModel.handleIntent(AuthIntent.ToggleMode) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (uiState.isRegisterMode) "Sudah punya akun?  " else "Belum punya akun?  ",
                color = TextTertiary,
                fontSize = 13.sp
            )
            Text(
                text = if (uiState.isRegisterMode) "Login" else "Daftar",
                color = BrandText,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SecurityBadge(label = "AES-256-GCM")
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(10.dp).background(Border))
            Spacer(modifier = Modifier.width(12.dp))
            SecurityBadge(label = "E2E ENCRYPTED")
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.width(1.dp).height(10.dp).background(Border))
            Spacer(modifier = Modifier.width(12.dp))
            SecurityBadge(label = "ZERO-KNOWLEDGE")
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}
}

@Composable
private fun SecurityBadge(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp)
                .background(Brand500, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = TextTertiary,
            letterSpacing = 0.6.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun KryptaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextTertiary
            )
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedContainerColor = SurfaceWhite,
            unfocusedContainerColor = SurfaceWarm,
            focusedBorderColor = BorderFocus,
            unfocusedBorderColor = Border,
            cursorColor = Brand500,
            focusedLabelColor = BrandText,
            unfocusedLabelColor = TextTertiary
        ),
        singleLine = true
    )
}

@Composable
fun InternetErrorDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                Icons.Default.WifiOff,
                contentDescription = null,
                tint = SemanticError,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text(
                text = "Koneksi Terputus",
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp
            )
        },
        text = {
            Text(
                text = "KryptaPass memerlukan koneksi internet untuk mengamankan dan menyinkronkan data Anda. Harap periksa koneksi Wi-Fi atau data seluler Anda.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Brand500),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Mengerti", fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    )
}
