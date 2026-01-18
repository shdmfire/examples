/**
 *
 * Copyright (c) 2020-2025 IRext Opensource Organization
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

#include <Arduino.h>
#include <WiFiS3.h>
#include <ArduinoGraphics.h>
#include <Arduino_LED_Matrix.h>

#include "configure.h"
#include "remote.h"


#define WIFI_SERVER_PORT (8000)

#define ALIVE_DEBUG_INTERVAL (20 * 1000)

// global variable definitions
constexpr char ssid[] = SECRET_SSID;
constexpr char pass[] = SECRET_PASS;

// commands and events
auto *aHello = "a_hello";
auto *eHello = "e_hello";
auto *aBin = "a_bin";
auto *eBin = "e_bin";
auto *aControl = "a_control";
auto *eControl = "e_control";
auto *aError = "a_error";
auto *eError = "e_error";

int status = WL_IDLE_STATUS;
unsigned long lastStatusCheck = 0;
boolean wifiStatusPrinted = false;
ArduinoLEDMatrix matrix;

WiFiServer server(WIFI_SERVER_PORT);
WiFiClient client;
boolean clientConnected = false;

void drawIp(const char *ipAddr) {
    matrix.beginDraw();

    matrix.stroke(0xFFFFFFFF);
    matrix.textScrollSpeed(100);

    matrix.textFont(Font_5x7);
    matrix.beginText(0, 1, 0xFFFFFF);
    matrix.println(ipAddr);
    matrix.endText(SCROLL_LEFT);
    matrix.endDraw();
}

void printWiFiStatus() {
    unsigned long currentMillis = millis();

    if (currentMillis - lastStatusCheck >= ALIVE_DEBUG_INTERVAL) {
        IPAddress ip = WiFi.localIP();
        if (0 == strcmp(ip.toString().c_str(), "0.0.0.0")) {
            lastStatusCheck = currentMillis;
            return;
        }
        Serial.print("SSID: ");
        Serial.println(WiFi.SSID());

        Serial.print("IP address: ");
        Serial.println(ip);

        const long rssi = WiFi.RSSI();
        Serial.print("Signal strength (RSSI): ");
        Serial.print(rssi);
        Serial.println(" dBm");
        lastStatusCheck = currentMillis;

        if (0 == wifiStatusPrinted) {
            drawIp(ip.toString().c_str());
            wifiStatusPrinted = true;
        }
    }
}

void setup() {
    Serial.begin(115200);
    while (!Serial) {
        delay(100);
    }

    matrix.begin();
    matrix.beginDraw();

    matrix.stroke(0xFFFFFFFF);
    matrix.textScrollSpeed(100);

    constexpr char text[] = "IRext Example";
    matrix.textFont(Font_4x6);
    matrix.beginText(0, 1, 0xFFFFFF);
    matrix.println(text);
    matrix.endText(SCROLL_LEFT);
    matrix.endDraw();

    Serial.println("IRext Arduino example started in station mode");
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(ssid);

    status = WiFi.begin(ssid, pass);

    if (status == WL_CONNECTED) {
        Serial.println("\nConnected to Wi-Fi");
        server.begin();
    }
    else {
        Serial.print("\nFailed to connect Wi-Fi, status: ");
        Serial.println(status);
    }
}

void onConnected(WiFiClient *client) {
    client->flush();
    Serial.println("Client connected");
    client->println(eHello);
}

void onDisconnected(WiFiClient *client) {
    remoteClose();
    client->flush();
    client->stop();
    Serial.println("Client disconnected");
}

void onError(WiFiClient *client) {
    client->flush();
    client->stop();
}

void onCommand(const String *command, WiFiClient *client) {
    if (command->startsWith(aHello)) {
        Serial.println("Received hello command");
        client->println(eBin);
        client->flush();
    } else if (command->startsWith(aBin)) {
        Serial.println("Received bin command");
        if (remoteOpen(command->c_str()) > 0) {
            client->println(eControl);
        } else {
            Serial.println("Failed to parse bin command");
            client->println(eError);
        }
    } else if (command->startsWith(aControl)) {
        Serial.println("Received control command");
        remoteControl(command->c_str());
    } else if (command->startsWith(aError)) {
        Serial.println("Received error command");
        onError(client);
    }
}

void loop() {
    if (WiFi.status() != WL_CONNECTED) {
        Serial.print("Connection lost. Reconnecting...");
        status = WiFi.begin(ssid, pass);
        if (status == WL_CONNECTED) {
            Serial.println("Reconnected!");
        }
    } else {
        printWiFiStatus();
        client = server.available();
        if (client.connected()) {
            if (false == clientConnected) {
                onConnected(&client);
            }
            clientConnected = true;
            if (client.available()) {
                const String received = client.readStringUntil('\n');
                Serial.println(received);
                onCommand(&received, &client);
            }
        } else {
            if (clientConnected) {
                onDisconnected(&client);
            }
            clientConnected = false;
        }
    }
    delay(10);
}
