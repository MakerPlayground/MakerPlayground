#ifndef MP_RGB_LEDGND_H
#define MP_RGB_LEDGND_H

//#include "MP_Led.h"
#include <Arduino.h>

class MP_RGB_LEDGND //: MP_Led
{
  public:
	MP_RGB_LEDGND(uint8_t r, uint8_t g, uint8_t b);
	~MP_RGB_LEDGND() {};

	void init();
	void on(char color[]) ;
	void off() ;

  private:
	uint8_t r;
	uint8_t g;
	uint8_t b;
};

#endif