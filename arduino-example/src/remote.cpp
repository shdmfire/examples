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
#include <Base64.h>

#include "utils.h"
#include "ir_decode.h"


#define REMOTE_BIN_MAX (1024)

#define ABIN_COMMAND_SEG (5)
#define SEG_ABIN_HEADER  (0)
#define SEG_ABIN_CATE    (1)
#define SEG_ABIN_SUBCATE (2)
#define SEG_ABIN_LENGTH  (3)
#define SEG_ABIN_BIN     (4)

// global variable definitions
unsigned char *remoteBin = nullptr;
int remoteBinLen = 0;


// public function definitions
int onRemoteBin(const char *binStr) {
    char *aBinCommand[ABIN_COMMAND_SEG];
    char *remoteBinStr = nullptr;
    int categoryId = 0;
    int subCateId = 0;
    int aBinCommandSeg = 0;
    int remoteBinBase64Len = 0;

    aBinCommandSeg = splitString(binStr, aBinCommand, ABIN_COMMAND_SEG, ",");
    if (ABIN_COMMAND_SEG != aBinCommandSeg) {
        Serial.println("Invalid aBinCommand");
        return -1;
    }
    categoryId = strtol(aBinCommand[SEG_ABIN_CATE], nullptr, 10);
    subCateId = strtol(aBinCommand[SEG_ABIN_SUBCATE], nullptr, 10);
    remoteBinBase64Len = strtol(aBinCommand[SEG_ABIN_LENGTH], nullptr, 10);
    remoteBinStr = aBinCommand[SEG_ABIN_BIN];
    if (remoteBinBase64Len != strlen(remoteBinStr)) {
        Serial.println("remoteBin length not correct");
        return -1;
    }

    remoteBinLen = base64_dec_len(remoteBinStr, remoteBinBase64Len);
    remoteBin = static_cast<unsigned char*>(malloc(remoteBinLen));
    char debugStr[129];
    if (nullptr == remoteBin) {
        Serial.println("Not enough memory for remoteBin");
        return -1;
    }
    Serial.print("Remote bin length = ");
    Serial.println(remoteBinLen);
    memset(remoteBin, 0, remoteBinLen);

    if (remoteBinLen != base64_decode(reinterpret_cast<char*>(remoteBin), remoteBinStr, remoteBinBase64Len)) {
        Serial.println("Base64 decode failed");
        return -1;
    }

#if defined REMOTE_BIN_DEBUG
    snprintf(debugStr, 128, "%02x %02x %02x %02x %02x %02x %02x %02x",
        remoteBin[0], remoteBin[1], remoteBin[2], remoteBin[3],
        remoteBin[4], remoteBin[5], remoteBin[6], remoteBin[7]);
    Serial.println(debugStr);
    snprintf(debugStr, 128, "%02x %02x %02x %02x %02x %02x %02x %02x",
        remoteBin[remoteBinLen - 8], remoteBin[remoteBinLen - 7], remoteBin[remoteBinLen - 6], remoteBin[remoteBinLen - 5],
        remoteBin[remoteBinLen - 4], remoteBin[remoteBinLen - 3], remoteBin[remoteBinLen - 2], remoteBin[remoteBinLen - 1]);
    Serial.println(debugStr);
#endif

    if (IR_DECODE_FAILED == ir_binary_open(categoryId, subCateId, remoteBin, remoteBinLen)) {
        Serial.println("ir_binary_open failed");
        return -1;
    }
    Serial.println("ir_binary_open success");
    ir_close();

    return remoteBinLen;
}