#ifndef MP_TEMP_H
#define MP_TEMP_H

#include <Arduino.h>

class MP_Temp
{
  public:
	 virtual int checkTemp(char opt[], float treshold, uint8_t unit) const = 0;
	 virtual void init() const = 0;

};

#endif