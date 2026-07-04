package net.irext.ircontrol.compose.ui.screen


/**
 * Filename:       ControlEvent.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the ControlEvent source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */

sealed interface ControlEvent {
    data class Toast(val message: String) : ControlEvent
}
