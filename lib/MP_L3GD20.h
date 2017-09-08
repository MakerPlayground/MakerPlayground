
#ifndef MP_L3GD20_H
#define MP_L3GD20_H

//#include "MP_Gyro.h"
#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_L3GD20_U.h>



class MP_L3GD20 //: MP_Gyro
{
  public:
	
	 ~MP_L3GD20() {};

	 double getGyro_X() ;
	 double getGyro_Y() ;
	 double getGyro_Z() ;
	 void init() ;
	

  private:
	  Adafruit_L3GD20_Unified gyro = Adafruit_L3GD20_Unified(20);
	  sensors_event_t event;
	 

};


#endif