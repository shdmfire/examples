package net.irext.ircontrol.compose.ui.screen

sealed interface ControlEvent {
    data class Toast(val message: String) : ControlEvent
}
