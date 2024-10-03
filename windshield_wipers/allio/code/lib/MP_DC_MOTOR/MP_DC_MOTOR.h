#ifndef MP_DC_MOTOR_H
#define MP_DC_MOTOR_H

#include "MP_DEVICE.h"

class MP_DC_MOTOR
{
public:
	MP_DC_MOTOR(uint8_t pin1, uint8_t pin2);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	void on(uint8_t direction, uint8_t power);
	void off();
	
private:
	uint8_t pin1;
	uint8_t pin2;
#ifdef MP_DEBUG_ENABLE
	uint8_t direction;
	uint8_t power;
#endif
};

#endif
