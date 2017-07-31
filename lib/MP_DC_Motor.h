#ifndef MP_DC_Motor_H
#define MP_DC_Motor_H


//#include "MP_DCMotor.h"
#include <Wire.h>
#include <Adafruit_MotorShield.h>
#include "utility/Adafruit_MS_PWMServoDriver.h"
#include <Arduino.h>

class MP_DC_Motor //: MP_DCMotor
{
  public:
		MP_DC_Motor() ;
		~MP_DC_Motor() {};
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