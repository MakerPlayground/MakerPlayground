#include "MP_LED.h"

MP_LED::MP_LED(uint8_t pin)
  : pin(pin)
{
  
}

void MP_LED::init() 
{
	pinMode(this->pin, OUTPUT);
}


void MP_LED::on(int a) 
{
  digitalWrite(this->pin, HIGH);
}

void MP_LED::off() 
{
  digitalWrite(this->pin, LOW);
}


void MP_LED::dim(uint8_t percentage) 
{
	analogWrite(this->pin, 255 * percentage); /* Only PWM Pin */
}
