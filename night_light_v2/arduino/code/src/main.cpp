#include "Arduino.h"

const int BUTTON_PIN = 2;
const int LED_PIN = 10;
const int LDR_PIN = A0;

void setup() {
    Serial.begin(115200);

    pinMode(BUTTON_PIN, INPUT_PULLUP);

    pinMode(LED_PIN, OUTPUT);
	analogWrite(LED_PIN, 0);

    pinMode(LDR_PIN, INPUT);
}

void wait_for_button_press_and_release() {
    while(digitalRead(BUTTON_PIN));
    delay(30);
    while(!digitalRead(BUTTON_PIN));
    delay(30);
}

void loop() {
    analogWrite(LED_PIN, 0);
    wait_for_button_press_and_release();

    while (digitalRead(BUTTON_PIN)) {
        float lightValue = (analogRead(LDR_PIN) / 1023.0) * 100;
        if (lightValue < 50.0) {
            analogWrite(LED_PIN, 255);
        } else {
            analogWrite(LED_PIN, 0);
        }
        unsigned long startTime = millis();
        while ((millis() - startTime < 10000) && digitalRead(BUTTON_PIN));
    }      
    wait_for_button_press_and_release();

    analogWrite(LED_PIN, 255);
    wait_for_button_press_and_release();
}