#include "MP_Presence.h"


MP_Presence::MP_Presence(uint8_t pin)
  : pin(pin)
{
  
}

void MP_Presence::init() 
{
	pinMode(this->pin,INPUT);
}

int MP_Presence::isDetected() 
{
  return  digitalRead(pin) == HIGH; 
}
