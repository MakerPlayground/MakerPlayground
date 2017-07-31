#ifndef MP_HUMIDITY_H
#define MP_HUMIDITY_H

#include <Arduino.h>

class MP_Humidity
{
  public:
	virtual int humidity(char opt[], float treshold, uint8_t unit) const = 0;
	virtual void init() const = 0;

};

#endif