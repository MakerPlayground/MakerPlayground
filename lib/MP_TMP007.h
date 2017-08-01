#ifndef MP_TMP007_H
#define MP_TMP007_H

//#include "MP_Temp.h"
#include <Wire.h>
#include "Adafruit_TMP007.h"
#include <Arduino.h>


class MP_TMP007  //: MP_Temp
{
  public:
	 ~MP_TMP007() {};

	
	 double getTemp()  ;
	 void init() ;

  private:
	  Adafruit_TMP007 tmp007;
	 

};

#endif