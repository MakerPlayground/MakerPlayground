#ifndef MP_ADAFRUIT_MOTORSHIELD_H
#define MP_ADAFRUIT_MOTORSHIELD_H


//#include "MP_DCMotor.h"
#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "Adafruit_MS_PWMServoDriver.h"
#include <Arduino.h>

class MP_Adafruit_MotorShield //: MP_DCMotor
{
  public:
		MP_Adafruit_MotorShield() ;
		~MP_Adafruit_MotorShield() {};
		void on(char dir[], uint8_t speed) ;
		void reverse() ;
		void set_speed(uint8_t speed) ;
		void off() ;
		void init() ;
	
  private:
		Adafruit_MotorShield AFMS =Adafruit_MotorShield();
		Adafruit_DCMotor *myMotor;
		uint8_t pin=0; 
};

#endif