# Night Light V1

An automatic nightlight system that automatically turns the light on or off based on ambient light.

## Implementations

1. [C++/Arduino](arduino/README.md)
2. [C++/Arduino + ALLI/O Device Lib](arduino_allio_device_lib/README.md)
3. [ALLI/O](allio/README.md)

## Evaluation Results

Arduino Nano/ATMega328P (Microchip AVR)

|           | C++/Arduino  | C++/Arduino + ALLI/O Device Lib | ALLI/O       |
|-----------|--------------|---------------------------------|--------------|
| **RAM**   | 184 (9.0%)   | 192 (9.4%)                      | 198 (9.7%)   |
| **Flash** | 2,750 (9.0%) | 2,908 (9.5%)                    | 3,068 (10.0%)|

Arduino Zero/ATSAMD21 (ARM Cortex-M0+)

|           | C++/Arduino  | C++/Arduino + ALLI/O Device Lib | ALLI/O       |
|-----------|--------------|---------------------------------|--------------|
| **RAM**   | 3,028 (9.2%) | 3,060 (9.3%)                    | 3,068 (9.4%) |
| **Flash** | 16,364 (6.2%)| 16,924 (6.5%)                   | 17,080 (6.5%)|