#ifndef MP_RGB_LED5V_H
#define MP_RGB_LED5V_H

//#include "MP_Led.h"
#include <Arduino.h>

class MP_RGB_LED5V //: MP_Led
{
  public:
	MP_RGB_LED5V(uint8_t r, uint8_t g, uint8_t b);
	~MP_RGB_LED5V() {};

	void init();
	void on(char color[]) ;
	void off() ;

  private:
	uint8_t r;
	uint8_t g;
	uint8_t b;
};

#endif