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

    protected int keyCode;
    protected ACStatus acStatus;

    public abstract String toString();

}
