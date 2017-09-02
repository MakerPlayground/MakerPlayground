#ifndef MP_BUTTON_H
#define MP_BUTTON_H

//#include "MP_Button.h"
#include <Arduino.h>



class MP_Button //: MP_Button
{
  public:
	 MP_Button(uint8_t pin) ;
	 ~MP_Button() {};

	  int isReleased()  ;
	  int doubleRelease()  ;
	  void init() ;
	 

   private:
	  uint8_t pin;

};

#endif