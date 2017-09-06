#include "MP_DHT11.h"

  // what digital pin we're connected to

// Uncomment whatever type you're using!

 MP_DHT11::MP_DHT11(int pin)
	 :sensor(DHT(pin, 11)), pin(pin)
{
}


void MP_DHT11::init() 
{
	sensor.begin();
}

double MP_DHT11::getTemp() 
{
	return sensor.readTemperature() ;
}


double MP_DHT11::getHumidity() 
{
	return sensor.readHumidity() ;
}
