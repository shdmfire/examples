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

#include <ArduinoJson.h>

#include "serial_log.h"
#include "control_command.h"


// public function definitions
int parseControlCommand(int category, const char *commandJson,
                        t_remote_ac_status *ac_status, int* keyCode) {
    JsonDocument doc;
    DeserializationError error = deserializeJson(doc, commandJson);

    if (error) {
        serialPrint(LOG_ERROR, "Parsing command JSON failed: %s", error.c_str());
        return -1;
    }

    // Parse AC status fields from JSON according to ACStatus structure
    ac_status->ac_power = doc["acStatus"]["acPower"];
    ac_status->ac_temp = doc["acStatus"]["acTemp"];
    ac_status->ac_mode = doc["acStatus"]["acMode"];
    ac_status->ac_wind_dir = doc["acStatus"]["acWindDir"];
    ac_status->ac_wind_speed = doc["acStatus"]["acWindSpeed"];
    ac_status->ac_display = doc["acStatus"]["acDisplay"];
    ac_status->ac_sleep = doc["acStatus"]["acSleep"];
    ac_status->ac_timer = doc["acStatus"]["acTimer"];
    ac_status->change_wind_direction = doc["acStatus"]["changeWindDir"];
    *keyCode = doc["keyCode"];

    serialPrint(LOG_VERBOSE, "--- AC Status ---");
    serialPrint(LOG_VERBOSE, "Power: %d", ac_status->ac_power);
    serialPrint(LOG_VERBOSE, "Temperature: %d", ac_status->ac_temp);
    serialPrint(LOG_VERBOSE, "Mode: %d", ac_status->ac_mode);
    serialPrint(LOG_VERBOSE, "Wind Direction: %d", ac_status->ac_wind_dir);
    serialPrint(LOG_VERBOSE, "Wind Speed: %d", ac_status->ac_wind_speed);
    serialPrint(LOG_VERBOSE, "Display: %d", ac_status->ac_display);
    serialPrint(LOG_VERBOSE, "Sleep: %d", ac_status->ac_sleep);
    serialPrint(LOG_VERBOSE, "Timer: %d", ac_status->ac_timer);
    serialPrint(LOG_VERBOSE, "Change Wind Direction: %d", ac_status->change_wind_direction);
    serialPrint(LOG_VERBOSE, "Key Code: %d", *keyCode);

    return 0;
}