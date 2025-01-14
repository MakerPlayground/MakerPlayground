// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_LED_AH.h"
#include "MP_DEVICE.h"

unsigned long Begin1_recentBlockFinishTime_1 = 0;
void (*Begin1_currentFn)(void);
int count = 0;

// UNO 2
// Microbit 0
MP_LED_AH _LED1(0);

void Begin1_command_reset();
void Begin1_command_increment();
void Begin1_command_increment_options();
void Begin1_command_led_on();
void Begin1_command_led_on_options();

void setup() {
    MPSerial.begin(115200);

    _LED1.init();

    Begin1_currentFn = Begin1_command_reset;
}

void loop() {
    update();
    Begin1_currentFn();
}

void update() {
    currentTime = millis();
    _LED1.update(currentTime);
}

void Begin1_command_reset() {
    update();
    count = 0;
    _LED1.off();
    Begin1_currentFn = Begin1_command_increment;
}

void Begin1_command_increment() {
    update();
    count = count + 1;
    Begin1_currentFn = Begin1_command_increment_options;
}

void Begin1_command_increment_options() {
    if (count < 25000) {
        Begin1_currentFn = Begin1_command_increment;
    } else {
        Begin1_currentFn = Begin1_command_led_on;
    }
}

void Begin1_command_led_on() {
    update();
    _LED1.on(100);
    Begin1_recentBlockFinishTime_1 = millis();
    Begin1_currentFn = Begin1_command_led_on_options;
}

void Begin1_command_led_on_options() {
    update();
    if (millis() - Begin1_recentBlockFinishTime_1 > 10) {
        Begin1_currentFn = Begin1_command_reset;
    }
}