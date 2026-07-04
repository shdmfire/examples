package net.irext.ircontrol.compose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    tertiary = Accent,
)

/**
 * Minimal Material 3 theme for Compose migration.
 * Mirrors the existing XML AppTheme color tokens.
 * Maintains original XML theme for View-based components.
 */
@Composable
fun IRControlTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
