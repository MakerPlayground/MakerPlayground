#include "Arduino.h"

const int LED_PIN = 10;
const int LDR_PIN = A0;

void setup() {
    Serial.begin(115200);

    pinMode(LED_PIN, OUTPUT);
	analogWrite(LED_PIN, 0);

    pinMode(LDR_PIN, INPUT);
}

void loop() {
    float lightValue = (analogRead(LDR_PIN) / 1023.0) * 100;
    if (lightValue < 50.0) {
        analogWrite(LED_PIN, 255);
    } else if (lightValue >= 50.0) {
        analogWrite(LED_PIN, 0);
    }
}