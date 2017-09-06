#ifndef MP_TCS3200_H
#define MP_TCS3200_H
#define commonAnode true

//#include "MP_Button.h"
#include <Arduino.h>
#include <Wire.h>

class MP_TCS3200 //: MP_Button
{
  public:
	 MP_TCS3200() ;
	 ~MP_TCS3200() {};
	 int isColor(char color[]);
	  void init() ;

	  
};

#endif