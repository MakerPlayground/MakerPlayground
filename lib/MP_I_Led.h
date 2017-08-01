#ifndef MP_LED_H
#define MP_LED_H

#include <Arduino.h>

class MP_Led
{
  public:
	virtual void on(int a) const = 0;
	virtual void off() const = 0;
	virtual void dim(uint8_t percentage) const = 0 ;
	virtual void init() const = 0;
};

#endif