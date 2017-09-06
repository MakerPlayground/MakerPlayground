#include "MP_Temperature.h"



void MP_Temperater::init() 
{

	
	if (!tmp007.begin()) 
	{
		Serial.println("Could not find a valid TMP007 sensor, check wiring!");
		while (1);
	}

}


double MP_Temperater::getTemp() 
{
	return tmp007.readObjTempC() ;
}


