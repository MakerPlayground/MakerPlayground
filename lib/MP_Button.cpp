#include "MP_Button.h"


MP_Button::MP_Button(uint8_t pin) 
	:pin(pin)
{

}

void MP_Button::init() 

{
	pinMode(pin, INPUT_PULLUP);
}


int MP_Button::isReleased() 
{
	int returnVal = 0;
	if (digitalRead(pin) == LOW)
		returnVal++;
	while (digitalRead(pin) == LOW);
	return returnVal;
}


int MP_Button::doubleRelease() 
{
	int count=0;
	int returnVal = 0;
	if (digitalRead(pin) == LOW)
	{
		while (digitalRead(pin) == LOW);
		while (count < 500)
		{
			count++;
			if (digitalRead(pin) == LOW)
				returnVal++;
		}
		while (digitalRead(pin) == LOW);
		if(returnVal>1)
			return 1;
	}
	return 0;

}
