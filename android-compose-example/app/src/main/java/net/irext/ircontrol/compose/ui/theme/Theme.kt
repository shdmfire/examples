package net.irext.ircontrol.compose.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable


/**
 * Filename:       Theme.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the Theme source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */
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
