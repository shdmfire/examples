# IRext Java Example

## Overview

This repository contains a Java wrapper SDK for infrared (IR) signal decoding. The SDK provides a convenient way to decode infrared signals for various devices including air conditioners, TVs, set-top boxes, and other consumer electronics. It acts as a bridge between Java applications and native IR decoding libraries.

## IR Decode

The core component of this SDK is the IRDecode class, which provides the primary interface for interacting with native IR decoding functionality.

### Features

- **Multi-device Support**: Supports various device categories including Air Conditioners, TVs, Set-top Boxes, Network Boxes, DVD players, Fans, Projectors, Stereos, Lights, Cleaning Robots, Air Cleaners, and more.
- **Air Conditioner Control**: Comprehensive control over AC parameters including power, temperature, mode, wind speed, and swing settings.
- **Native Integration**: Uses JNI (Java Native Interface) to connect with native decoding libraries for optimal performance.
- **Parameter Validation**: Built-in validation for AC status parameters to ensure valid operations.
- **Temperature Range Detection**: Ability to query supported temperature ranges for different AC modes.

### Key Classes

#### IRDecode
The main entry point for the SDK. Provides methods for:
- Opening binary files or raw binary data
- Decoding IR signals with given key codes and AC status
- Getting temperature ranges for AC modes
- Querying supported features for different AC modes
- Managing native library lifecycle

#### ACStatus
Represents the status of an air conditioner with parameters such as:
- Power state (acPower)
- Temperature (acTemp)
- Operating mode (acMode)
- Wind direction (acWindDir)
- Wind speed (acWindSpeed)
- Display, sleep, timer, and wind direction change settings

#### TemperatureRange
Defines the minimum and maximum temperature range for air conditioners based on the operating mode.

### Usage Example

The SDK includes a main method in IRDecode class demonstrating basic usage:

```java
IRDecode irDecoder = IRDecode.getInstance();
if (Constants.ERROR_CODE_SUCCESS == irDecoder.openFile(Constants.CategoryID.TV.getValue(), 1, "/path/to/file.bin")) {
    int[] decoded = irDecoder.decodeBinary(1, null);
    for (int i = 0; i < decoded.length; i++) {
        System.out.print(decoded[i]);
        if (i != decoded.length - 1) {
            System.out.print(", ");
        }
    }
    System.out.println();
    irDecoder.closeBinary();
}
```

### Library Loading

The SDK loads the native library from `/data/irext/libirdecode_jni.so`. Ensure this path is accessible and contains the appropriate native library for your platform.

### Error Handling

The SDK defines various error codes in the Constants class:
- ERROR_CODE_SUCCESS (0): Operation successful
- ERROR_CODE_NETWORK_ERROR (-1): Network error
- ERROR_CODE_AUTH_FAILURE (1): Authentication failure
- ERROR_CODE_INVALID_CATEGORY (2): Invalid category
- And several other specific error codes

### Building and Running

To use this SDK:
1. Ensure the native library `libirdecode_jni.so` is available at `/data/irext/`
2. Compile the Java classes
3. Run with appropriate permissions to load the native library

## Related Links

- [IRext Official Site](https://site.irext.net)
- [IRext Documentation](https://site.irext.net/doc)
- [IRext Open Source Project](https://opensource.irext.net)

## License

Please refer to the [IRext repository](https://opensource.irext.net) for license information.