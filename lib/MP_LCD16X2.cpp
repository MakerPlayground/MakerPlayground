#if !defined(ESP8266)
#include "MP_LCD16X2.h"


void MP_LCD16X2::init() 
{
	Adafruit_LiquidCrystal lcd(0);  
	lcd.begin(16, 2);
}

void MP_LCD16X2::backlight_on() 
{
	lcd.setBacklight(HIGH);
}

void MP_LCD16X2::backlight_off() 
{
	lcd.setBacklight(LOW);
}

void MP_LCD16X2::show(char text[]) 
{
	lcd.print(text);
}

void MP_LCD16X2::clear() 
{
	lcd.clear();
}
#endif