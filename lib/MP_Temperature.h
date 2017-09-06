#ifndef MP_TEMPERATURE_H
#define MP_TEMPERATURE_H

//#include "MP_Temp.h"
#include <Wire.h>
#include "Adafruit_TMP007.h"
#include <Arduino.h>


class MP_Temperature  //: MP_Temp
{
  public:
	 ~MP_Temperature() {};

	
	 double getTemperature()  ;
	 void init() ;

  private:
	  Adafruit_TMP007 tmp007;
	 

};

#endif