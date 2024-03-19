#ifndef MP_TM1637_DISPLAY_H
#define MP_TM1637_DISPLAY_H

#include "MP_DEVICE.h"
#include "TM1637Display.h"

class MP_TM1637_DISPLAY
{
public:
	MP_TM1637_DISPLAY(uint8_t clk, uint8_t dio);
	int init();
	void update(unsigned long current_time);
	void printStatus();

	void showTwoIntWithColon(int8_t beforeColon, int8_t afterColon);
	void showData(double value);
	void showText(char str[]);
	void setBrightness(char c[]);
	void off();

private:
	TM1637Display display;
	double data;
	uint8_t brightness;
};
#endif
