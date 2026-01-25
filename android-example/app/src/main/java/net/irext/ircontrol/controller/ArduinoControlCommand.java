package net.irext.ircontrol.controller;

import com.google.gson.Gson;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.ircontrol.controller.base.ControlCommand;
import org.jspecify.annotations.NonNull;

import java.util.Base64;

/**
 * Filename:       ControlCommand.java
 * Revised:        Date: 2026-01-22
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Remote command to Arduino remote
 * <p>
 * Revision log:
 * 2026-01-22: created by strawmanbobi
 */
public class ArduinoControlCommand extends ControlCommand {

    public ArduinoControlCommand(int keyCode, ACStatus acStatus) {
        this.keyCode = keyCode;
        this.acStatus = acStatus;
    }

    public ArduinoControlCommand() {
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public ACStatus getAcStatus() {
        return acStatus;
    }

    public void setAcStatus(ACStatus acStatus) {
        this.acStatus = acStatus;
    }

    @Override
    public @NonNull String toString() {
        String jsonStr = new Gson().toJson(this);
        return Base64.getEncoder().encodeToString(jsonStr.getBytes());
    }
}
