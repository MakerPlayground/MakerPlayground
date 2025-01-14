#include "MP_GENERIC_DIGITAL_IN.h"

MP_GENERIC_DIGITAL_IN::MP_GENERIC_DIGITAL_IN(uint8_t pin)
	:pin(pin)
{
}

int MP_GENERIC_DIGITAL_IN::init()
{
	pinMode(this->pin, INPUT_PULLUP);
	return MP_ERR_OK;
}

void MP_GENERIC_DIGITAL_IN::update(unsigned long current_time) 
{

}

void MP_GENERIC_DIGITAL_IN::printStatus() 
{
}

bool MP_GENERIC_DIGITAL_IN::state()
{
	return digitalRead(this->pin);
}
