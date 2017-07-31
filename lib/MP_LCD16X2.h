
#ifndef MP_LCD16X2_H
#define MP_LCD16X2_H
#if !defined(ESP8266)
//#include "MP_LCD.h"
#include "Wire.h"
#include "Adafruit_LiquidCrystal.h"
#include <Arduino.h>

class MP_LCD16X2 //: MP_LCD
{
  public:

	~MP_LCD16X2() {};

	void backlight_on() ;
	void backlight_off() ;
	void show(char text[]) ;
	void clear() ;
	void init() ;

  private:
	  // Connect via i2c, default address #0 (A0-A2 not jumpered)
	  Adafruit_LiquidCrystal lcd;   

};

#endif
#endif