package net.irext.ircontrol.compose.ui.theme

import androidx.compose.ui.graphics.Color


/**
 * Filename:       Color.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the Color source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */

val PrimaryGray = Color(0xFF3F3F3F)      // 您原本的 Primary，深炭灰
val AccentGreen = Color(0xFF4A6F45)      // 略微降低饱和度的抹茶绿（原 Accent 为 0xFF3FAF2F）
val PrimaryDark = Color(0xFF1E1E1E)      // 略带灰度的黑色，比纯黑更护眼、更具质感

val LightPrimary = PrimaryGray
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFF1F1F1) // 浅灰按钮背景
val LightOnPrimaryContainer = Color(0xFF1C1C1C)

val LightSecondary = Color(0xFF5A5F65)       // 低饱和蓝灰
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFE2E6EC)
val LightOnSecondaryContainer = Color(0xFF171D22)

val LightTertiary = AccentGreen
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFD6E4D5) // 极浅的绿，用于柔和的提示
val LightOnTertiaryContainer = Color(0xFF102511)

val LightBackground = Color(0xFFF9F9FA)       // 接近白色的浅灰，不刺眼
val LightOnBackground = Color(0xFF1A1C1E)
val LightSurface = Color(0xFFF9F9FA)
val LightOnSurface = Color(0xFF1A1C1E)
val LightSurfaceVariant = Color(0xFFE1E2E5)   // 卡片与分组背景
val LightOnSurfaceVariant = Color(0xFF44474B) // 次要文字与未激活图标
val LightOutline = Color(0xFF74777F)          // 边框描边

val DarkPrimary = Color(0xFFC6C6C6)
val DarkOnPrimary = Color(0xFF2E2E2E)
val DarkPrimaryContainer = Color(0xFF474747)
val DarkOnPrimaryContainer = Color(0xFFE2E2E2)

val DarkSecondary = Color(0xFFC2C7CE)
val DarkOnSecondary = Color(0xFF2C3137)
val DarkSecondaryContainer = Color(0xFF43474E)
val DarkOnSecondaryContainer = Color(0xFFDFE2E9)

val DarkTertiary = Color(0xFFA5CFA4)
val DarkOnTertiary = Color(0xFF1D3B1C)
val DarkTertiaryContainer = Color(0xFF335232)
val DarkOnTertiaryContainer = Color(0xFFC0ECC0)

val DarkBackground = PrimaryDark
val DarkOnBackground = Color(0xFFE2E2E5)
val DarkSurface = PrimaryDark
val DarkOnSurface = Color(0xFFE2E2E5)
val DarkSurfaceVariant = Color(0xFF44474B)
val DarkOnSurfaceVariant = Color(0xFFC4C7CC)
val DarkOutline = Color(0xFF8E9196)

val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
