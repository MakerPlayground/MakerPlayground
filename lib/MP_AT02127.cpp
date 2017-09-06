#include "MP_AT02127.h"


MP_AT02127::MP_AT02127(uint8_t pin)
  : pin(pin)
{
  
}

void MP_AT02127::init() 
{
	pinMode(this->pin,INPUT);
}


double MP_AT02127::getValue() 
{
  return  digitalRead(pin)*1.0; 
}

