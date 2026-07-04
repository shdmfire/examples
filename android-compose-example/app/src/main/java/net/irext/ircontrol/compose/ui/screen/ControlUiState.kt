package net.irext.ircontrol.compose.ui.screen

data class ControlUiState(
    val title: String = "",
    val emitterIp: String = "",
    val isEmitterConnected: Boolean = false,
    val isLoading: Boolean = false,
)
