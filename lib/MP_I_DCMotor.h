#ifndef MP_DCMOTOR_H
#define MP_DCMOTOR_H


#include <Arduino.h>

class MP_DCMotor
{
  public:
	 virtual void on(char dir[], uint8_t speed) const=0;
	 virtual void reverse() const=0;
	 virtual void set_speed(uint8_t speed) const=0;
	 virtual void stop() const =0;
	 virtual void init() const =0;
	
};

#endif