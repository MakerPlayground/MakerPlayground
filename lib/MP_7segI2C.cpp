#include "MP_SolidStateRelay.h"

MP_SolidStateRelay::MP_SolidStateRelay(uint8_t pin)
  : pin(pin)
{
  
}

void MP_SolidStateRelay::init() 
{
	pinMode(this->pin, OUTPUT);
}


void MP_SolidStateRelay::on(int a) 
{
  digitalWrite(this->pin, LOW);
}

void MP_SolidStateRelay::off() 
{
  digitalWrite(this->pin, HIGH);
}
