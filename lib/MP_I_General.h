#ifndef MP_GENERNAL_H
#define MP_GENERNAL_H

#include <Arduino.h>

class MP_General
{
  public:
	virtual int delayFn(char a[],uint16_t time, char b[]) const = 0;
	virtual void init() const=0;

	
};

#endif