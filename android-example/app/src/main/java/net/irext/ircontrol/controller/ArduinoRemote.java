package net.irext.ircontrol.controller;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import net.irext.decode.sdk.bean.ACStatus;
import net.irext.ircontrol.controller.base.ControlCommand;
import net.irext.ircontrol.controller.base.ControlHelper;
import net.irext.ircontrol.controller.base.Remote;
import org.jspecify.annotations.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

/**
 * Filename:       ArduinoRemote.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Communication interface to Arduino
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public class ArduinoRemote extends Remote {
    private static final String TAG = ArduinoRemote.class.getSimpleName();

    public static final int EMITTER_DISCONNECTED = 0;
    public static final int EMITTER_CONNECTED = 1;
    public static final int EMITTER_WORKING = 2;

    public static final int EMITTER_PORT = 8000;

    public static final String A_REQUEST_HELLO = "a_hello";
    public static final String E_RESPONSE_HELLO = "e_hello";

    public static final String A_REQUEST_BIN = "a_bin";
    public static final String E_RESPONSE_BIN = "e_bin";

    public static final String A_REQUEST_CTRL = "a_control";
    public static final String E_RESPONSE_CTRL = "e_control";

    private Socket emitterConn = null;
    private int connectionStatus = EMITTER_DISCONNECTED;
    private IRSocketEmitterCallback callback;

    private Context mContext = null;

    private static ArduinoRemote mInstance;

    public static ArduinoRemote getInstance(Context context, IRSocketEmitterCallback callback) {
        if (mInstance == null) {
            mInstance = new ArduinoRemote(context, callback);
        }
        return mInstance;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    public interface IRSocketEmitterCallback {
        void onConnected();
        void onDisconnected();
        void onResponse(String response);
    }

    public ArduinoRemote(Context context, IRSocketEmitterCallback callback) {
        this.mContext = context;
        this.callback = callback;
    }

    public void setCallback(IRSocketEmitterCallback callback) {
        this.callback = callback;
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public void connectToEmitter(String ipAddress, String port) {
        if (connectionStatus == EMITTER_DISCONNECTED) {
            if (ipAddress == null || port == null) {
                return;
            }
            new Thread(() -> {
                try {
                    emitterConn = new Socket(ipAddress, Integer.parseInt(port));
                    emitterConn.setKeepAlive(true);
                    connectionStatus = EMITTER_CONNECTED;

                    onConnected();
                    
                    BufferedReader in = new BufferedReader(new InputStreamReader(emitterConn.getInputStream()));
                    String response;
                    while ((response = in.readLine()) != null) {
                        onResponse(response);
                    }

                    if (callback != null) {
                        callback.onDisconnected();
                    }
                    
                    connectionStatus = EMITTER_DISCONNECTED;
                } catch (IOException ioException) {
                    Log.e(TAG, "Connection error: " + ioException.getMessage());
                    
                    if (callback != null) {
                        callback.onDisconnected();
                    }

                    connectionStatus = EMITTER_DISCONNECTED;
                }
            }).start();
        } else {
            disconnect();
        }
    }

    public void disconnect() {
        try {
            if (emitterConn != null && !emitterConn.isClosed()) {
                emitterConn.close();
            }
            connectionStatus = EMITTER_DISCONNECTED;
        } catch (IOException e) {
            Log.e(TAG, "Error closing connection: " + e.getMessage());
        }
    }

    public void sendHelloToEmitter() {
        new Thread(() -> {
            try {
                Log.d(TAG, "sending a_hello to emitter");
                PrintWriter out = new PrintWriter(emitterConn.getOutputStream(), true);
                out.println(A_REQUEST_HELLO);
            } catch (IOException e) {
                Log.e(TAG, "Error sending hello: " + e.getMessage());
            }
        }).start();
    }

    public void sendBinToEmitter(byte[] binContent, int categoryId, int subCate) {
        if (binContent == null) {
            Log.e(TAG, "binary bytes is null");
            return;
        }
        String binBase64 = Base64.getEncoder().encodeToString(binContent);
        String binStr = A_REQUEST_BIN + "," + categoryId + "," + subCate + "," + binBase64.length() + "," + binBase64;
        Log.d(TAG, "sending bin in base64: " + binStr);
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(emitterConn.getOutputStream(), true);
                out.println(binStr);
            } catch (Exception e) {
                Log.e(TAG, "Error sending binary data: " + e.getMessage());
            }
        }).start();
    }

    public void sendControlToEmitter(String command) {

        String commandStr = A_REQUEST_CTRL + "," + command.length() + "," + command;

        Log.d(TAG, "sending command in base64: " + commandStr);
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(emitterConn.getOutputStream(), true);
                out.println(commandStr);
            } catch (IOException e) {
                Log.e(TAG, "Error sending control data: " + e.getMessage());
            }
        }).start();
    }

    public void sendDecodedToEmitter(String binContent) {
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(emitterConn.getOutputStream(), true);
                out.println(binContent);
            } catch (IOException e) {
                Log.e(TAG, "Error sending decoded data: " + e.getMessage());
            }
        }).start();
    }

    private void onConnected() {
        if (callback != null) {
            Log.d(TAG, "the emitter is connected");
            callback.onConnected();
        }
    }

    private void onResponse(String response) {
        if (response.startsWith(ArduinoRemote.E_RESPONSE_HELLO)) {
            Log.d(TAG, "received e_hello");
        } else if (response.startsWith(ArduinoRemote.E_RESPONSE_BIN)) {
            Log.d(TAG, "received e_bin");
        } else if (response.startsWith(ArduinoRemote.E_RESPONSE_CTRL)) {
            connectionStatus = EMITTER_WORKING;
        } else {
            Log.e(TAG, "unexpected response : " + response);
        }
        callback.onResponse(response);
    }


    public void irControl(int category, int subCategory, int keyCode) {

        Log.d(TAG, "irControl, category = " + category + ", subCategory = " + subCategory + ", keyCode = " + keyCode);

        ACStatus acStatus = new ACStatus();

        int inputKeyCode = ControlHelper.translateKeyCode(category, keyCode, acStatus);

        ArduinoControlCommand command = new ArduinoControlCommand(inputKeyCode, acStatus);
        String controlCommand = command.toString();
        sendControlToEmitter(controlCommand);

    }

    private static class ArduinoControlCommand extends ControlCommand {

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
}