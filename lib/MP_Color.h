#ifndef MP_Color_H
#define MP_Color_H
#define commonAnode true

//#include "MP_Button.h"
#include <Arduino.h>
#include <Wire.h>

class MP_Color //: MP_Button
{
  public:
	 MP_Color() ;
	 ~MP_Color() {};
	 int isColor(char color[]);
	  void init() ;

	  
};

#endif