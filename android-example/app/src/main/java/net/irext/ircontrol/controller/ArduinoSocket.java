package net.irext.ircontrol.controller;

import android.util.Log;
import android.util.Patterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

/**
 * Filename:       ArduinoSocket.java
 * Revised:        Date: 2026-01-18
 * Revision:       Revision: 1.0
 * <p>
 * Description:    Communication interface to Arduino
 * <p>
 * Revision log:
 *2026-01-18: created by strawmanbobi
 */
public class ArduinoSocket {
    private static final String TAG = ArduinoSocket.class.getSimpleName();

    public static final int EMITTER_DISCONNECTED = 0;
    public static final int EMITTER_CONNECTED = 1;
    public static final int EMITTER_AVAILABLE = 2;
    public static final int EMITTER_BIN_RECEIVED = 3;

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

    public interface IRSocketEmitterCallback {
        void onConnected();
        void onDisconnected();
        void onResponse(String response);
    }

    public ArduinoSocket(IRSocketEmitterCallback callback) {
        this.callback = callback;
    }

    public void setCallback(IRSocketEmitterCallback callback) {
        this.callback = callback;
    }

    public int getConnectionStatus() {
        return connectionStatus;
    }

    public boolean isConnected() {
        return connectionStatus == EMITTER_AVAILABLE;
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
                        if (callback != null) {
                            callback.onResponse(response);
                        }
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

    public void sendDecodedToEmitter(String value) {
        new Thread(() -> {
            try {
                PrintWriter out = new PrintWriter(emitterConn.getOutputStream(), true);
                out.println(value);
            } catch (IOException e) {
                Log.e(TAG, "Error sending decoded data: " + e.getMessage());
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
            } catch (IOException e) {
                Log.e(TAG, "Error sending binary data: " + e.getMessage());
            }
        }).start();
    }

    public void processEHello(String response) {
        sendHelloToEmitter();
    }

    public void processEBin(String response, byte[] binContent, int categoryId, int subCate) {
        sendBinToEmitter(binContent, categoryId, subCate);
    }

    public void processECtrl(String response) {
        // Handle control response if needed
    }

    private void onConnected() {
        if (callback != null) {
            Log.d(TAG, "the emitter is connected");
            callback.onConnected();
        }
    }

    private void onResponse(String response) {
        Log.d(TAG, "emitter: " + response);
        if (response.startsWith(ArduinoSocket.E_RESPONSE_HELLO)) {
            processEHello(response);
        } else if (response.startsWith(ArduinoSocket.E_RESPONSE_BIN)) {

        } else if (response.startsWith(ArduinoSocket.E_RESPONSE_CTRL)) {
            processECtrl(response);
        } else {
            Log.e(TAG, "unexpected response : " + response);
        }
    }
}