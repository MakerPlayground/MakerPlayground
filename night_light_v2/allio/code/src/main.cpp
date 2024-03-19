// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_BUTTON_AL.h"
#include "MP_LED_AH.h"
#include "MP_LIGHT_SENSOR.h"

unsigned long Begin1_recentBlockFinishTime_1 = 0;
void (*current_Begin1)(void);
void begin_begin1();
void command_light_off();
void command_light_off_options();
void subdiagram_light_auto_begin1_options();
void subdiagram_light_auto_light_on();
void subdiagram_light_auto_light_off();
void subdiagram_light_auto_light_on_light_off_options();
void command_light_on();
void command_light_on_options();

MP_BUTTON_AL _Button1(2);
MP_LED_AH _LED1(10);
MP_LIGHT_SENSOR _Light1(A0);

void setup() {
    MPSerial.begin(115200);

    status_code = _Button1.init();
    if (status_code != 0) {
        MP_ERR("Button1", status_code);
        while(1);
    }

    status_code = _LED1.init();
    if (status_code != 0) {
        MP_ERR("LED1", status_code);
        while(1);
    }

    status_code = _Light1.init();
    if (status_code != 0) {
        MP_ERR("Light1", status_code);
        while(1);
    }

    current_Begin1 = begin_begin1;
}

void loop() {
    update();
    current_Begin1();
}

void update() {
    currentTime = millis();

    _Button1.update(currentTime);
    _LED1.update(currentTime);
    _Light1.update(currentTime);

#ifdef MP_DEBUG_ENABLE
    if (currentTime - latestLogTime > MP_LOG_INTERVAL) {
        PR_VAL(F("Light1"));
        MPSerial.print("Percent="); MPSerial.print(_Light1.getPercent());
        PR_END();
        latestLogTime = millis();
    }
#endif
}

void begin_begin1() {
    current_Begin1 = command_light_off;
}

void command_light_off() {
    update();
    _LED1.off();
    current_Begin1 = command_light_off_options;
}

void command_light_off_options() {
    update();
    if (_Button1.isPressAndRelease()) {
        current_Begin1 = subdiagram_light_auto_begin1_options;
    }
}

void subdiagram_light_auto_begin1_options() {
    update();
    if (_Button1.isPressAndRelease()) {
        current_Begin1 = command_light_on;
    } else if (_Light1.getPercent() < 50.0) {
        current_Begin1 = subdiagram_light_auto_light_on;
    } else {
        current_Begin1 = subdiagram_light_auto_light_off;
    }
}

void subdiagram_light_auto_light_on() {
    update();
    _LED1.on(100.0);
    Begin1_recentBlockFinishTime_1 = millis();
    current_Begin1 = subdiagram_light_auto_light_on_light_off_options;
}

void subdiagram_light_auto_light_off() {
    update();
    _LED1.off();
    Begin1_recentBlockFinishTime_1 = millis();
    current_Begin1 = subdiagram_light_auto_light_on_light_off_options;
}

void subdiagram_light_auto_light_on_light_off_options() {
    update();
    if (_Button1.isPressAndRelease()) {
        current_Begin1 = command_light_on;
    } else if (millis() - Begin1_recentBlockFinishTime_1 > 10000) {
        current_Begin1 = subdiagram_light_auto_begin1_options;
    }
}

void command_light_on() {
    update();
    _LED1.on(100.0);
    current_Begin1 = command_light_on_options;
}

void command_light_on_options() {
    update();
    if (_Button1.isPressAndRelease()) {
        current_Begin1 = command_light_off;
    }
}
