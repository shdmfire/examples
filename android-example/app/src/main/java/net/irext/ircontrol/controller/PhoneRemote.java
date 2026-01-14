package net.irext.ircontrol.controller;

import android.content.Context;
import net.irext.decode.sdk.IRDecode;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.ircontrol.controller.implementable.IRemote;

public class PhoneRemote implements IRemote {

    IRDecode mIRDecode;

    Context mContext;
    public PhoneRemote(Context context) {
        mContext = context;
    }

    @Override
    public int irControl(int category, int subCategory, int keyCode) {
        int []decoded;
        ACStatus acStatus = new ACStatus();
        int inputKeyCode = ControlUtils.translateKeyCode(category, keyCode, acStatus);
        decoded = mIRDecode.decodeBinary(inputKeyCode, acStatus);
        ControlUtils.transmitIr(mContext, decoded);
        return 0;
    }
}
