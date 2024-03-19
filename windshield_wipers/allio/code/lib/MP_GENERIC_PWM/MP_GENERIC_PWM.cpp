#include "MP_GENERIC_PWM.h"

MP_GENERIC_PWM::MP_GENERIC_PWM(uint8_t pin)
	: pin(pin)
{
}

int MP_GENERIC_PWM::init()
{
	pinMode(this->pin, OUTPUT);
	analogWrite(this->pin, 0);
	return MP_ERR_OK;
}

void MP_GENERIC_PWM::update(unsigned long current_time) 
{
}

#ifdef MP_DEBUG_ENABLE
void MP_GENERIC_PWM::printStatus() 
{
	Serial.print(F(" power = "));
	Serial.print(this->power);
}
#endif

void MP_GENERIC_PWM::on(uint8_t power)
{
#ifdef MP_DEBUG_ENABLE
	this->power = power;
#endif
	analogWrite(this->pin, power);
}

void MP_GENERIC_PWM::off()
{
	analogWrite(this->pin, 0);
}