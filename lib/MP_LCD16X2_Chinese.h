/*#ifndef MP_LCD16X2_CHINESE_H
#define MP_LCD16X2_CHINESE_H

//#include "MP_LCD.h"
#include "Wire.h"
#include "Adafruit_LiquidCrystal.h"
#include <Arduino.h>

class MP_LCD16X2_Chinese //: MP_LCD
{
  public:

	~MP_LCD16X2_Chinese() {};

	void backlight_on() const;
	void backlight_off() const;
	void show(char text[]) const;
	void clear() const;
	void init() const;

  private:
	  // Connect via i2c, default address #0 (A0-A2 not jumpered)
	  Adafruit_LiquidCrystal lcd;   

};

#endif*/