#ifndef MP_PAM8302A_H
#define MP_PAM8302A_H

//#include "MP_Amp.h"
/*
#include <SD.h>   
#include <TMRpcm.h>    
#include <SPI.h>
*/
#include <Arduino.h>

class MP_PAM8302A //: MP_Amp
{
  public:
  	// MP_PAM8302A(uint8_t speakerPin) ;
	MP_PAM8302A(uint8_t speakerPin) ;
	 
	~MP_PAM8302A() {};

	 /*void play(char file[], float vol)   ;
	 void volUp()   ;
	 void volDown()   ;
	 void setVol(float vol)   ;
	 void stop()   ; */

	 void beep(float percentage, uint16_t dur)   ;
	 void tone(uint16_t hz, uint16_t dur)   ;
	 void init() ;

private:
	//uint8_t volLevel;
	//TMRpcm tmrpcm;
	uint8_t speakerPin;
	//uint8_t SDPin ;
};

#endif