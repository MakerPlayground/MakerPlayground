#ifndef MP_AMP_H
#define MP_AMP_H

#include <Arduino.h>

class MP_Amp
{
  public:
	//virtual void play(char file[], uint8_t vol) const = 0;
	/*virtual void volUp() const = 0;
	virtual void volDown() const = 0 ;
	virtual void setVol(uint8_t vol) const = 0 ;
	virtual void stop() const = 0;*/
	virtual void beep(uint16_t hz,uint16_t dur) const = 0;
	virtual void init() const =0;

};

#endif