#ifndef MP_SERVO_PCA9685_H
#define MP_SERVO_PCA9685_H

#include "MP_DEVICE.h"
#include <Wire.h>
#include <Adafruit_PWMServoDriver.h>

class MP_SERVO_PCA9685
{
public:
	MP_SERVO_PCA9685();
	int init();
	void update(unsigned long current_time);
	void printStatus();

	void moveServo(uint8_t channel, int degree);

private:
    Adafruit_PWMServoDriver pwm;
	int degree;
};

#endif
