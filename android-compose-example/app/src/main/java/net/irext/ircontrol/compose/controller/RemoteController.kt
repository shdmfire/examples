package net.irext.ircontrol.compose.controller

import net.irext.ircontrol.compose.bean.RemoteControl

class RemoteController(
    private val phoneRemote: PhoneRemote,
    private val arduinoRemote: ArduinoRemote,
) {
    suspend fun send(
        remoteControl: RemoteControl,
        command: ControlCommand,
    ): ControlResult {
        return if (arduinoRemote.status.value == EmitterStatus.Working) {
            arduinoRemote.control(
                remoteControl.categoryId,
                remoteControl.subCategory,
                command,
            )
        } else {
            phoneRemote.control(
                remoteControl.categoryId,
                remoteControl.subCategory,
                command,
            )
        }
    }
}

enum class ControlResult {
    Success,
    Failed,
    PendingEmitterResult,
}
