#include <Arduino.h>
#include "MP_Temperature.h"
#include "MP_Button.h"
#include "MP_LED.h"

void beginScene();
void scene1();

void (*currentScene)(void);

MP_Temperature Temperature1;
MP_Button Button1(2);
MP_LED jkkk(3);

void setup() {
    Serial.begin(115200);
    Temperature1.init();
    Button1.init();
    jkkk.init();
    currentScene = beginScene;
}

void loop() {
    currentScene();
}

void beginScene() {
    currentScene = scene1;
}

void scene1() {
    jkkk.on(100.00);
    double Temperature1_Temperature;
    while (1) {
        Temperature1_Temperature = Temperature1.getTemperature();
        if(((Temperature1_Temperature > 30.00)) && Button1.isReleased()) {
            currentScene = beginScene;
            break;
        }
    }
}
