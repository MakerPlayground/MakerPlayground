#ifndef MP_LSM303_H
#define MP_LSM303_H

//#include "MP_Mag.h"
#include <Arduino.h>
#include <Wire.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_LSM303.h>



class MP_LSM303// : MP_Mag
{
  public:
	 
	 ~MP_LSM303() {};
	 double getAccelX();
	 double getAccelY();
	 double getAccelZ();
	 int compass(char opt[])  ;
	 double getMagX();
	 double getMagY();
	 double getMagZ();
	 void init() ;
	

  private:
	  Adafruit_LSM303 lsm;
      sensors_event_t event;
	 

};

#endif