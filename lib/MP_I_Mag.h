#ifndef MP_MAG_H
#define MP_MAG_H

#include <Arduino.h>

class MP_Mag
{
  public:
	virtual int compass(char opt[]) const = 0;
	virtual int mag_x(char opt[],float treshold,uint8_t unit) const = 0;
	virtual int mag_y(char opt[],float treshold, uint8_t unit) const = 0;
	virtual int mag_z(char opt[], float treshold, uint8_t unit) const = 0;
	virtual void init() const=0;

	
};

#endif