#include <Arduino.h>
#include "MP_OneAnalogReadToPercent.h"

void beginScene();

void (*currentScene)(void);

MP_OneAnalogReadToPercent _Light1(A5);

void setup() {
    Serial.begin(115200);
    _Light1.init();
    currentScene = beginScene;
}

void loop() {
    currentScene();
}

void beginScene() {
    double _Light1_Percent;
    while (1) {
        _Light1_Percent = _Light1.getPercent();
        if((_Light1_Percent < 75.0 && _Light1_Percent > 25.0)) {
            currentScene = beginScene;
            break;
        }
    }
}
