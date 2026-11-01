package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CostGuardianDarkColorScheme = darkColorScheme(
    primary = FinOpsEmeraldPrimary,
    onPrimary = FinOpsEmeraldOn,
    primaryContainer = FinOpsEmeraldContainer,
    onPrimaryContainer = FinOpsEmeraldOnContainer,
    
    secondary = FinOpsCyanSecondary,
    onSecondary = FinOpsCyanOn,
    secondaryContainer = FinOpsCyanContainer,
    onSecondaryContainer = FinOpsCyanOnContainer,
    
    tertiary = FinOpsAmberWarning,
    onTertiary = FinOpsAmberOnContainer,
    tertiaryContainer = FinOpsAmberContainer,
    onTertiaryContainer = FinOpsAmberOnContainer,
    
    background = FinOpsObsidianBg,
    onBackground = FinOpsTextPrimary,
    
    surface = FinOpsSlateSurface,
    onSurface = FinOpsTextPrimary,
    surfaceVariant = FinOpsSurfaceVariant,
    onSurfaceVariant = FinOpsTextSecondary,
    surfaceContainer = FinOpsSurfaceContainer,
    
    outline = FinOpsOutline,
    outlineVariant = FinOpsOutlineVariant,
    
    error = FinOpsRubyCritical,
    onError = FinOpsTextPrimary,
    errorContainer = FinOpsRubyContainer,
    onErrorContainer = FinOpsRubyOnContainer
)

private val CostGuardianLightColorScheme = lightColorScheme(
    primary = FinOpsEmeraldPrimary,
    onPrimary = FinOpsTextPrimary,
    primaryContainer = FinOpsEmeraldContainer,
    onPrimaryContainer = FinOpsEmeraldOnContainer,
    secondary = FinOpsCyanSecondary,
    onSecondary = FinOpsTextPrimary,
    background = FinOpsObsidianBg, // Cyberpunk theme maintains rich dark posture
    onBackground = FinOpsTextPrimary,
    surface = FinOpsSlateSurface,
    onSurface = FinOpsTextPrimary,
    surfaceVariant = FinOpsSurfaceVariant,
    onSurfaceVariant = FinOpsTextSecondary,
    outline = FinOpsOutline
)

@Composable
fun CostGuardianTheme(
    darkTheme: Boolean = true, // Default to cybernetic dark command center
    dynamicColor: Boolean = false, // Keep intentional FinOps brand aesthetics
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> CostGuardianDarkColorScheme
        else -> CostGuardianLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
