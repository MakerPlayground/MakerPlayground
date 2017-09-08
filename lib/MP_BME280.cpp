
#include "MP_BME280.h"



void MP_BME280::init()  
{	
	if (!bme.begin()) {
		Serial.println("Could not find a valid BME280 sensor, check wiring!");
		while (1);
	}

}

double MP_BME280::getPressure() {	
	return bme.readPressure() / 100.0 ;	
}

double MP_BME280::getAltitude() {
	return bme.readAltitude(SEALEVELPRESSURE_HPA) ;
}

double MP_BME280::getHumidity() {
	return bme.readHumidity() ;
}

double MP_BME280::getTemperature() {
	return bme.readTemperature() ;
}



