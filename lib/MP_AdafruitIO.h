#ifndef MP_ADAFRUITIO_H
#define MP_ADAFRUITIO_H


#if (!defined(ARDUINO_SAMD_MKR1000) && defined(ARDUINO_ARCH_SAMD)) || defined(ARDUINO_SAMD_MKR1000) || defined(ESP8266) || defined(ARDUINO_STM32_FEATHER)


#include <Arduino.h>
#include <AdafruitIO_WiFi.h>



class MP_AdafruitIO //: MP_Button
{
  public:
	  MP_AdafruitIO() ;
	 //~MP_AdafruitIO() {};

	  void intToCloud(int val)  ;
	  void floatToCloud(float val)  ;
	  void analogPercentageToCloud(int val)  ;
	 // void digitalFromCloud() 
	//  void setFeed(char* feed) ;
	  void adaControlLED() ;

	  int downtohost();
	  void setup(char* IO_USERNAME,char* IO_KEY,char* WIFI_SSID, char* WIFI_PASS,char* feedName) ;
	
   private:
	  AdafruitIO_WiFi* io;
	  AdafruitIO_Feed* name;
	  char* IOUSERNAME;
	 char* IOKEY;
	  char* WIFISSID;
	  char* WIFIPASS;

	 // char* feedName;


};

#endif
#endif