package net.irext.ircontrol.ui.screen

sealed interface ControlEvent {
    data class Toast(val message: String) : ControlEvent
}
