#ifndef MP_GENERICBUTTON_H
#define MP_GENERICBUTTON_H

//#include "MP_Button.h"
#include <Arduino.h>



class MP_GenericButton //: MP_Button
{
  public:
	 MP_GenericButton(uint8_t pin) ;
	 ~MP_GenericButton() {};

	  int release()  ;
	  int doubleRelease()  ;
	  void init() ;
	 

   private:
	  uint8_t pin;

};

#endif