package net.irext.ircontrol.compose.controller

import net.irext.ircontrol.compose.bean.RemoteControl
import net.irext.ircontrol.compose.controller.base.ControlCommand


/**
 * Filename:       RemoteController.kt
 * Created:        Date: 2026-07-14
 *
 * Description:    Provides the RemoteController source for the IRControl Android Compose sample.
 *
 * Revision log:
 * 2026-07-14: created by shdmfire and strawmanbobi
 */
sealed interface ControlResult {
    data object Success : ControlResult
    data object Failed : ControlResult
    data object PendingEmitterResult : ControlResult
}

class RemoteController(
    private val phoneRemote: PhoneRemote,
    private val arduinoRemote: ArduinoRemote,
) {
    fun send(
        remoteControl: RemoteControl,
        command: ControlCommand,
    ): ControlResult {
        return if (arduinoRemote.connectionStatus == ArduinoRemote.EMITTER_WORKING) {
            arduinoRemote.irControl(
                remoteControl.categoryId,
                remoteControl.subCategory,
                command.keyCode,
            )
            ControlResult.PendingEmitterResult
        } else {
            val result = phoneRemote.irControl(
                remoteControl.categoryId,
                remoteControl.subCategory,
                command.keyCode,
            )
            if (result == 0) ControlResult.Success else ControlResult.Failed
        }
    }
}
