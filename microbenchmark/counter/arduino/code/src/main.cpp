#include <Arduino.h>

const int pin = 0;
volatile int count = 0;

void setup() {
    pinMode(pin, OUTPUT);
}

void loop() {
    digitalWrite(pin, LOW);
    count = 0;
    for (int i=0; i<25000; i++) {
        count += 1;
    }
    digitalWrite(pin, HIGH);
    delay(10);
}