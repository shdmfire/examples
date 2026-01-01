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

#include <string.h>


// public function definitions
int splitString(const char *str, char *parts[],
                const int parts_max, const char *delimiter) {
    char *pch = nullptr;
    char *copy = nullptr;
    char *tmp = nullptr;
    int i = 0;

    copy = strdup(str);
    if (nullptr == copy) {
        goto _exit;
    }

    pch = strtok(copy, delimiter);

    tmp = strdup(pch);
    if (nullptr == tmp) {
        goto _exit;
    }

    if (i >= parts_max) {
        goto _exit;
    }
    parts[i++] = tmp;

    while (pch) {
        pch = strtok(nullptr, delimiter);
        if (nullptr == pch)
            break;

        tmp = strdup(pch);
        if (nullptr == tmp) {
            goto _exit;
        }

        if (i >= parts_max) {
            goto _exit;
        }
        parts[i++] = tmp;
    }

    free(copy);
    return i;

    _exit:
        free(copy);
    for (int j = 0; j < i; j++) {
        free(parts[j]);
    }
    return -1;
}