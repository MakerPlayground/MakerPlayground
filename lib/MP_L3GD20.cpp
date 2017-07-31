
#include "MP_L3GD20.h"
#define PI 3.14159265359 


void MP_L3GD20::init() 
{

	
  Serial.begin(9600);
  
  /* Enable auto-ranging */
  gyro.enableAutoRange(true);
 
  /* Initialise the sensor */
  if (!gyro.begin())
  {
    /* There was a problem detecting the L3GD20 ... check your connections */
    Serial.println("Could not find a valid L3GD20 sensor, check wiring!");
    while (1);
  }
}

double MP_L3GD20::getRotateX() 
{
	
	gyro.getEvent(&event);
	return event.gyro.x * PI ;	
}

double MP_L3GD20::getRotateY() 
{
	
	gyro.getEvent(&event);
	return event.gyro.y * PI ;	
}

double MP_L3GD20::getRotateZ() 
{
	
	gyro.getEvent(&event);
	return event.gyro.z * PI ;	
}
