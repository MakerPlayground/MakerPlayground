#ifndef MP_7SEGI2C_H
#define MP_7SEGI2C_H

#include <Arduino.h>
#include "TM1637Display.h"

class MP_7segI2C //: MP_Led
{
  public:
	MP_7segI2C(uint8_t sda,uint8_t sck);
	~MP_7segI2C() {};
	void showValue(double value);
	void showData(double data);
	void setBrightness(char c[]);
	void off() ;
	void init() ;

   private:
   	int brightness;
   	TM1637Display display;

};

#endif
