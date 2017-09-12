
#include "MP_TCS3200.h"


 MP_TCS3200::MP_TCS3200(uint8_t s0,uint8_t s1,uint8_t s2,uint8_t s3,uint8_t out,uint8_t LED)
 :s0(s0),s1(s1),s2(s2),s3(s3),out(out),LED(LED)
{
 
}

int rgb2hsv(int r,int g,int b,double out[])
{
 

    double min, max, delta;

    min = r < g ? r : g;
    min = min  < b ? min  : b;

    max = r > g ? r : g;
    max = max  > b ? max  : b;

    out[2] = max;                                // v
    delta = max - min;



    if (delta < 0.00001)
    {
        out[1] = 0;
        out[0] = 0; // undefined, maybe nan?


        return 0;
    }
    if( max > 0.0 ) { // NOTE: if Max is == 0, this divide would cause a crash
        out[1] = (delta / max);                  // s
    } else {
        // if max is 0, then r = g = b = 0
        // s = 0, h is undefined
        out[1] = 0.0;
        out[0] = NAN ;                            // its now undefined
        return 0;
    }
    if( r >= max )                           // > is bogus, just keeps compilor happy
        out[0] = ( g - b ) / delta;        // between yellow & magenta
    else
    if( g >= max )
        out[0] = 2.0 + ( b - r ) / delta;  // between cyan & yellow
    else
        out[0] = 4.0 + ( r - g ) / delta;  // between magenta & cyan


    out[0] *= 60.0;                              // degrees

    if( out[0] < 0.0 )
        out[0] += 360.0;

   
    return 0;
}


void MP_TCS3200::init() 
{


pinMode(s0, OUTPUT);
pinMode(s1, OUTPUT);
pinMode(s2, OUTPUT);
pinMode(s3, OUTPUT);
pinMode(LED, OUTPUT);
pinMode(out, INPUT);
//This pin may be set to ground and not available on the breakout
//If not available don't worry about it.

//Set Frequency scaling to largest value
digitalWrite(s0, HIGH);
digitalWrite(s1, HIGH);
digitalWrite(LED, LOW);
}

int MP_TCS3200::isColor(char color[]) 

{
  delay(100);

  int red = 0;
int green = 0;
int blue = 0;
digitalWrite(LED,1);
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
digitalWrite(LED,0);
Serial.print("R");
Serial.print(255-red, DEC);
Serial.print(" G");
Serial.print(255-green, DEC);
Serial.print(" B");
Serial.print(255-blue, DEC);
Serial.println();

double hsv[3] ;
rgb2hsv(255-red,255-green,255-blue,hsv);
hsv[1]*=100;
hsv[2]*=100;
hsv[2]/=255;

   Serial.println(hsv[0]) ;
Serial.println(hsv[1]) ;
      Serial.println(hsv[2]) ;
Serial.println("-----"+(hsv[0]>=270 && hsv[0]<330)&&(strcmp(color, "Magenta") == 0)) ;

 if(hsv[2]>97&&hsv[1]<5&&strcmp(color, "White") == 0)
{
   Serial.println("White") ;
  return 1;
}

 if((hsv[0]>=330 || hsv[0]<15)&&strcmp(color, "Red") == 0)
{
   Serial.println("Red") ;
  return 1;
}
 if((hsv[0]>=15 && hsv[0]<25)&&strcmp(color, "Orange") == 0)
{
   Serial.println("Orange") ;
  return 1;
}
 if((hsv[0]>=25 && hsv[0]<75)&&strcmp(color, "Yellow") == 0) //30
{
   Serial.println("Yellow") ;
  return 1;
}
 if((hsv[0]>=75 && hsv[0]<165)&&strcmp(color, "Green") == 0)//135
{
   Serial.println("Green") ;
  return 1;
}
 if((hsv[0]>=165 && hsv[0]<220)&&strcmp(color, "Cyan") == 0) //210
{
   Serial.println("Cyan") ;
  return 1;
}
 if((hsv[0]>=220 && hsv[0]<235)&&strcmp(color, "Blue") == 0) //225
{
   Serial.println("Blue") ;
  return 1;
}
 if((hsv[0]>=235 && hsv[0]<270)&&strcmp(color, "Violet") == 0)
{
   Serial.println("Violet") ;
  return 1;
}
 if((hsv[0]>=270 && hsv[0]<330)&&strcmp(color, "Magenta") == 0)
{
   Serial.println("Margenta") ;
  return 1;
}
Serial.println("mdkfmiwmgirwmg") ;
return 0;





}



