#ifndef MP_BARO_H
#define MP_BARO_H

#include <Arduino.h>

class MP_Baro
{
  public:
	virtual int pressure(char opt[],float treshold,uint8_t unit) const = 0;
	virtual int attitude(char opt[],float treshold, uint8_t unit) const = 0;
	virtual void init() const =0;
	
};

#endif