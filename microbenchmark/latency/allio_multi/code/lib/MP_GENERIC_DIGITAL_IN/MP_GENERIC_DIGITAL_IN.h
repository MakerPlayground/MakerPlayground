#ifndef MP_GENERIC_DIGITAL_IN_H
#define MP_GENERIC_DIGITAL_IN_H

#include "MP_DEVICE.h"

class MP_GENERIC_DIGITAL_IN
{
public:
	MP_GENERIC_DIGITAL_IN(uint8_t pin);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	bool state();
	
private:
	uint8_t pin;
};

#endif
