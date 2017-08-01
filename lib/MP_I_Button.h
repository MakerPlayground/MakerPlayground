#ifndef MP_BUTTON_H
#define MP_BUTTON_H

#include <Arduino.h>

class MP_Button
{
  public:
	virtual int release() const = 0;
	virtual int doubleRelease() const = 0;
	virtual void init() const =0;

	
};

#endif