
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


double MP_LSM303::getAccelX()
{
	lsm.read();
	return lsm.accelData.x ;	
}

double MP_LSM303::getAccelY()
{
	lsm.read();
	return lsm.accelData.y ;	
}

double MP_LSM303::getAccelZ()
{
	lsm.read();
	return lsm.accelData.z ;	
}


int MP_LSM303::compass(char opt[]) 
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

	if (((heading > 0.0f && heading < 0.0f + ERROR )|| (heading > 360.0f - ERROR && heading < 360.0f ) ) && !strcmp(opt, "N"))
		return 1;
	else if (heading > 45.0f - ERROR && heading < 45.0f + ERROR && !strcmp(opt, "NE"))
		return 1;
	else if (heading > 90.0f - ERROR && heading < 90.0f + ERROR && !strcmp(opt, "E"))
		return 1;
	else if (heading > 135.0f - ERROR && heading < 135.0f + ERROR && !strcmp(opt, "SE"))
		return 1;
	else if (heading > 180.0f - ERROR && heading < 180.0f + ERROR && !strcmp(opt, "S"))
		return 1;
	else if (heading > 225.0f - ERROR && heading < 225.0f + ERROR && !strcmp(opt, "SW"))
		return 1;
	else if (heading > 270.0f - ERROR && heading < 270.0f + ERROR && !strcmp(opt, "W"))
		return 1;
	else if (heading > 315.0f - ERROR && heading < 315.0f + ERROR && !strcmp(opt, "NW"))
		return 1;
	else
		return 0;

}

double MP_LSM303::getMagX() 
{
	lsm.read();
	return lsm.magData.x ;	
}

double MP_LSM303::getMagY() 
{
	lsm.read();
	return lsm.magData.y ;	
}
double MP_LSM303::getMagZ() 
{
	lsm.read();
	return lsm.magData.z ;	
}