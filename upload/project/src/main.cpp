#include <Arduino.h>
#include "MP_LED.h"
#include "MP_BME280.h"
#include "MP_LSM9DS1.h"

void beginScene();
void scene1();

void (*currentScene)(void);

MP_LED LED1(3);
MP_BME280 Temperature1;
MP_LSM9DS1 Accelerometer1;

void setup() {
    Serial.begin(115200);
    LED1.init();
    Temperature1.init();
    Accelerometer1.init();
    currentScene = beginScene;
}

void loop() {
    currentScene();
}

void beginScene() {
    double Accelerometer1_Accel_X;
    while (1) {
        Accelerometer1_Accel_X = Accelerometer1.getAccel_X();
        if((Accelerometer1_Accel_X < 78.78787878787881 && Accelerometer1_Accel_X > -65.07936507936506)) {
            currentScene = scene1;
            break;
        }
    }
}

void scene1() {
    LED1.on(50.00);
    currentScene = beginScene;
}
