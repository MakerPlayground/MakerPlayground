#  Windshield Wipers

A car windshield wiper simulation with independent front and rear wiper controls. Each windshield has three modes selected by a button: slow wipe, fast wipe, and spray-and-wipe. In slow/fast wipe mode, the motor drives the wiper from its start position to its end position (detected by limit switches), then reverses back, pausing at the rest position before the next sweep — slow mode pauses 1 second between sweeps while fast mode pauses 2 seconds. In spray-and-wipe mode, the washer pump runs for 2 seconds first, then the wiper performs a single sweep. Both front and rear wipers operate as independently and can run simultaneously.

> **Note:** The Visuino and XOD implementations only include the fast wipe mode, as implementing all three modes is too complex on these platforms. Additionally, only the front wiper diagram is provided, but it can be easily duplicated for the rear wiper due to the parallel execution nature of these platforms.

> **Note:** The SinelaboreRT and Ardublockly implementations only include the fast wipe mode for both front and rear wipers in order to keep the diagram size manageable.

## Implementations

1. [C++/Arduino](arduino/README.md)
2. [ALLI/O](allio/README.md)
3. [Ardublockly](ardublockly/README.md)
4. [MakeCode](makecode/README.md)
5. [SinelaboreRT](sinelaborert/README.md)
6. [Visuino](visuino/README.md)
7. [XOD](xod/README.md)

## Evaluation Results

Arduino Nano/ATMega328P (Microchip AVR)

|           | C++/Arduino  |    ALLI/O     |
|-----------|--------------|---------------|
| **RAM**   | 196 (9.6%)   | 292 (14.3%)   |
| **Flash** | 2,798 (9.1%) | 4,302 (14.0%) |

Arduino Zero/ATSAMD21 (ARM Cortex-M0+)

|           | C++/Arduino  |    ALLI/O     |
|-----------|--------------|---------------|
| **RAM**   | 3,044 (9.3%) | 3,216 (9.8%)  |
| **Flash** | 12,824 (4.9%)| 14,380 (5.5%) |

micro:bit v2/Nordic nRF52833 (ARM Cortex-M4)

|           | C++/Arduino  |    ALLI/O     |
|-----------|--------------|---------------|
| **RAM**   | 284 (0.2%)   | 460 (0.4%)    |
| **Flash** | 3,796 (0.7%) | 5,384 (1.0%)  |