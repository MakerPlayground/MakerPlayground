# Night Light V2

An automatic nightlight system with a push button to toggle between three modes of operation: off, auto (based on ambient light), and on.

## Implementations

1. [C++/Arduino](arduino/README.md)
2. [C++/Arduino + ALLI/O Device Lib](arduino_allio_device_lib/README.md)
3. [ALLI/O](allio/README.md)

## Evaluation Results

Arduino Nano/ATMega328P (Microchip AVR)

|           | C++/Arduino   | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------|---------------------------------|---------------|
| **RAM**   | 184 (9.0%)    | 199 (9.7%)                      | 209 (10.2%)   |
| **Flash** | 3,182 (10.4%) | 3,450 (11.2%)                   | 3,712 (12.1%) |

Arduino Zero/ATSAMD21 (ARM Cortex-M0+)

|           | C++/Arduino   | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------|---------------------------------|---------------|
| **RAM**   | 3,028 (9.2%)  | 3,068 (9.4%)                    | 3,084 (9.4%)  |
| **Flash** | 16,512 (6.3%) | 17,248 (6.6%)                   | 17,544 (6.7%) |

micro:bit v2/Nordic nRF52833 (ARM Cortex-M4)

|           | C++/Arduino   | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------|---------------------------------|---------------|
| **RAM**   | 280 (0.2%)    | 324 (0.2%)                      | 340 (0.3%)    |
| **Flash** | 5,936 (1.1%)  | 6,416 (1.2%)                    | 6,696 (1.3%)  |