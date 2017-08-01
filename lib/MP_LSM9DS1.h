#ifndef MP_LSM9DS1_H
#define MP_LSM9DS1_H

//#include "MP_Accel.h"
#include <Arduino.h>
#include <Wire.h>
#include <SPI.h>
#include <SparkFunLSM9DS1.h>

class MP_LSM9DS1 //: MP_Accel
{
  public:
  	  MP_LSM9DS1();
	 ~MP_LSM9DS1() {};

	 double getAccelX();
	 double getAccelY();
	 double getAccelZ();
	 double getSlopX();
	 double getSlopY();
	 double getSlopZ();
	 double getRotateX();
	 double getRotateY();
	 double getRotateZ();
	 double getMagX();
	 double getMagY();
	 double getMagZ();
	 int compass(char opt[])  ;
	 int tap()  ;
	 int doubletap()  ;
	 int freefall()  ;
	 void init() ;

  private:
	  LSM9DS1 imu;

};

#endif