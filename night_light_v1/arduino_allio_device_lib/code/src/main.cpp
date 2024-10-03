// uncomment the following line to print error and debug messages
// #define MP_DEBUG_ENABLE

#include "MakerPlayground.h"
#include "MP_LED_AH.h"
#include "MP_LIGHT_SENSOR.h"

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
}

void loop() {
    _Light1.update(millis());
    if (_Light1.getPercent() < 50.0) {
        _LED1.on(100.0);
    } else if (_Light1.getPercent() >= 50.0) {
        _LED1.off();
    }
}