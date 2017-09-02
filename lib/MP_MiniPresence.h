#ifndef MP_MiniPresence_H
#define MP_MiniPresence_H

#include <Arduino.h>

class MP_MiniPresence //: MP_Led
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