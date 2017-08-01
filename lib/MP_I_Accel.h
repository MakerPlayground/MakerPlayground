#ifndef MP_ACCEL_H
#define MP_ACCEL_H

#include <Arduino.h>

class MP_Accel
{
  public:
	virtual int accel_x(char opt[],float treshold,uint8_t unit) const = 0;
	virtual int accel_y(char opt[],float treshold, uint8_t unit) const = 0;
	virtual int accel_z(char opt[], float treshold, uint8_t unit) const = 0;
	virtual int slop_x(char opt[], float treshold, uint8_t unit) const = 0;
	virtual int slop_y(char opt[], float treshold, uint8_t unit) const = 0;
	virtual int slop_z(char opt[], float treshold, uint8_t unit) const = 0;
	virtual int tap() const = 0;
	virtual int doubletap() const = 0;
	virtual int freefall() const = 0;
	virtual void init() const =0;
	
};

#endif