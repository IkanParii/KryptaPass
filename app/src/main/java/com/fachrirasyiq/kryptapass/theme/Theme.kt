package com.fachrirasyiq.kryptapass.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val KryptaLightColorScheme = lightColorScheme(
    primary             = Brand500,
    onPrimary           = Color.White,
    primaryContainer    = Brand100,
    onPrimaryContainer  = BrandText,

    secondary           = Accent500,
    onSecondary         = Color.White,
    secondaryContainer  = WarningBg,
    onSecondaryContainer= SemanticWarning,

    tertiary            = Brand400,
    onTertiary          = Color.White,
    tertiaryContainer   = Brand100,
    onTertiaryContainer = BrandText,

    error               = Error,
    onError             = Color.White,
    errorContainer      = ErrorBg,
    onErrorContainer    = SemanticError,

    background          = Canvas,
    onBackground        = TextPrimary,

    surface             = SurfaceWhite,
    onSurface           = TextPrimary,
    surfaceVariant      = SurfaceWarm,
    onSurfaceVariant    = TextSecondary,

    outline             = Border,
    outlineVariant      = Color(0xFFF0EFED),

    inverseSurface      = TextPrimary,
    inverseOnSurface    = SurfaceWhite,
    inversePrimary      = Brand400,

    surfaceTint         = Brand500,
    scrim               = Color(0x33000000)
)

@Composable
fun KryptapassTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = KryptaLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SurfaceWhite.toArgb()
            window.navigationBarColor = Canvas.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
