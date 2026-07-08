package net.irext.ircontrol.compose.controller

import net.irext.decode.sdk.bean.ACStatus
import net.irext.decode.sdk.utils.Constants

fun ControlCommand.toDecodeKeyCode(category: Int, acStatus: ACStatus): Int {
    if (Constants.CategoryID.AIR_CONDITIONER.value != category) {
        return keyCode
    }

    acStatus.acPower = Constants.ACPower.POWER_OFF.value
    acStatus.acMode = Constants.ACMode.MODE_COOL.value
    acStatus.acTemp = Constants.ACTemperature.TEMP_24.value
    acStatus.acWindSpeed = Constants.ACWindSpeed.SPEED_AUTO.value
    acStatus.acWindDir = Constants.ACSwing.SWING_ON.value
    acStatus.changeWindDir = 0
    acStatus.acDisplay = 0
    acStatus.acTimer = 0
    acStatus.acSleep = 0

    return when (this) {
        ControlCommand.Power -> Constants.ACFunction.FUNCTION_SWITCH_POWER.value
        ControlCommand.Up -> Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.value
        ControlCommand.Down -> Constants.ACFunction.FUNCTION_SWITCH_WIND_DIR.value
        ControlCommand.Right -> Constants.ACFunction.FUNCTION_CHANGE_MODE.value
        ControlCommand.Ok -> Constants.ACFunction.FUNCTION_SWITCH_SWING.value
        ControlCommand.Plus -> Constants.ACFunction.FUNCTION_TEMPERATURE_UP.value
        ControlCommand.Minus -> Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.value
        ControlCommand.Left,
        ControlCommand.Back,
        ControlCommand.Home,
        ControlCommand.Menu -> -1
    }
}
