#ifndef MP_AT02127_H
#define MP_AT02127_H

#include <Arduino.h>

class MP_AT02127 //: MP_Led
{
  public:
	MP_AT02127(uint8_t pin);
	~MP_AT02127() {};

	double getValue() ;
	void init() ;


  private:
	uint8_t pin;

};

#endif