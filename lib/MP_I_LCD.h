#ifndef MP_LCD_H
#define MP_LCD_H

#include <Arduino.h>

class MP_Lcd
{
  public:
	virtual void backlight_on() const = 0;
	virtual void backlight_off() const = 0;
	virtual void show(char text[] ) const = 0 ;
	virtual void clear() const = 0 ;
	virtual void init() const =0;
};

#endif