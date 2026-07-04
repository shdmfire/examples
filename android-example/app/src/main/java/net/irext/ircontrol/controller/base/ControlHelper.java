package net.irext.ircontrol.controller.base;

import android.content.Context;
import android.hardware.ConsumerIrManager;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.decode.sdk.utils.Constants;
import net.irext.ircontrol.R;
import net.irext.ircontrol.utils.ToastUtils;

import java.util.Objects;

import static net.irext.ircontrol.controller.base.Remote.*;

/**
 * Filename:       ControlHelper.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Remote control helper class
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public class ControlHelper {

    public static int translateKeyCode(int category, int keyCode, ACStatus acStatus) {
        int inputKeyCode = 0;

        if (Constants.CategoryID.AIR_CONDITIONER.getValue() == category) {
            acStatus.setAcPower(Constants.ACPower.POWER_OFF.getValue());
            acStatus.setAcMode(Constants.ACMode.MODE_COOL.getValue());
            acStatus.setAcTemp(Constants.ACTemperature.TEMP_24.getValue());
            acStatus.setAcWindSpeed(Constants.ACWindSpeed.SPEED_AUTO.getValue());
            acStatus.setAcWindDir(Constants.ACSwing.SWING_ON.getValue());
            acStatus.setChangeWindDir(0);
            acStatus.setAcDisplay(0);
            acStatus.setAcTimer(0);
            acStatus.setAcSleep(0);

            switch(keyCode) {
                case KEY_POWER:
                    // power key --> change power
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_POWER.getValue();
                    break;
                case KEY_UP:
                    // up key --> change wind speed
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_WIND_SPEED.getValue();
                    break;
                case KEY_DOWN:
                    // down key --> change wind dir
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_WIND_DIR.getValue();
                    break;
                case KEY_RIGHT:
                    // right key --> change mode
                    inputKeyCode = Constants.ACFunction.FUNCTION_CHANGE_MODE.getValue();
                    break;
                case KEY_OK:
                    // center key --> fix wind dir
                    inputKeyCode = Constants.ACFunction.FUNCTION_SWITCH_SWING.getValue();
                    break;
                case KEY_PLUS:
                    // plus key --> temp up
                    inputKeyCode = Constants.ACFunction.FUNCTION_TEMPERATURE_UP.getValue();
                    break;
                case KEY_MINUS:
                    // minus key --> temp down
                    inputKeyCode = Constants.ACFunction.FUNCTION_TEMPERATURE_DOWN.getValue();
                    break;

                default:
                    return -1;
            }
        } else {
            inputKeyCode = keyCode;
        }
        return inputKeyCode;
    }

    public static void transmitIr(Context context, int []decoded) {
        // debug decoded value
        StringBuilder decodedValue = new StringBuilder();
        for (int i = 0; i < Objects.requireNonNull(decoded).length; i++) {
            decodedValue.append(decoded[i]);
            decodedValue.append(",");
        }
        // send decoded integer array to IR emitter
        ConsumerIrManager irEmitter =
                (ConsumerIrManager) context.getSystemService(Context.CONSUMER_IR_SERVICE);
        if (null != irEmitter && irEmitter.hasIrEmitter()) {
            if (decoded.length > 0) {
                irEmitter.transmit(38000, decoded);
            }
        } else {
            ToastUtils.showToast(context, context.getString(R.string.ir_not_supported), null);
        }
    }
}
