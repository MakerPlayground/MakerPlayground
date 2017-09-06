#include "MP_Color.h"




void MP_Color::init() 
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

int MP_Color::isColor(char color[]) 

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

 MP_Color::MP_Color()
{
 }

/*
 MP_Color::MP_Color()
{
  tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);
}

void MP_Color::init() 
{
  Serial.println("Color View Test!");

  if (tcs.begin()) {
    Serial.println("Found sensor");
  } else {
    Serial.println("No TCS34725 found ... check your connections");
    while (1); // halt!
  }
}

float MAX (float r, float g, float b)
{
  float temp = r;
  if (g > temp)
    temp = g;
  if (b > temp)
    temp = b;
  return temp;
}

float MIN (float r, float g, float b)
{
  float temp = r;
  if (g < temp)
    temp = g;
  if (b < temp)
    temp = b;
  return temp;
}

void RGBtoHSV( float r, float g, float b, float *h, float *s, float *v )
{
  float min, max, delta;
  min = MIN( r, g, b );
  max = MAX( r, g, b );
  *v = max;       // v
  delta = max - min;

  if( max != 0 )
    *s = delta / max;   // s
  else {
    // r = g = b = 0    // s = 0, v is undefined
    *s = 0;
    *h = -1;
    return;
  }
  if( r == max )
    *h = ( g - b ) / delta;   // between yellow & magenta
  else if( g == max )
    *h = 2 + ( b - r ) / delta; // between cyan & yellow
  else
    *h = 4 + ( r - g ) / delta; // between magenta & cyan
  *h *= 60;       // degrees
  if( *h < 0 )
    *h += 360;
}


int MP_Color::isColor(char color[]) 
{
 uint16_t clear, red, green, blue;

  tcs.setInterrupt(false);      // turn on LED

  delay(60);  // takes 50ms to read 
  
  tcs.getRawData(&red, &green, &blue, &clear);

  tcs.setInterrupt(true);  // turn off LED
  
  Serial.print("C:\t"); Serial.print(clear);
  Serial.print("\tR:\t"); Serial.print(red);
  Serial.print("\tG:\t"); Serial.print(green);
  Serial.print("\tB:\t"); Serial.print(blue);

  // Figure out some basic hex code for visualization
  uint32_t sum = clear;
  float r, g, b;
  r = red; r /= sum;
  g = green; g /= sum;
  b = blue; b /= sum;
 // r *= 256; g *= 256; b *= 256;
  // Serial.print("\t");
  // Serial.print((int)r, HEX); Serial.print((int)g, HEX); Serial.print((int)b, HEX);
  // Serial.println();
  
  float h,s,v ;
  RGBtoHSV(r,g,b,&h,&s,&v);
  Serial.print("\t");
  Serial.print(h); Serial.print("\t"); Serial.print(s*100);  Serial.print("\t"); Serial.print(v*100);
  Serial.println();

  if (strcmp(color, "Red") == 0) {
      return (h < 60 || h > 300);
  } else if (strcmp(color, "Green") == 0) {
      return (h < 180 && h > 60);
  } else if (strcmp(color, "Blue") == 0) {
      return (h < 320 && h > 180);
  } else {
    return 0;
  }
}


*/
