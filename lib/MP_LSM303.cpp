
#include "MP_LSM303.h"
#define ERROR 10.0f

void MP_LSM303::init() 
{
#ifndef ESP8266
  while (!Serial);     // will pause Zero, Leonardo, etc until serial console opens
#endif

  Serial.println("Magnetometer Test"); Serial.println("");


  /* Initialise the sensor */
  if(!lsm.begin())
  {
    /* There was a problem detecting the LSM303 ... check your connections */
    Serial.println("Could not find a valid LSM303 sensor, check wiring!");
    while(1);

}
}


double MP_LSM303::getAccel_X()
{
	lsm.read();
	return lsm.accelData.x/100.0 ;	
}

double MP_LSM303::getAccel_Y()
{
	lsm.read();
	return lsm.accelData.y/100.0 ;	
}

double MP_LSM303::getAccel_Z()
{
	lsm.read();
	return lsm.accelData.z/100.0 ;	
}


int MP_LSM303::checkDirection(char opt[]) 
{
	
	
	lsm.read();

	float Pi = 3.14159;

	// Calculate the angle of the vector y,x
	float heading = (atan2(lsm.magData.y, lsm.magData.x) * 180) / Pi;
    
	// Normalize to 0-360
	if (heading < 0)
	{
		heading = 360 + heading;
	}

	if (((heading > 0.0f && heading < 0.0f + ERROR )|| (heading > 360.0f - ERROR && heading < 360.0f ) ) && !strcmp(opt, "North"))
		return 1;
	else if (heading > 45.0f - ERROR && heading < 45.0f + ERROR && !strcmp(opt, "NorthEast"))
		return 1;
	else if (heading > 90.0f - ERROR && heading < 90.0f + ERROR && !strcmp(opt, "East"))
		return 1;
	else if (heading > 135.0f - ERROR && heading < 135.0f + ERROR && !strcmp(opt, "SouthEast"))
		return 1;
	else if (heading > 180.0f - ERROR && heading < 180.0f + ERROR && !strcmp(opt, "South"))
		return 1;
	else if (heading > 225.0f - ERROR && heading < 225.0f + ERROR && !strcmp(opt, "SouthWest"))
		return 1;
	else if (heading > 270.0f - ERROR && heading < 270.0f + ERROR && !strcmp(opt, "West"))
		return 1;
	else if (heading > 315.0f - ERROR && heading < 315.0f + ERROR && !strcmp(opt, "NorthWest"))
		return 1;
	else
		return 0;

}

double MP_LSM303::getMag_X() 
{
	lsm.read();
	return lsm.magData.x/10.0 ;	
}

double MP_LSM303::getMag_Y() 
{
	lsm.read();
	return lsm.magData.y/10.0 ;	
}
double MP_LSM303::getMag_Z() 
{
	lsm.read();
	return lsm.magData.z/10.0 ;	
}