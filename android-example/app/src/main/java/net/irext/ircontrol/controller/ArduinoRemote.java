package net.irext.ircontrol.controller;

import android.content.Context;
import android.util.Log;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.ircontrol.controller.base.IRemote;

import static net.irext.ircontrol.controller.ArduinoSocket.*;

/**
 * Filename:       ArduinoRemote.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Remote implementation by Arduino
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public class ArduinoRemote implements IRemote {

    private static final String TAG = ArduinoRemote.class.getSimpleName();

    Context mContext;
    ArduinoSocket mArduinoSocket;

    public ArduinoRemote(Context context, ArduinoSocket socket) {
        mContext = context;
        mArduinoSocket = socket;
    }

    @Override
    public int irControl(int category, int subCategory, int keyCode) {

        Log.d(TAG, "irControl, category = " + category + ", subCategory = " + subCategory + ", keyCode = " + keyCode);

        ACStatus acStatus = new ACStatus();

        int inputKeyCode = ControlHelper.translateKeyCode(category, keyCode, acStatus);

        ArduinoControlCommand command = new ArduinoControlCommand(inputKeyCode, acStatus);
        String controlCommand = command.toString();
        mArduinoSocket.sendControlToEmitter(controlCommand);

        return 0;
    }
}
