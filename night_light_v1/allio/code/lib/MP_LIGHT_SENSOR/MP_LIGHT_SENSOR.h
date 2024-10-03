#ifndef MP_LIGHT_SENSOR_H
#define MP_LIGHT_SENSOR_H

#include "MP_DEVICE.h"

class MP_LIGHT_SENSOR
{
public:
	MP_LIGHT_SENSOR(uint8_t pin);
	int init();
	void update(unsigned long current_time);
	void printStatus();
	
	double getPercent();

private:
	uint8_t pin;
	double value;
};

#endif
