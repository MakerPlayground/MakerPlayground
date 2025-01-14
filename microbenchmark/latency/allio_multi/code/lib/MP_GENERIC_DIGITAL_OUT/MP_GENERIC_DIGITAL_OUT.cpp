#include "MP_GENERIC_DIGITAL_OUT.h"

MP_GENERIC_DIGITAL_OUT::MP_GENERIC_DIGITAL_OUT(uint8_t pin)
	:pin(pin)
{
}

int MP_GENERIC_DIGITAL_OUT::init()
{
	pinMode(this->pin, OUTPUT);
	digitalWrite(this->pin, LOW);
	return MP_ERR_OK;
}

void MP_GENERIC_DIGITAL_OUT::update(unsigned long current_time) 
{

}

void MP_GENERIC_DIGITAL_OUT::printStatus() 
{
}

void MP_GENERIC_DIGITAL_OUT::on()
{
	digitalWrite(this->pin, HIGH);
}

void MP_GENERIC_DIGITAL_OUT::off()
{
	digitalWrite(this->pin, LOW);
}