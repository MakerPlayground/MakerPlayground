#include "MP_TMP007.h"



void MP_TMP007::init() 
{

	
	if (!tmp007.begin()) 
	{
		Serial.println("Could not find a valid TMP007 sensor, check wiring!");
		while (1);
	}

}


double MP_TMP007::getTemperature() 
{
	return tmp007.readObjTempC() ;
}


