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
#include <cstdarg>
#include <cstdio>
#include <cstring>

#include "serial_log.h"

#define LOG_BUF_SIZE    (128)

// public variable definitions
int logLevel = LOG_VERBOSE;
char logBuf[LOG_BUF_SIZE] = { 0 };


// public function definitions
void serialPrint(const int logType, const char* fmt, ...) {
    if (logType < logLevel) {
        return;
    }
    memset(logBuf, 0, LOG_BUF_SIZE);

    va_list ap;
    va_start(ap, fmt);
    vsnprintf(logBuf, LOG_BUF_SIZE, fmt, ap);
    va_end(ap);

    Serial.println(logBuf);
}

void setLogLevel(const int level) {
    logLevel = level;
}

int getLogLevel() {
    return logLevel;
}