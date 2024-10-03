# Whack-A-Mole

## Implementations

1. [C++/Arduino](arduino/README.md)
2. [C++/FreeRTOS](freertos/README.md)
3. [ALLI/O](allio/README.md)

## Evaluation Results

Arduino Nano/ATMega328P (Microchip AVR)

|           | C++/Arduino + ALLI/O Device Lib | C++/FreeRTOS   | ALLI/O        |
|-----------|---------------------------------|----------------|---------------|
| **RAM**   | 582 (28.4%)                     | 765 (37.4%)    | 588 (28.7%)   |
| **Flash** | 8,846 (28.8%)                   | 15,120 (49.2%) | 9,858 (32.1%) |

Arduino Zero/ATSAMD21 (ARM Cortex-M0+)

|           | C++/Arduino + ALLI/O Device Lib | C++/FreeRTOS   | ALLI/O        |
|-----------|---------------------------------|----------------|---------------|
| **RAM**   | 3,780 (11.5%)                   | 18,636 (56.9%) | 3,788 (11.6%)   |
| **Flash** | 25,428 (9.7%)                   | 32,328 (12.3%) | 26,584 (10.1%) |