#include "MP_LIGHT_SENSOR.h"

MP_LIGHT_SENSOR::MP_LIGHT_SENSOR(uint8_t pin)
	:pin(pin)
{
}

int MP_LIGHT_SENSOR::init()
{
	pinMode(this->pin, INPUT);
	return MP_ERR_OK;
}

void MP_LIGHT_SENSOR::update(unsigned long current_time) 
{
	this->value = (analogRead(pin)/1023.0)*100;
}

double MP_LIGHT_SENSOR::getPercent()
{
	return this->value;
}

void MP_LIGHT_SENSOR::printStatus()
{
	Serial.print(F("Value = "));
	Serial.print(this->value);
}