package net.irext.ircontrol.controller;

import android.content.Context;
import android.util.Log;
import net.irext.decode.sdk.IRDecode;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.ircontrol.controller.implementable.IRemote;

/**
 * Filename:       PhoneRemote.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Remote implementation by Android phone
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public class PhoneRemote implements IRemote {

    private static final String TAG = PhoneRemote.class.getSimpleName();

    IRDecode mIRDecode;

    Context mContext;
    public PhoneRemote(Context context) {
        mContext = context;
        mIRDecode = new IRDecode();
    }

    @Override
    public int irControl(int category, int subCategory, int keyCode) {
        int []decoded;
        StringBuilder debugStr = new StringBuilder();
        ACStatus acStatus = new ACStatus();
        int inputKeyCode = ControlHelper.translateKeyCode(category, keyCode, acStatus);
        decoded = mIRDecode.decodeBinary(inputKeyCode, acStatus);

        for (int i = 0; i < decoded.length; i++) {
            debugStr.append(decoded[i]);
            if (i != decoded.length - 1) {
                debugStr.append(",");
            }
        }
        Log.d(TAG, "IR control decoded: " + debugStr);
        ControlHelper.transmitIr(mContext, decoded);
        return 0;
    }
}
