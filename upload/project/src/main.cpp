#include <Arduino.h>
#include "MP_DC_Motor.h"

void beginScene();
void scene1();

void (*currentScene)(void);

MP_DC_Motor DC_Motor1;

void setup() {
    Serial.begin(115200);
    DC_Motor1.init();
    currentScene = beginScene;
}

void loop() {
    currentScene();
}

void beginScene() {
    currentScene = scene1;
}

void scene1() {
    DC_Motor1.on("Forward", 100.00);
    currentScene = beginScene;
}
