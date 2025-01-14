# Scoreboard

## Implementations

1. [C++/Arduino](arduino/README.md)
2. [ALLI/O](allio/README.md)

## Evaluation Results

Arduino Nano/ATMega328P (Microchip AVR)

|           | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------------------------|---------------|
| **RAM**   | 249 (12.2%)                     | 255 (12.5%)   |
| **Flash** | 3,580 (11.7%)                   | 3,724 (12.1%) |

Arduino Zero/ATSAMD21 (ARM Cortex-M0+)

|           | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------------------------|---------------|
| **RAM**   | 3,084 (9.4%)                    | 3,088 (9.4%)  |
| **Flash** | 13,524 (5.2%)                   | 13,840 (5.3%) |

micro:bit v2/Nordic nRF52833 (ARM Cortex-M4)

|           | C++/Arduino + ALLI/O Device Lib | ALLI/O        |
|-----------|---------------------------------|---------------|
| **RAM**   | 344 (0.3%)                      | 348 (0.3%)    |
| **Flash** | 5,272 (1.0%)                    | 5,600 (1.1%)  |