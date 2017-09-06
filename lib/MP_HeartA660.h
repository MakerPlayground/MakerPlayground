#ifndef MP_HEARTA660_H
#define MP_HEARTA660_H

#include <Arduino.h>

class MP_HeartA660 //: MP_Led
{
  public:
	MP_HeartA660(uint8_t pin);

	int isDetected();
	void init();

  private:
	uint8_t pin;

};

#endif