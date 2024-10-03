#include "MP_DC_MOTOR.h"

MP_DC_MOTOR::MP_DC_MOTOR(uint8_t pin1, uint8_t pin2)
	: pin1(pin1)
	, pin2(pin2)
{
}

int MP_DC_MOTOR::init()
{
	pinMode(this->pin1, OUTPUT);
	analogWrite(this->pin1, 0);
	pinMode(this->pin2, OUTPUT);
	analogWrite(this->pin2, 0);
	return MP_ERR_OK;
}

void MP_DC_MOTOR::update(unsigned long current_time) 
{
}

#ifdef MP_DEBUG_ENABLE
void MP_DC_MOTOR::printStatus() 
{
	Serial.print(F("direction = "));
	Serial.print(this->direction);
	Serial.print(F(" power = "));
	Serial.print(this->power);
}
#endif

void MP_DC_MOTOR::on(uint8_t direction, uint8_t power)
{
#ifdef MP_DEBUG_ENABLE
	this->direction = direction;
	this->power = power;
#endif
	if (direction == 0) 
	{
		analogWrite(this->pin1, power);
		analogWrite(this->pin2, 0);
	} 
	else 
	{
		analogWrite(this->pin1, 0);
		analogWrite(this->pin2, power);
	}
}

void MP_DC_MOTOR::off()
{
	analogWrite(this->pin1, 0);
	analogWrite(this->pin2, 0);
}