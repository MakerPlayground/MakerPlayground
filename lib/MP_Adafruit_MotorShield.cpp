

#include "MP_Adafruit_MotorShield.h"

 uint8_t direction; 

MP_Adafruit_MotorShield::MP_Adafruit_MotorShield()
	
{
	myMotor = AFMS.getMotor(1);
	
}


void MP_Adafruit_MotorShield::init() {
	
	AFMS.begin();
	direction = 1;

}

void MP_Adafruit_MotorShield::on(char dir[], uint8_t speed)   
{
	

	if(!strcmp(dir,"CW"))
		direction = 1 ;
	else if(!strcmp(dir,"CW"))
		direction = 2 ;

	myMotor->setSpeed(speed);	
	if (direction == 1)
	{
		myMotor->run(FORWARD);		
	}
	else if (direction == 2)
	{
		myMotor->run(BACKWARD);		
	}
}
void MP_Adafruit_MotorShield::reverse() {
	
	if (direction == 1)
	{
		direction = 2;
		myMotor->run(BACKWARD);
	}
	else if (direction == 2)
	{
		direction = 1;
		myMotor->run(FORWARD);
	}
	
}
void MP_Adafruit_MotorShield::set_speed(uint8_t speed)  
{
	myMotor->setSpeed(speed);

}
void MP_Adafruit_MotorShield::off() {
	myMotor->setSpeed(0);
}

