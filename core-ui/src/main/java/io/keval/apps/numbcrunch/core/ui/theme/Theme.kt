package io.keval.apps.numbcrunch.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = NumbCrunchPrimary,
    onPrimary = NumbCrunchOnPrimary,
    primaryContainer = NumbCrunchPrimaryContainer,
    onPrimaryContainer = NumbCrunchOnPrimaryContainer,
    background = NumbCrunchBackground,
    onBackground = NumbCrunchOnBackground,
    surface = NumbCrunchSurface,
    onSurface = NumbCrunchOnSurface,
    surfaceVariant = NumbCrunchSurfaceVariant,
    onSurfaceVariant = NumbCrunchOnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = NumbCrunchPrimaryContainer,
    onPrimary = NumbCrunchOnPrimaryContainer,
    primaryContainer = NumbCrunchPrimary,
    onPrimaryContainer = NumbCrunchOnPrimary,
)

@Composable
fun NumbCrunchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NumbCrunchTypography,
        content = content,
    )
}
