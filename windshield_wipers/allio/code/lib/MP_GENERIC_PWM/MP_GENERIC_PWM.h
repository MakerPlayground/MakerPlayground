#ifndef MP_GENERIC_PWM_H
#define MP_GENERIC_PWM_H

#include "MP_DEVICE.h"

class MP_GENERIC_PWM
{
public:
	MP_GENERIC_PWM(uint8_t pin);
	int init();
	void update(unsigned long current_time);
#ifdef MP_DEBUG_ENABLE
	void printStatus();
#endif

	void on(uint8_t power);
	void off();
	
private:
	uint8_t pin;
#ifdef MP_DEBUG_ENABLE
	uint8_t power;
#endif
};

#endif
