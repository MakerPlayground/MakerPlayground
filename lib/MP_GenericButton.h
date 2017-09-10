#ifndef MP_GENERICBUTTON_H
#define MP_GENERICBUTTON_H

//#include "MP_Button.h"
#include <Arduino.h>



class MP_GenericButton //: MP_Button
{
  public:
	 MP_GenericButton(uint8_t pin) ;
	 ~MP_GenericButton() {};

	  int isReleased()  ;
	  int isMultiPressed(double press)  ;
	  int isPressed()  ;
	  void init() ;
	 

   private:
	  uint8_t pin;

};

#endif
