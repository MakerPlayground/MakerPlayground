#include "MP_HC_SR501.h"


MP_HC_SR501::MP_HC_SR501(uint8_t pin)
  : pin(pin)
{
  
}

void MP_HC_SR501::init() 
{
	pinMode(this->pin,INPUT);
}

int MP_HC_SR501::isDetected() 
{
  return  digitalRead(pin) == HIGH; 
}
