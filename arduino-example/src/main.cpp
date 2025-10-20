//
// Created by strawmanbobi on 10/14/25.
//

#include <Arduino.h>
#include <WiFiS3.h> // Library for the UNO R4 WiFi connectivity
#include "configure.h" // Your secret credentials file

// --- Global Variables ---
// Read credentials from the secrets file
const char ssid[] = SECRET_SSID;
const char pass[] = SECRET_PASS;

int status = WL_IDLE_STATUS;

// --- Function to print connection details ---
void printWiFiStatus() {
  // Print the SSID of the network you're attached to
  Serial.print("SSID: ");
  Serial.println(WiFi.SSID());

  // Print the UNO R4's IP address
  IPAddress ip = WiFi.localIP();
  Serial.print("IP Address: ");
  Serial.println(ip);

  // Print the received signal strength (RSSI)
  long rssi = WiFi.RSSI();
  Serial.print("Signal Strength (RSSI): ");
  Serial.print(rssi);
  Serial.println(" dBm");
}

// ----------------------------------------------------------------------
void setup() {
  Serial.begin(115200);
  while (!Serial); // Wait for serial port to connect

  Serial.println("--- Arduino UNO R4 WiFi: Station Mode ---");

  // Set the board to Wi-Fi Station (client) mode
  // The WiFiS3 library handles this mode implicitly with WiFi.begin()

  // Attempt to connect to the Wi-Fi network
  Serial.print("Attempting to connect to SSID: ");
  Serial.println(ssid);

  // Connect to the Wi-Fi network
  // This is a blocking call that retries until a connection is made or times out
  status = WiFi.begin(ssid, pass);

  if (status == WL_CONNECTED) {
    // If connected successfully
    Serial.println("\n✅ Connection Successful!");
    printWiFiStatus();
  } else {
    // If connection failed
    Serial.print("\n❌ Connection Failed! Status: ");
    Serial.println(status);
  }
}

// ----------------------------------------------------------------------
void loop() {
  // Check WiFi status and attempt to reconnect if disconnected
  if (WiFi.status() != WL_CONNECTED) {
    Serial.print("Connection lost. Reconnecting...");
    status = WiFi.begin(ssid, pass);
    if (status == WL_CONNECTED) {
      Serial.println("Reconnected!");
      printWiFiStatus();
    }
  }

  // Your main application logic goes here
  delay(5000);
}