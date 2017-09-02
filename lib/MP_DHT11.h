
#ifndef MP_DHT11_H
#define MP_DHT11_H

//#include "MP_Baro.h"
#include "DHT.h"

#include <Arduino.h>


class MP_DHT11 //: MP_Baro
{
  public:
	 MP_DHT11(int pin);
	 double getHumidity()  ;
	 double getTemp()  ;
	 void init() ;

  private:
	 DHT sensor;
	 uint8_t pin;

};

#endif
