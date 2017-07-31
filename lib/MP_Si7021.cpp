#include "MP_Si7021.h"



void MP_Si7021::init() 
{
	sensor = Adafruit_Si7021();

	if (!sensor.begin()) {
		Serial.println("Could not find a valid Si7021 sensor, check wiring!");
		while (1);
	}

}

double MP_Si7021::getTemp() 
{
	return sensor.readTemperature() ;
}


double MP_Si7021::getHumidity() 
{
	return sensor.readHumidity() ;
}
