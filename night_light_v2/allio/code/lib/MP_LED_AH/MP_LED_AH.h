#ifndef MP_LED_AH_H
#define MP_LED_AH_H

#include "MP_DEVICE.h"

class MP_LED_AH
{
public:
	MP_LED_AH(uint8_t pin);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	void on(int brightness);
	void off();
	
private:
	uint8_t pin;
	int brightness;
};

#endif
