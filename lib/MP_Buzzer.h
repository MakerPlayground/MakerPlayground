#ifndef MP_BUZZER_H
#define MP_BUZZER_H

#include <Arduino.h>

class MP_Buzzer 
{
  public:
	MP_Buzzer(uint8_t pin);
	~MP_Buzzer() {};

	void init();
	void beep(double frequency, double duration);
  
  private:
	uint8_t pin;
};

#endif