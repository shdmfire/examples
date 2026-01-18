package net.irext.ircontrol.utils;

import android.util.Patterns;

/**
 * Filename:       MiscUtils.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Misc utilities
 * <p>
 * Revision log:
 * 2017-04-08: created by strawmanbobi
 */
public class MiscUtils {

    public static boolean isValidIPv4(String ip) {
        if (ip == null) {
            return false;
        }
        return Patterns.IP_ADDRESS.matcher(ip).matches();
    }

}
