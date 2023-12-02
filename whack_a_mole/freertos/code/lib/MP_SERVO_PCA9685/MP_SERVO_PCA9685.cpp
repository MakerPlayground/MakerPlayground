#include "MP_SERVO_PCA9685.h"

#define SERVOMIN  95
#define SERVOMAX  510

MP_SERVO_PCA9685::MP_SERVO_PCA9685()
  : degree(0)
{
}

int MP_SERVO_PCA9685::init()
{
    pwm.begin();
    pwm.setOscillatorFrequency(25000000);   // to be expose as property in future release
    pwm.setPWMFreq(50);
    return MP_ERR_OK;
}

void MP_SERVO_PCA9685::update(unsigned long current_time) 
{
}

void MP_SERVO_PCA9685::printStatus()
{
    Serial.print(F("current angle = "));
    Serial.println(this->degree);
}

void MP_SERVO_PCA9685::moveServo(uint8_t channel, int degree)
{
    this->degree = degree;
	pwm.setPWM(channel, 0, map(degree, 0, 180, SERVOMIN, SERVOMAX));
}
