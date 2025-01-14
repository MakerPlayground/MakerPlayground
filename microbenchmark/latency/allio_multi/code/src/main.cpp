// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_GENERIC_DIGITAL_IN.h"
#include "MP_GENERIC_DIGITAL_OUT.h"
#include "MP_DEVICE.h"

unsigned long Begin1_recentBlockFinishTime_1 = 0;
void (*Begin1_currentFn)(void);
void (*Begin2_currentFn)(void);
void (*Begin3_currentFn)(void);
void (*Begin4_currentFn)(void);
void (*Begin5_currentFn)(void);
void (*Begin6_currentFn)(void);
void (*Begin7_currentFn)(void);
void (*Begin8_currentFn)(void);

MP_GENERIC_DIGITAL_OUT _output(12);
MP_GENERIC_DIGITAL_IN _in1(2);
MP_GENERIC_DIGITAL_IN _in2(3);
MP_GENERIC_DIGITAL_IN _in3(4);
MP_GENERIC_DIGITAL_IN _in4(5);
MP_GENERIC_DIGITAL_IN _in5(6);
MP_GENERIC_DIGITAL_IN _in6(7);
MP_GENERIC_DIGITAL_IN _in7(8);
MP_GENERIC_DIGITAL_IN _in8(9);

void Begin1_command_options();
void Begin2_command_options();
void Begin3_command_options();
void Begin4_command_options();
void Begin5_command_options();
void Begin6_command_options();
void Begin7_command_options();
void Begin8_command_options();

void Begin1_command();
void Begin2_command();
void Begin3_command();
void Begin4_command();
void Begin5_command();
void Begin6_command();
void Begin7_command();
void Begin8_command();

void setup() {
    MPSerial.begin(115200);

    _output.init();
    _in1.init();
    _in2.init();
    _in3.init();
    _in4.init();
    _in5.init();
    _in6.init();
    _in7.init();
    _in8.init();

    Begin1_currentFn = Begin1_command_options;
    Begin2_currentFn = Begin2_command_options;
    Begin3_currentFn = Begin3_command_options;
    Begin4_currentFn = Begin4_command_options;
    Begin5_currentFn = Begin5_command_options;
    Begin6_currentFn = Begin6_command_options;
    Begin7_currentFn = Begin7_command_options;
    Begin8_currentFn = Begin8_command_options;
}

void loop() {
    update();
    Begin1_currentFn();
    Begin2_currentFn();
    Begin3_currentFn();
    Begin4_currentFn();
    Begin5_currentFn();
    Begin6_currentFn();
    Begin7_currentFn();
    Begin8_currentFn();
}

void update() {
    currentTime = millis();
    _output.update(currentTime);
    _in1.update(currentTime);
    _in2.update(currentTime);
    _in3.update(currentTime);
    _in4.update(currentTime);
    _in5.update(currentTime);
    _in6.update(currentTime);
    _in7.update(currentTime);
    _in8.update(currentTime);
}

void Begin1_command_options() {
    if (!_in1.state()) {
        Begin1_currentFn = Begin1_command;
    }
}

void Begin1_command() {
    _output.on();
    delay(10);
    _output.off();
    Begin1_currentFn = Begin1_command_options;
}

void Begin2_command_options() {
    if (!_in2.state()) {
        Begin2_currentFn = Begin2_command;
    }
}

void Begin2_command() {
    MPSerial.println('2');
    Begin2_currentFn = Begin2_command_options;
}

void Begin3_command_options() {
    if (!_in3.state()) {
        Begin3_currentFn = Begin3_command;
    }
}

void Begin3_command() {
    MPSerial.println('3');
    Begin3_currentFn = Begin3_command_options;
}

void Begin4_command_options() {
    if (!_in4.state()) {
        Begin4_currentFn = Begin4_command;
    }
}

void Begin4_command() {
    MPSerial.println('4');
    Begin4_currentFn = Begin4_command_options;
}

void Begin5_command_options() {
    if (!_in5.state()) {
        Begin5_currentFn = Begin5_command;
    }
}

void Begin5_command() {
    MPSerial.println('5');
    Begin5_currentFn = Begin5_command_options;
}

void Begin6_command_options() {
    if (!_in6.state()) {
        Begin6_currentFn = Begin6_command;
    }
}

void Begin6_command() {
    MPSerial.println('6');
    Begin6_currentFn = Begin6_command_options;
}

void Begin7_command_options() {
    if (!_in7.state()) {
        Begin7_currentFn = Begin7_command;
    }
}

void Begin7_command() {
    MPSerial.println('7');
    Begin7_currentFn = Begin7_command_options;
}

void Begin8_command_options() {
    if (!_in8.state()) {
        Begin8_currentFn = Begin8_command;
    }
}

void Begin8_command() {
    MPSerial.println('8');
    Begin8_currentFn = Begin8_command_options;
}