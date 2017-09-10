#include "MP_GenericButton.h"


MP_GenericButton::MP_GenericButton(uint8_t pin) 
	:pin(pin)
{

}

void MP_GenericButton::init() 

{
	pinMode(pin, INPUT_PULLUP);
}


int MP_GenericButton::isReleased() 
{
	int returnVal = 0;
	if (digitalRead(pin) == LOW)
		returnVal++;
//	while (digitalRead(pin) == LOW);
	return returnVal;
}

int MP_GenericButton::isPressed() 
{
	int returnVal = 0;
	if (digitalRead(pin) == HIGH)
		returnVal++;
//	while (digitalRead(pin) == HIGH);
	return returnVal;
}


int MP_GenericButton::isMultiPressed(double press) 
{
	int buttonState = 0;
	int reading = 0;
	long timer = 0;
	int count = 0;
	while (1) {
		if (timer < 55000 && count != press ){
	    reading = digitalRead(pin);
		    if (buttonState != reading){
		      if (reading == 1){
		        timer = 0;
		        count++;
		        Serial.println(count);
		      } else{
		        timer = 0;
		        delay(100);  
		      }
		    } else if (reading == 1){
		      timer = 0;
		    }
		    buttonState = reading;
		} else if (count == press){
		    timer = 0;
		    count = 0;
		    Serial.println("Full count");
		    return 1;
		} else{
		    count = 0;
		    timer = 0;
		    Serial.println("Time out");
		    return 0;
		}
		timer++;
	}
	
	return 0;
}
