#include "MP_HeartA660.h"


MP_HeartA660::MP_HeartA660(uint8_t pin)
  : pin(pin)
{
  
}

void MP_HeartA660::init() 
{
	pinMode(this->pin,INPUT);
}

int MP_HeartA660::isDetected() 
{
  return  analogRead(pin) > 550 ; 
}
