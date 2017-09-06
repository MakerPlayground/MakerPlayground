
#include "MP_TCS3200.h"

void MP_TCS3200::init() 
{
 int outputEnabled = 2; // write LOW to turn on Note, may not be hooked up.
 int s0 = 3; // sensor pins
 int s1 = 4;
 int s2 = 5;
 int s3 = 6;
 int nLED = 7; // illuminating LED
 int out = 8; // TCS230 output

pinMode(outputEnabled, OUTPUT);
pinMode(s0, OUTPUT);
pinMode(s1, OUTPUT);
pinMode(s2, OUTPUT);
pinMode(s3, OUTPUT);
pinMode(nLED, OUTPUT);
pinMode(out, INPUT);
//This pin may be set to ground and not available on the breakout
//If not available don't worry about it.
digitalWrite(outputEnabled, LOW);
//Set Frequency scaling to largest value
digitalWrite(s0, HIGH);
digitalWrite(s1, HIGH);
digitalWrite(nLED, LOW);
}

int MP_TCS3200::isColor(char color[]) 

{
   int outputEnabled = 2; // write LOW to turn on Note, may not be hooked up.
 int s0 = 3; // sensor pins
 int s1 = 4;
 int s2 = 5;
 int s3 = 6;
 int nLED = 7; // illuminating LED
 int out = 8; // TCS230 output
  int red = 0;
int green = 0;
int blue = 0;
digitalWrite(nLED,1);
digitalWrite(s2, LOW);
digitalWrite(s3, LOW);
// count OUT, pRed, RED
red = pulseIn(out, digitalRead(out) == HIGH ? LOW : HIGH);
digitalWrite(s3, HIGH);
//count OUT, pBLUE, BLUE
blue = pulseIn(out, digitalRead(out) == HIGH ? LOW : HIGH);
digitalWrite(s2, HIGH);
// count OUT, pGreen, GREEN
green = pulseIn(out, digitalRead(out) == HIGH ? LOW : HIGH);
digitalWrite(nLED,0);
Serial.print("R");
Serial.print(red, DEC);
Serial.print(" G");
Serial.print(green, DEC);
Serial.print(" B");
Serial.print(blue, DEC);
Serial.println();
//Simple logic to test for color
if (red < blue && red < green) {
  if(strcmp(color, "Red") == 0)
    return 1;
  else
    return 0;
}
else if (blue < red && blue < green) 
  {
  if(strcmp(color, "Blue") == 0)
    return 1;
  else
    return 0;
}
else 
  {
  if(strcmp(color, "Green") == 0)
    return 1;
  else
    return 0;
}




}

 MP_TCS3200::MP_TCS3200()
{
 }