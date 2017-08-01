#include "MP_Delay.h"



void MP_Delay::init() 
{

}
int MP_Delay::delayFn(char a[],uint16_t time, char b[]) 
{
	delay(time);
	return 1;
}
