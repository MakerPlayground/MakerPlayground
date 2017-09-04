#ifndef MP_LED_H
#define MP_LED_H

//#include "MP_Led.h"
#include <Arduino.h>

class MP_RGB_LED //: MP_Led
{
  public:
	MP_RGB_LED(uint8_t r, uint8_t g, uint8_t b);
	~MP_RGB_LED() {};

	void init();
	void on(char color[]) ;
	void off() ;

  private:
	uint8_t r;
	uint8_t g;
	uint8_t b;
};

#endif