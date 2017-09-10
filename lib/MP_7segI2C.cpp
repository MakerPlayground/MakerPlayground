#include "MP_7segI2C.h"

MP_7segI2C::MP_7segI2C(uint8_t sda,uint8_t sck)
  : display(TM1637Display ( sck,sda))//,sda(sda),sck(sck)
{
  
}

void MP_7segI2C::init() 
{
	Serial.begin(112500);
	brightness = 7;

}

void MP_7segI2C::showValue(double value)
{
   display.setBrightness(brightness, true); // Turn on
   display.showNumberDec((int)value, false);



}
void MP_7segI2C::showData(double data)
{
   display.setBrightness(brightness, true); // Turn on
   display.showNumberDec((int)data, false);
}

void MP_7segI2C::setBrightness(char c[])
{
	brightness = (int)c[0]-49;
	Serial.println(brightness);

}
	

void MP_7segI2C::off()
{
	uint8_t data[] = { 0x0, 0x0, 0x0, 0x0 };
	display.setSegments(data); 
}
	





