#ifndef MP_GYRO_H
#define MP_GYRO_H

#include <Arduino.h>

class MP_Gyro
{
  public:
	virtual int rotate_x(char opt[],float treshold,uint8_t unit) const = 0;
	virtual int rotate_y(char opt[],float treshold, uint8_t unit) const = 0;
	virtual int rotate_z(char opt[], float treshold, uint8_t unit) const = 0;
	virtual void init() const =0;
	
};

#endif