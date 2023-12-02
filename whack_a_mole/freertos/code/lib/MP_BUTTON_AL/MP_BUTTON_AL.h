#ifndef MP_BUTTON_AL_H
#define MP_BUTTON_AL_H

#include "MP_DEVICE.h"

class MP_BUTTON_AL
{
public:
	MP_BUTTON_AL(uint8_t pin);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	bool isPress();
	bool isPressAndRelease();
	bool isNotPress();

private:
	uint8_t pin;
	unsigned long checkpoint;
	enum ButtonState { RELEASED, PRESSED, JUST_RELEASE };
	ButtonState state = RELEASED;
};

#endif
