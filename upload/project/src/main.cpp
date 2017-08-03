#include <Arduino.h>
#include "MP_LED.h"

void beginScene();
void scene1();
void scene2();

void (*currentScene)(void);

MP_LED LED1(3);

void setup() {
    Serial.begin(115200);
    LED1.init();
    currentScene = beginScene;
}

void loop() {
    currentScene();
}

void beginScene() {
    currentScene = scene1;
}

void scene1() {
    LED1.on(100.00);
    delay(2000.00);
    currentScene = scene2;
}

void scene2() {
    LED1.off();
    delay(500.00);
    currentScene = beginScene;
}
