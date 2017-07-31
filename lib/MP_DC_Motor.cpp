

#include "MP_DC_Motor.h"

 uint8_t direction; 

MP_DC_Motor::MP_DC_Motor()
	
{
	myMotor = AFMS.getMotor(1);
	
}


void MP_DC_Motor::init() {
	
	AFMS.begin();
	direction = 1;

}

void MP_DC_Motor::on(char dir[], uint8_t speed)   
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
void MP_DC_Motor::reverse() {
	
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
void MP_DC_Motor::set_speed(uint8_t speed)  
{
	myMotor->setSpeed(speed);

}
void MP_DC_Motor::off() {
	myMotor->setSpeed(0);
}

