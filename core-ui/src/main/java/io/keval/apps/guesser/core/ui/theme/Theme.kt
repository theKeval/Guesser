package io.keval.apps.guesser.core.ui.theme

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
    primary = GuesserPrimary,
    onPrimary = GuesserOnPrimary,
    primaryContainer = GuesserPrimaryContainer,
    onPrimaryContainer = GuesserOnPrimaryContainer,
    background = GuesserBackground,
    onBackground = GuesserOnBackground,
    surface = GuesserSurface,
    onSurface = GuesserOnSurface,
    surfaceVariant = GuesserSurfaceVariant,
    onSurfaceVariant = GuesserOnSurfaceVariant,
)

private val DarkColorScheme = darkColorScheme(
    primary = GuesserPrimaryContainer,
    onPrimary = GuesserOnPrimaryContainer,
    primaryContainer = GuesserPrimary,
    onPrimaryContainer = GuesserOnPrimary,
)

@Composable
fun GuesserTheme(
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
        typography = GuesserTypography,
        content = content,
    )
}
