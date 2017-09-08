#ifndef MP_LSM303_H
#define MP_LSM303_H

//#include "MP_Mag.h"
#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303_U.h>



class MP_LSM303// : MP_Mag
{
  public:
	 
	 ~MP_LSM303() {};
	 double getAccel_X();
	 double getAccel_Y();
	 double getAccel_Z();
	 int checkDirection(char opt[])  ;
	 double getMag_X();
	 double getMag_Y();
	 double getMag_Z();
	 void init() ;
	

  private:
	  Adafruit_LSM303 lsm;
      sensors_event_t event;
	 

};

#endif