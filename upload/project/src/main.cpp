#include <Arduino.h>
#include "MP_OneAnalogReadToPercent.h"

void beginScene();
void scene1();

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
    double _Light1_Light;
    while (1) {
        _Light1_Light = _Light1.getLight();
        if((Light1_Light < 75.0 && Light1_Light > 25.0)) {
            currentScene = scene1;
            break;
        }
    }
}

void scene1() {
    currentScene = beginScene;
}
