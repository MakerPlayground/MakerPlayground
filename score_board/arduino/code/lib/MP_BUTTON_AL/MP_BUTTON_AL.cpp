#include "MP_BUTTON_AL.h"

MP_BUTTON_AL::MP_BUTTON_AL(uint8_t pin)
	:pin(pin)
{
}

int MP_BUTTON_AL::init()
{
	pinMode(pin, INPUT_PULLUP);
    checkpoint = 0;
	return MP_ERR_OK;
}

void MP_BUTTON_AL::update(unsigned long current_time)
{
    if (state == RELEASED && (digitalRead(pin) == LOW) && current_time >= 30 + checkpoint) {
        state = PRESSED;
        checkpoint = millis();
    }
    else if (state == PRESSED && (digitalRead(pin) == HIGH) && current_time >= 30 + checkpoint) {
        state = JUST_RELEASE;
        checkpoint = millis();
    }
    else if (state == JUST_RELEASE && current_time >= 150 + checkpoint) {
        state = RELEASED;
        checkpoint = millis();
    }
}

void MP_BUTTON_AL::printStatus() 
{
	Serial.print(F("Button is "));
	Serial.println(isPress() ? F("pressed"): F("not pressed"));
}

bool MP_BUTTON_AL::isPress()
{
    return state == PRESSED; // (digitalRead(pin) == LOW);
}

bool MP_BUTTON_AL::isPressAndRelease()
{
    if (state == JUST_RELEASE) {
        state = RELEASED;
        checkpoint = millis();
        return true;
    }
    return false;
}

bool MP_BUTTON_AL::isNotPress()
{
    return state == RELEASED; // (digitalRead(pin) == HIGH);
}