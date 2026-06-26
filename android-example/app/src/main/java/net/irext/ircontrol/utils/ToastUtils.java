package net.irext.ircontrol.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Filename:       ToastUtil.java
 * Revised:        Date: 2020-10-24
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Utils of popup
 * <p>
 * Revision log:
 * 2020-10-24: created by strawmanbobi
 */
public class ToastUtils {

    public static void showToast(Context context, String text, Integer specificDuration) {
        int duration = Toast.LENGTH_LONG;

        if (null != specificDuration) {
            duration = specificDuration;
        }

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
}
