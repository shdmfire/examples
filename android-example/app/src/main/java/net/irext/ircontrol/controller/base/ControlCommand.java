package net.irext.ircontrol.controller.base;

import net.irext.decode.sdk.bean.ACStatus;

/**
 * Filename:       ControlCommand.java
 * Revised:        Date: 2026-01-22
 * Revision:       Revision: 1.0
 * <p>
 * Description:    ControlCommand base class
 * <p>
 * Revision log:
 * 2026-01-22: created by strawmanbobi
 */
public abstract class ControlCommand {

    public static final ControlCommand POWER = create(Remote.KEY_POWER, "POWER");
    public static final ControlCommand UP = create(Remote.KEY_UP, "UP");
    public static final ControlCommand DOWN = create(Remote.KEY_DOWN, "DOWN");
    public static final ControlCommand LEFT = create(Remote.KEY_LEFT, "LEFT");
    public static final ControlCommand RIGHT = create(Remote.KEY_RIGHT, "RIGHT");
    public static final ControlCommand OK = create(Remote.KEY_OK, "OK");
    public static final ControlCommand PLUS = create(Remote.KEY_PLUS, "PLUS");
    public static final ControlCommand MINUS = create(Remote.KEY_MINUS, "MINUS");
    public static final ControlCommand BACK = create(Remote.KEY_BACK, "BACK");
    public static final ControlCommand HOME = create(Remote.KEY_HOME, "HOME");
    public static final ControlCommand MENU = create(Remote.KEY_MENU, "MENU");

    protected int keyCode;
    protected ACStatus acStatus;

    private static ControlCommand create(int keyCode, String name) {
        return new ControlCommand() {
            {
                this.keyCode = keyCode;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    public int getKeyCode() {
        return keyCode;
    }

    public abstract String toString();

}
