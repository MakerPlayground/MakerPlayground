#ifndef MP_DELAY_H
#define MP_DELAY_H

//#include "MP_General.h"
#include <Arduino.h>




class MP_Delay //: MP_General
{
  public:
	  
	 ~MP_Delay() {};

	 int delayFn(char a[],uint16_t time, char b[])  ;
	 void init() ;


};

#endif