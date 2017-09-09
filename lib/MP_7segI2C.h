#ifndef MP_SolidStateRelay_H
#define MP_SolidStateRelay_H

//#include "MP_Led.h"
#include <Arduino.h>

class MP_SolidStateRelay //: MP_Led
{
  public:
	MP_SolidStateRelay(uint8_t pin);
	~MP_SolidStateRelay() {};

	void on(int a) ;
	void off() ;
	void init() ;


  private:
	uint8_t pin;

};

#endif
