#include "MP_Buzzer.h"

MP_Buzzer::MP_Buzzer(uint8_t pin) 
    : pin(pin) {
}

void MP_Buzzer::init() {
    pinMode(this->pin, OUTPUT);
}

void MP_Buzzer::beep(double frequency, double duration) { 
    //tone(this->pin, frequency, duration);
    tone(this->pin, frequency);
    delay(duration);
    noTone(this->pin);
}