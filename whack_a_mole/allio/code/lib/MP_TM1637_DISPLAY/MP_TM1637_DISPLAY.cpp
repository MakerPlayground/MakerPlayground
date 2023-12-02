#include "MP_TM1637_DISPLAY.h"

// Adapt from https://docs.onion.io/omega2-arduino-dock-starter-kit/arduino-kit-seven-segment-display.html
static const uint8_t alphabetToSegmentMap[] = {
	//DpGFEDCBA  Element  Char   7-segment map:
	0b01110111, //  0  'A'          AAA
	0b01111100, //  1  'b'         F   B
	0b00111001, //  2  'C'         F   B
	0b01011110, //  3  'd'          GGG
	0b01111001, //  4  'E'         E   C
	0b01110001, //  5  'F'         E   C
	0b00111101, //  6  'G'          DDD
	0b01110110, //  7  'H'
	0b00000110, //  8  'I'
	0b00001110, //  9  'J'
	0b01110110, // 10  'K'  Same as 'H'
	0b00111000, // 11  'L'
	0b00000000, // 12  'M'  NO DISPLAY
	0b01010100, // 13  'n'
	0b00111111, // 14  'O'
	0b01110011, // 15  'P'
	0b01100111, // 16  'q'
	0b01010000, // 17  'r'
	0b01101101, // 18  'S'
	0b01111000, // 19  't'
	0b00111110, // 20  'U'
	0b00111110, // 21  'V'  Same as 'U'
	0b00000000, // 22  'W'  NO DISPLAY
	0b01110110, // 23  'X'  Same as 'H'
	0b01101110, // 24  'y'
	0b01011011, // 25  'Z'  Same as '2'
};

MP_TM1637_DISPLAY::MP_TM1637_DISPLAY(uint8_t clk, uint8_t dio)
	:display(TM1637Display(clk, dio))
	,data(0)
	,brightness(7)
{
}

int MP_TM1637_DISPLAY::init()
{
	uint8_t buffer[] = {0x0, 0x0, 0x0, 0x0};
	display.setBrightness(this->brightness, true);
	display.setSegments(buffer);
	return MP_ERR_OK;
}

void MP_TM1637_DISPLAY::update(unsigned long current_time)
{

}

void MP_TM1637_DISPLAY::printStatus()
{
	Serial.print(F("value = "));
	Serial.println(this->data);

	Serial.print(F("brightness = "));
	Serial.println(this->brightness);
}

void MP_TM1637_DISPLAY::showTwoIntWithColon(int8_t beforeColon, int8_t afterColon)
{
	data = beforeColon * 100 + afterColon;
	display.showNumberDec((int) data);
}

void MP_TM1637_DISPLAY::showData(double value)
{
	data = value;
	display.showNumberDec((int) value);
}

void MP_TM1637_DISPLAY::showText(char str[])
{
	uint8_t buffer[] = {0x0, 0x0, 0x0, 0x0};
	for (uint8_t i=0; i<4; i++)
	{
		if (str[i] == '\0') {
			break;
		}
		buffer[i] = alphabetToSegmentMap[str[i] - 'A'];
	}
	display.setSegments(buffer);
}

void MP_TM1637_DISPLAY::setBrightness(char c[])
{
	brightness = (uint8_t) (c[0] - '0');
	display.setBrightness(brightness, true);
}

void MP_TM1637_DISPLAY::off()
{
	uint8_t buffer[] = {0x0, 0x0, 0x0, 0x0};
	display.setSegments(buffer);
}
