#ifndef MP_Presence_H
#define MP_Presence_H

#include <Arduino.h>

class MP_Presence //: MP_Led
{
  public:
	MP_Presence(uint8_t pin);
	~MP_Presence() {};

	int isDetected();
	void init();

  private:
	uint8_t pin;

};

#endif