package net.irext.ircontrol.controller;

import android.content.Context;
import net.irext.ircontrol.controller.implementable.IRemote;

public class ArduinoRemote implements IRemote {

    Context mContext;
    ArduinoSocket mArduinoSocket;

    public ArduinoRemote(Context context, ArduinoSocket socket) {
        mContext = context;
        mArduinoSocket = socket;
    }

    @Override
    public int irControl(int category, int subCategory, int keyCode) {
        return 0;
    }
}
