#ifndef MP_GENERIC_DIGITAL_OUT_H
#define MP_GENERIC_DIGITAL_OUT_H

#include "MP_DEVICE.h"

class MP_GENERIC_DIGITAL_OUT
{
public:
	MP_GENERIC_DIGITAL_OUT(uint8_t pin);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	void on();
	void off();
	
private:
	uint8_t pin;
};

#endif
