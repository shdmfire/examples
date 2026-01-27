package net.irext.ircontrol.controller.base;

/**
 * Filename:       Remote.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Remote interface
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public abstract class Remote {

    public static final int KEY_POWER = 0;
    public static final int KEY_UP = 1;
    public static final int KEY_DOWN = 2;
    public static final int KEY_LEFT = 3;
    public static final int KEY_RIGHT = 4;
    public static final int KEY_OK = 5;
    public static final int KEY_PLUS = 6;
    public static final int KEY_MINUS = 7;
    public static final int KEY_BACK = 8;
    public static final int KEY_HOME = 9;
    public static final int KEY_MENU = 10;

    int irControl(int category, int subCategory, int keyCode) {
        return 0;
    }

}
