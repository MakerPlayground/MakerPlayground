#include "MP_TCS34725.h"



 MP_TCS34725::MP_TCS34725()
{
  tcs = Adafruit_TCS34725(TCS34725_INTEGRATIONTIME_50MS, TCS34725_GAIN_4X);
}

void MP_TCS34725::init() 
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


int MP_TCS34725::isColor(char color[]) 
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



