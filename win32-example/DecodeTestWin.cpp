/**************************************************************************************
Filename:       DecodeTestWin.cpp
Revised:        Date: 2016-11-05
Revision:       Revision: 1.0

Description:    This file provides main entry for irda decoder

Revision log:
* 2016-11-05: created by strawmanbobi
**************************************************************************************/


#include <ctype.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "ir_decoder\src\include\ir_defs.h"
#include "ir_decoder\src\include\ir_decode.h"

#define INPUT_MAX 3

// global variable definition
t_remote_ac_status ac_status;
UINT16 user_data[USER_DATA_SIZE];

void input_number(int *val)
{
    char n[50]={0};
    int i = 0;
    *val = 0;
    scanf_s("%3s", n, (unsigned)_countof(n));
    getchar();
    while(1)
    {
        if(n[i] < '0'||n[i] > '9')
        {
            printf("\nInvalid number format, please re-input : ");
            scanf_s("%3s", n, (unsigned)_countof(n));
            i=0;
        }
        else
        {
            i++;
        }
        if(n[i] == '\0')
            break;
    }
    i = 0;
    while (n[i] != '\0')
    {
        *val = (*val * 10 + (int)n[i] - 48);
        i++;
    }
}

static INT8 decode_as_ac(char *file_name, int sub_cate)
{
    BOOL op_match = TRUE;
    UINT8 function_code = AC_FUNCTION_MAX;
    int key_code = 0;
    int first_time = 1;
    int length = 0;
    int index = 0;

    // get status
    UINT8 supported_mode = 0x00;
    INT8 min_temperature = 0;
    INT8 max_temperature = 0;
    UINT8 supported_speed = 0x00;
    UINT8 supported_swing = 0x00;
    UINT8 supported_wind_direction = 0x00;

    BOOL need_control = TRUE;

    // init air conditioner status
    ac_status.ac_display = 0;
    ac_status.ac_sleep = 0;
    ac_status.ac_timer = 0;
    ac_status.ac_power = AC_POWER_ON;
    ac_status.ac_mode = AC_MODE_COOL;
    ac_status.ac_temp = AC_TEMP_20;
    ac_status.ac_wind_dir = AC_SWING_ON;
    ac_status.ac_wind_speed = AC_WS_AUTO;
    ac_status.change_wind_direction = FALSE;

    if (IR_DECODE_FAILED == ir_file_open(REMOTE_CATEGORY_AC, sub_cate, file_name))
    {
        printf("Failed to open file: %s\n", file_name);
        ir_close();
        return IR_DECODE_FAILED;
    }

    do
    {
        if (1 == first_time)
        {
            printf("Please input valid key code "
                   "(Key code could be referenced from https://site.irext.net/doc#keymap) : \n");
            first_time = 0;
        }
        else
        {
            printf("Please input valid key code : \n");
        }
        input_number(&key_code);

        op_match = TRUE;
        need_control = FALSE;

        printf("input key code = %d\n", key_code);

        if (99 == key_code)
        {
            break;
        }

        if (14 == key_code)
        {
            if (IR_DECODE_SUCCEEDED == get_supported_mode(&supported_mode))
            {
                printf("supported mode = %02X\n", supported_mode);
            }
            else
            {
                printf("get supported mode failed\n");
            }
        }
        else if (15 == key_code)
        {
            if (IR_DECODE_SUCCEEDED == get_supported_wind_speed(ac_status.ac_mode, &supported_speed))
            {
                printf("supported wind speed in %d = %02X\n", ac_status.ac_mode, supported_speed);
            }
            else
            {
                printf("get supported wind speed failed\n");
            }
        }
        else if (16 == key_code)
        {
            if (IR_DECODE_SUCCEEDED == get_temperature_range(ac_status.ac_mode, &min_temperature, &max_temperature))
            {
                printf("supported temperature range in mode %d = %d, %d\n",
                          ac_status.ac_mode, min_temperature, max_temperature);
            }
            else
            {
                printf("get supported temperature range failed\n");
            }
        }
        else if (17 == key_code)
        {
            if (IR_DECODE_SUCCEEDED == get_supported_wind_direction(&supported_wind_direction))
            {
                printf("supported swing type = %02X\n", supported_wind_direction);
            }
            else
            {
                printf("get swing type failed\n");
            }
        }
        else
        {
            int temp_mode = 0;
            int temp_wind_speed = 0;

            switch (key_code)
            {
                case 0:
                    ac_status.ac_power = ((ac_status.ac_power == AC_POWER_ON) ? AC_POWER_OFF : AC_POWER_ON);
                    need_control = TRUE;
                    break;

                case 1:
                    temp_mode = (int) ac_status.ac_mode;
                    temp_mode++;
                    ac_status.ac_mode = (t_ac_mode) (temp_mode % AC_MODE_MAX);
                    need_control = TRUE;
                    break;

                case 2:
                case 7:
                    ac_status.ac_temp = (t_ac_temperature) ((ac_status.ac_temp == AC_TEMP_30) ? AC_TEMP_30 : (ac_status.ac_temp + 1));
                    need_control = TRUE;
                    break;

                case 3:
                case 8:
                    ac_status.ac_temp = (t_ac_temperature) ((ac_status.ac_temp == AC_TEMP_16) ? AC_TEMP_16 : (ac_status.ac_temp - 1));
                    need_control = TRUE;
                    break;

                case 9:
                    temp_wind_speed = (int)ac_status.ac_wind_speed;
                    temp_wind_speed++;
                    ac_status.ac_wind_speed = (t_ac_wind_speed) (temp_wind_speed % AC_WS_MAX);
                    need_control = TRUE;
                    break;

                case 10:
                    ac_status.ac_wind_dir = ((ac_status.ac_wind_dir == AC_SWING_ON) ? AC_SWING_OFF : AC_SWING_ON);
                    need_control = TRUE;
                    break;

                case 11:
                    if (ac_status.ac_wind_dir == AC_SWING_OFF) {
                        ac_status.change_wind_direction = TRUE;
                    }
                    need_control = TRUE;
                    break;

                default:
                    op_match = FALSE;
                    break;
            }

            if (TRUE == op_match && TRUE == need_control)
            {
                printf("switch AC to power = %d, mode = %d, temp = %d, speed = %d, swing = %d with function code = %d\n",
                       ac_status.ac_power,
                       ac_status.ac_mode,
                       ac_status.ac_temp,
                       ac_status.ac_wind_speed,
                       ac_status.ac_wind_dir,
                       function_code);

                length = ir_decode(function_code, user_data, &ac_status);
                printf("\n === Binary decoded : %d\n", length);
                for (index = 0; index < length; index++)
                {
                    printf("%d, ", user_data[index]);
                }
                printf("===\n");
            }
        }
    } while (TRUE);

    ir_close();

    return IR_DECODE_SUCCEEDED;
}

static INT8 decode_as_tv(char *file_name, UINT8 ir_hex_encode)
{
    // keyboard input
    int key_code = 0;
    int first_time = 1;
    int length = 0;
    int index = 0;

    // here remote category TV represents for command typed IR code
    if (IR_DECODE_FAILED == ir_file_open(REMOTE_CATEGORY_TV, ir_hex_encode, file_name))
    {
        ir_close();
        return IR_DECODE_FAILED;
    }

    do
    {
        if (1 == first_time)
        {
            printf("Please input valid key code "
                   "(Key code could be referenced from https://irext.net/doc#keymap) : \n");
            first_time = 0;
        }
        else
        {
            printf("Please input valid key code : \n");
        }
        input_number(&key_code);
        if (99 == key_code)
        {
            break;
        }
        length = ir_decode(key_code, user_data, NULL);
        printf("\n === Binary decoded : %d\n", length);
        for (index = 0; index < length; index++)
        {
            printf("%d, ", user_data[index]);
        }
        printf("===\n");

    } while (TRUE);

    ir_close();
    return IR_DECODE_SUCCEEDED;
}

static void print_usage(const char *progn) {
    printf("Usage: %s [function] [file] [subcate]\n"
              "[function] : 0 - decode for AC; 1 - decode for TV\n"
              "[file]     : the remote control binary file\n"
              "[subcate]  : the sub_cate value from remote_index", progn);
}

int main(int argc, char *argv[])
{
    char function = '0';
    UINT8 ir_hex_encode = 0;

    if (4 != argc)
    {
        print_usage(argv[0]);
        return -1;
    }

    function = argv[1][0];
    ir_hex_encode = (UINT8) (argv[3][0] - '0');

    switch (function)
    {
        case '0':
            printf("Decode %s as status-typed binary\n", argv[2]);
            decode_as_ac(argv[2], ir_hex_encode);
            break;

        case '1':
            printf("Decode %s as command-typed binary in sub_cate %d\n", argv[2], ir_hex_encode);
            decode_as_tv(argv[2], ir_hex_encode);
            break;

        default:
            printf("Decode functionality not supported : %c\n", function);
            break;
    }


}