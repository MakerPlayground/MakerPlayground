#ifndef MP_SOIL_H
#define MP_SOIL_H

#include <Arduino.h>

class MP_Soil //: MP_Led
{
  public:
	MP_Soil(uint8_t pin);
	~MP_Soil() {};

	double getValue() ;
	void init() ;


  private:
	uint8_t pin;

};

#endif