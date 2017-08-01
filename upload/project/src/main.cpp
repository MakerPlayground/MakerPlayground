#include <Arduino.h>
#include "MP_LED.h"
#include "MP_Button.h"

void (*currentScene)(void);

MP_Button Button1();
MP_LED LED1();

void setup() {
    Serial.begin(115200);
    Button1.init();
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
    while (1) {
        if((Button1.isPressed())) {
            currentScene = scene2;
            break;
        }
    }
}

void scene2() {
    LED1.off();
    currentScene = beginScene;
}
