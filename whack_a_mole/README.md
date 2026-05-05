# Whack-A-Mole

A two-player competitive Whack-A-Mole game. Each player has 5 holes with servo-driven moles and 5 corresponding buttons. Press the Start button to begin a 60-second round: a mole randomly pops up in one of the 5 holes and the player must press the matching button within 1 second to score a point, after which a new mole immediately pops up. If the player misses, the mole retreats and a new one pops up anyway. Both players play simultaneously on their own set of holes. At the end of the round, all moles hide and the winner's display shows "WIN"; if both players have the same score, both displays show "DRAW".

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
| **RAM**   | 3,780 (11.5%)                   | 18,636 (56.9%) | 3,788 (11.6%) |
| **Flash** | 25,428 (9.7%)                   | 32,328 (12.3%) | 26,584 (10.1%)|

micro:bit v2/Nordic nRF52833 (ARM Cortex-M4)

|           | C++/Arduino + ALLI/O Device Lib | C++/FreeRTOS   | ALLI/O        |
|-----------|---------------------------------|----------------|---------------|
| **RAM**   | 868 (0.7%)                      | TBD | 876 (0.7%)   |
| **Flash** | 9,712 (1.9%)                    | TBD | 10,884 (2.1%) |