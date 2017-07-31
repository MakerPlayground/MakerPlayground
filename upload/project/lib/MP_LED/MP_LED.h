#ifndef MP_LED_H
#define MP_LED_H

//#include "MP_Led.h"
#include <Arduino.h>

class MP_LED //: MP_Led
{
  public:
	MP_LED(uint8_t pin);
	~MP_LED() {};

	void on(int a) ;
	void off() ;
	void dim(uint8_t percentage) ;
	void init() ;


  private:
	uint8_t pin;

};

#endif