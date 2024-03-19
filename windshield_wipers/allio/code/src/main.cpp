// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_BUTTON_AL.h"
#include "MP_DC_MOTOR.h"
#include "MP_GENERIC_PWM.h"

unsigned long Begin1_recentBlockFinishTime = 0;
unsigned long Begin2_recentBlockFinishTime = 0;
void (*Begin1_currentFn)(void);
void (*Begin1_returnFn)(void);
void (*Begin2_currentFn)(void);
void (*Begin2_returnFn)(void);
MP_DC_MOTOR* Begin1_param1;
MP_BUTTON_AL* Begin1_param2;
MP_BUTTON_AL* Begin1_param3;
MP_GENERIC_PWM* Begin1_param4;
MP_DC_MOTOR* Begin2_param1;
MP_BUTTON_AL* Begin2_param2;
MP_BUTTON_AL* Begin2_param3;
MP_GENERIC_PWM* Begin2_param4;

MP_BUTTON_AL _Btn1(2);
MP_BUTTON_AL _Btn2(4);
MP_BUTTON_AL _Btn3(7);
MP_BUTTON_AL _Btn4(A0);
MP_BUTTON_AL _Btn5(A1);
MP_BUTTON_AL _Btn6(A2);
MP_BUTTON_AL _SwA1(8);
MP_BUTTON_AL _SwA2(12);
MP_BUTTON_AL _SwB1(A3);
MP_BUTTON_AL _SwB2(A4);
MP_DC_MOTOR _MotorA(3, 5);
MP_DC_MOTOR _MotorB(9, 10);
MP_GENERIC_PWM _PumpA(6);
MP_GENERIC_PWM _PumpB(11);

void Begin1_begin_options();
void Begin1_command_wiper_on_1_options();
void Begin1_command_wiper_on_2_options();
void Begin1_command_spray_on_1_options();
void Begin1_command_wiper_on_3_options();
void Begin1_subdiagram_wiper_on_motor_cw();
void Begin1_subdiagram_wiper_on_motor_cw_options();
void Begin1_subdiagram_wiper_on_motor_ccw();
void Begin1_subdiagram_wiper_on_motor_ccw_options();
void Begin1_subdiagram_wiper_on_motor_stop();
void Begin1_subdiagram_spray_on_pump_on();
void Begin1_subdiagram_spray_on_pump_on_options();
void Begin1_subdiagram_spray_on_pump_off();
void Begin2_begin_options();
void Begin2_command_wiper_on_1_options();
void Begin2_command_wiper_on_2_options();
void Begin2_command_spray_on_1_options();
void Begin2_command_wiper_on_3_options();
void Begin2_subdiagram_wiper_on_motor_cw();
void Begin2_subdiagram_wiper_on_motor_cw_options();
void Begin2_subdiagram_wiper_on_motor_ccw();
void Begin2_subdiagram_wiper_on_motor_ccw_options();
void Begin2_subdiagram_wiper_on_motor_stop();
void Begin2_subdiagram_spray_on_pump_on();
void Begin2_subdiagram_spray_on_pump_on_options();
void Begin2_subdiagram_spray_on_pump_off();

void setup() {
    MPSerial.begin(115200);

    _Btn1.init();
    _Btn2.init();
    _Btn3.init();
    _Btn4.init();
    _Btn5.init();
    _Btn6.init();
    _SwA1.init();
    _SwA2.init();
    _SwB1.init();
    _SwB2.init();
    _MotorA.init();
    _MotorB.init();
    _PumpA.init();
    _PumpB.init();

    Begin1_currentFn = Begin1_begin_options;
    Begin2_currentFn = Begin2_begin_options;
}

void loop() {
    update();
    Begin1_currentFn();
    Begin2_currentFn();
}

void update() {
    currentTime = millis();

    _Btn1.update(currentTime);
    _Btn2.update(currentTime);
    _Btn3.update(currentTime);
    _Btn4.update(currentTime);
    _Btn5.update(currentTime);
    _Btn6.update(currentTime);
    _SwA1.update(currentTime);
    _SwA2.update(currentTime);
    _SwB1.update(currentTime);
    _SwB2.update(currentTime);
    _MotorA.update(currentTime);
    _MotorB.update(currentTime);
    _PumpA.update(currentTime);
    _PumpB.update(currentTime);
}

void Begin1_begin_options() {
    if (_Btn1.isPress()) {
        Begin1_param1 = &_MotorA;
        Begin1_param2 = &_SwA1;
        Begin1_param3 = &_SwA2;
        Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_cw;
        Begin1_returnFn = Begin1_command_wiper_on_1_options;
    } else if (_Btn2.isPress()) {
        Begin1_param1 = &_MotorA;
        Begin1_param2 = &_SwA1;
        Begin1_param3 = &_SwA2;
        Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_cw;
        Begin1_returnFn = Begin1_command_wiper_on_2_options;
    } else if (_Btn3.isPress()) {
        Begin1_param4 = &_PumpA;
        Begin1_currentFn = Begin1_subdiagram_spray_on_pump_on;
        Begin1_returnFn = Begin1_command_spray_on_1_options;
    }
}

void Begin1_command_wiper_on_1_options() {
    if (millis() - Begin1_recentBlockFinishTime > 1000) {
        Begin1_currentFn = Begin1_begin_options;
    }
}

void Begin1_command_wiper_on_2_options() {
    if (millis() - Begin1_recentBlockFinishTime > 2000) {
        Begin1_currentFn = Begin1_begin_options;
    }
}

void Begin1_command_spray_on_1_options() {
    Begin1_param1 = &_MotorA;
    Begin1_param2 = &_SwA1;
    Begin1_param3 = &_SwA2;
    Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_cw;
    Begin1_returnFn = Begin1_command_wiper_on_3_options;
}

void Begin1_command_wiper_on_3_options() {
    Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_cw;
    Begin1_returnFn = Begin1_begin_options;
}

void Begin1_subdiagram_wiper_on_motor_cw() {
    Begin1_param1->on(0, 100);
    Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_cw_options;
}

void Begin1_subdiagram_wiper_on_motor_cw_options() {
    if (Begin1_param3->isPress()) {
        Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_ccw;
    }
}

void Begin1_subdiagram_wiper_on_motor_ccw() {
    Begin1_param1->on(1, 100);
    Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_ccw_options;
}

void Begin1_subdiagram_wiper_on_motor_ccw_options() {
    if (Begin1_param2->isPress()) {
        Begin1_currentFn = Begin1_subdiagram_wiper_on_motor_stop;
    }
}

void Begin1_subdiagram_wiper_on_motor_stop() {
    Begin1_param1->off();
    Begin1_currentFn = Begin1_returnFn;
}

void Begin1_subdiagram_spray_on_pump_on() {
    _PumpA.on(100);
    Begin1_recentBlockFinishTime = millis();
    Begin1_currentFn = Begin1_subdiagram_spray_on_pump_on_options;
}

void Begin1_subdiagram_spray_on_pump_on_options() {
    if (millis() - Begin1_recentBlockFinishTime > 2000) {
        Begin1_currentFn = Begin1_subdiagram_spray_on_pump_off;
    }
}

void Begin1_subdiagram_spray_on_pump_off() {
    _PumpA.off();
    Begin1_currentFn = Begin1_returnFn;
}

void Begin2_begin_options() {
    if (_Btn1.isPress()) {
        Begin2_param1 = &_MotorB;
        Begin2_param2 = &_SwB1;
        Begin2_param3 = &_SwB2;
        Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_cw;
        Begin2_returnFn = Begin2_command_wiper_on_1_options;
    } else if (_Btn2.isPress()) {
        Begin2_param1 = &_MotorB;
        Begin2_param2 = &_SwB1;
        Begin2_param3 = &_SwB2;
        Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_cw;
        Begin2_returnFn = Begin2_command_wiper_on_2_options;
    } else if (_Btn3.isPress()) {
        Begin2_param4 = &_PumpB;
        Begin2_currentFn = Begin2_subdiagram_spray_on_pump_on;
        Begin2_returnFn = Begin2_command_spray_on_1_options;
    }
}

void Begin2_command_wiper_on_1_options() {
    if (millis() - Begin2_recentBlockFinishTime > 1000) {
        Begin2_currentFn = Begin2_begin_options;
    }
}

void Begin2_command_wiper_on_2_options() {
    if (millis() - Begin2_recentBlockFinishTime > 2000) {
        Begin2_currentFn = Begin2_begin_options;
    }
}

void Begin2_command_spray_on_1_options() {
    Begin2_param1 = &_MotorB;
    Begin2_param2 = &_SwB1;
    Begin2_param3 = &_SwB2;
    Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_cw;
    Begin2_returnFn = Begin2_command_wiper_on_3_options;
}

void Begin2_command_wiper_on_3_options() {
    Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_cw;
    Begin2_returnFn = Begin2_begin_options;
}

void Begin2_subdiagram_wiper_on_motor_cw() {
    Begin2_param1->on(0, 100);
    Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_cw_options;
}

void Begin2_subdiagram_wiper_on_motor_cw_options() {
    if (Begin2_param3->isPress()) {
        Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_ccw;
    }
}

void Begin2_subdiagram_wiper_on_motor_ccw() {
    Begin2_param1->on(1, 100);
    Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_ccw_options;
}

void Begin2_subdiagram_wiper_on_motor_ccw_options() {
    if (Begin2_param2->isPress()) {
        Begin2_currentFn = Begin2_subdiagram_wiper_on_motor_stop;
    }
}

void Begin2_subdiagram_wiper_on_motor_stop() {
    Begin2_param1->off();
    Begin2_currentFn = Begin2_returnFn;
}

void Begin2_subdiagram_spray_on_pump_on() {
    _PumpB.on(100);
    Begin2_recentBlockFinishTime = millis();
    Begin2_currentFn = Begin2_subdiagram_spray_on_pump_on_options;
}

void Begin2_subdiagram_spray_on_pump_on_options() {
    if (millis() - Begin2_recentBlockFinishTime > 2000) {
        Begin2_currentFn = Begin2_subdiagram_spray_on_pump_off;
    }
}

void Begin2_subdiagram_spray_on_pump_off() {
    _PumpB.off();
    Begin2_currentFn = Begin2_returnFn;
}