#include "ir_decoder\include\ir_decode.h"

#include<iostream>
using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif

// global variable definition
long binary_length = 0;
UINT8 *binary_content = NULL;

t_remote_ac_status ac_status;
UINT16 user_data[USER_DATA_SIZE] = { 0 };

INT8 decode_as_ac(char* file_name)
{
	// keyboard input
	int in_char = 0;
	int count = 0;
	BOOL op_match = TRUE;
	UINT8 function_code = AC_FUNCTION_MAX;

	// get status
	UINT8 supported_mode = 0x00;
	INT8 min_temperature = 0;
	INT8 max_temperature = 0;
	UINT8 supported_speed = 0x00;
	UINT8 supported_swing = 0x00;

	BOOL need_control = TRUE;

	// init air conditioner status
	ac_status.ac_display = 0;
	ac_status.ac_sleep = 0;
	ac_status.ac_timer = 0;
	ac_status.ac_power = AC_POWER_OFF;
	ac_status.ac_mode = AC_MODE_COOL;
	ac_status.ac_temp = AC_TEMP_20;
	ac_status.ac_wind_dir = AC_SWING_ON;
	ac_status.ac_wind_speed = AC_WS_AUTO;

	if (IR_DECODE_FAILED == ir_file_open(0, 0, file_name))
	{
		ir_close();
		return IR_DECODE_FAILED;
	}

	do
	{
		in_char = getchar();
		op_match = TRUE;
		need_control = TRUE;
		switch (in_char)
		{
		case 'w':
		case 'W':
			// temperature plus
			ac_status.ac_temp = (t_ac_temperature)((ac_status.ac_temp == AC_TEMP_30) ? AC_TEMP_30 : (ac_status.ac_temp + 1));
			function_code = AC_FUNCTION_TEMPERATURE_UP;
			break;
		case 's':
		case 'S':
			ac_status.ac_temp = (t_ac_temperature)((ac_status.ac_temp == AC_TEMP_16) ? AC_TEMP_16 : (ac_status.ac_temp - 1));
			function_code = AC_FUNCTION_TEMPERATURE_DOWN;
			// temperature minus
			break;
		case 'a':
		case 'A':
			ac_status.ac_wind_speed = (t_ac_wind_speed) (ac_status.ac_wind_speed + 1);
			ac_status.ac_wind_speed = (t_ac_wind_speed) (ac_status.ac_wind_speed % AC_WS_MAX);
			function_code = AC_FUNCTION_WIND_SPEED;
			// wind speed loop
			break;
		case 'd':
		case 'D':
			ac_status.ac_wind_dir = (t_ac_swing)((ac_status.ac_wind_dir == 0) ? 1 : 0);
			function_code = AC_FUNCTION_WIND_SWING;
			// wind swing loop
			break;
		case 'q':
		case 'Q':
			ac_status.ac_mode = (t_ac_mode)(ac_status.ac_mode + 1);
			ac_status.ac_mode = (t_ac_mode) (ac_status.ac_mode % AC_MODE_MAX);
			function_code = AC_FUNCTION_MODE;
			break;
		case '1':
			// turn on
			ac_status.ac_power = AC_POWER_ON;
			function_code = AC_FUNCTION_POWER;
			break;
		case '2':
			// turn off
			ac_status.ac_power = AC_POWER_OFF;
			// FUNCTION MAX refers to power off
			// function_code = AC_FUNCTION_POWER;
			break;
		case '3':
			if (IR_DECODE_SUCCEEDED == get_supported_mode(&supported_mode))
			{
				ir_printf("supported mode = %02X\n", supported_mode);
			}
			need_control = FALSE;
			break;

		case '4':
			if (IR_DECODE_SUCCEEDED == get_supported_swing(ac_status.ac_mode, &supported_swing))
			{
				ir_printf("supported swing in %d = %02X\n", ac_status.ac_mode, supported_swing);
			}
			need_control = FALSE;
			break;
		case '5':
			if (IR_DECODE_SUCCEEDED == get_supported_wind_speed(ac_status.ac_mode, &supported_speed))
			{
				ir_printf("supported wind speed in %d = %02X\n", ac_status.ac_mode, supported_speed);
			}
			need_control = FALSE;
			break;

		case '6':
			if (IR_DECODE_SUCCEEDED == get_temperature_range(ac_status.ac_mode, &min_temperature, &max_temperature))
			{
				ir_printf("supported temperature range in mode %d = %d, %d\n",
					ac_status.ac_mode, min_temperature, max_temperature);
			}
			need_control = FALSE;
			break;

		default:
			op_match = FALSE;
			break;
		}

		if (TRUE == op_match && TRUE == need_control)
		{
			ir_printf("switch AC to power = %d, mode = %d, temp = %d, speed = %d, swing = %d\n",
				ac_status.ac_power,
				ac_status.ac_mode,
				ac_status.ac_temp,
				ac_status.ac_wind_speed,
				ac_status.ac_wind_dir
				);
			ir_decode(function_code, user_data, &ac_status, TRUE);
		}
	} while ('0' != in_char);

	ir_close();

	// free binary buffer
	ir_free(binary_content);
	binary_length = 0;

	return IR_DECODE_SUCCEEDED;
}

INT8 decode_as_tv(char *file_name, UINT8 irda_hex_encode)
{
	// keyboard input
	int in_char = 0;
	int key_code = -1;
	int count = 0;

	if (IR_DECODE_FAILED == ir_file_open(1, 1, file_name))
	{
		return IR_DECODE_FAILED;
	}

	do
	{
		in_char = getchar();
		if (in_char >= '0' && in_char <= '9')
		{
			key_code = in_char - '0';
			ir_decode(key_code, user_data, NULL, FALSE);
		}
		else if (in_char >= 'a' && in_char <= 'f')
		{
			key_code = 10 + (in_char - 'a');
			ir_decode(key_code, user_data, NULL, FALSE);
		}
		else if (in_char == 'q')
		{
			ir_close();
		}
		else
		{
			// do nothing
		}
	} while ('Q' != in_char);

	// free binary buffer
	ir_free(binary_content);
	binary_length = 0;

	return IR_DECODE_SUCCEEDED;
}

int main(int argc, char *argv[])
{
	int nRetCode = 0;

	char function = '0';
	UINT8 irda_hex_encode = 0;

	if (4 != argc)
	{
		ir_printf("number of args error !\n");
		return -1;
	}

	function = argv[1][0];
	irda_hex_encode = (UINT8)(argv[3][0] - '0');
	ir_printf("decode functionality = %c\n", function);

	switch (function)
	{
	case '0':
		ir_printf("decode binary file as AC\n");
		decode_as_ac(argv[2]);
		break;

	case '1':
		ir_printf("decode binary file as TV : %d\n", irda_hex_encode);
		decode_as_tv(argv[2], irda_hex_encode);
		break;

	default:
		ir_printf("decode functionality error !\n");
		break;
	}

	system("pause");

	return nRetCode;
}