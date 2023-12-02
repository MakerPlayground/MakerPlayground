// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_LED_AH.h"
#include "MP_LIGHT_SENSOR.h"

void (*current_Begin1)(void);
void begin_begin1();
void begin_begin1_options();
void command_light_off();
void command_light_on();

MP_LED_AH _LED1(10);
MP_LIGHT_SENSOR _Light1(A0);

void setup() {
    MPSerial.begin(115200);
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
    current_Begin1 = begin_begin1_options;
}

void command_light_on() {
    update();
    _LED1.on(100.0);
    current_Begin1 = begin_begin1;
}

void command_light_off() {
    update();
    _LED1.off();
    current_Begin1 = begin_begin1;
}

void begin_begin1_options() {
    update();
    if((( _Light1.getPercent() < 50.0 ))) {
        current_Begin1 = command_light_on;
    }
    if((( _Light1.getPercent() >= 50.0 ))) {
        current_Begin1 = command_light_off;
    }
}
