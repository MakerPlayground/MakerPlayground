#include<Arduino.h>

#define LED_PIN 12

void setup()
{
    Serial.begin(115200);

    pinMode(2, INPUT_PULLUP);
    pinMode(3, INPUT_PULLUP);
    pinMode(4, INPUT_PULLUP);
    pinMode(5, INPUT_PULLUP);
    pinMode(6, INPUT_PULLUP);
    pinMode(7, INPUT_PULLUP);
    pinMode(8, INPUT_PULLUP);
    pinMode(9, INPUT_PULLUP);

    pinMode(LED_PIN, OUTPUT);
    digitalWrite(LED_PIN, LOW);
}

void loop()
{
    if (digitalRead(2) == LOW) 
    {
        digitalWrite(LED_PIN, HIGH);
        delay(10);
        digitalWrite(LED_PIN, LOW);
        Serial.println('2');
    }
    if (digitalRead(3) == LOW) 
    {
        Serial.println('3');
    }
    if (digitalRead(4) == LOW) 
    {
        Serial.println('4');
    }
    if (digitalRead(5) == LOW) 
    {
        Serial.println('5');
    }
    if (digitalRead(6) == LOW) 
    {
        Serial.println('6');
    }
    if (digitalRead(7) == LOW) 
    {
        Serial.println('7');
    }
    if (digitalRead(8) == LOW) 
    {
        Serial.println('8');
    }
    if (digitalRead(9) == LOW) 
    {
        Serial.println('9');
    }
}