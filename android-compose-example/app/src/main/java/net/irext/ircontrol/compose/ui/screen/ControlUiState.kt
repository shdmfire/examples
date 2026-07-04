package net.irext.ircontrol.compose.ui.screen


/**
 * Filename:       ControlUiState.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the ControlUiState source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */

data class ControlUiState(
    val title: String = "",
    val emitterIp: String = "",
    val isEmitterConnected: Boolean = false,
    val isLoading: Boolean = false,
)
