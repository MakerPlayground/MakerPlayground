#include "MP_AdafruitIO.h"
#if (!defined(ARDUINO_SAMD_MKR1000) && defined(ARDUINO_ARCH_SAMD)) || defined(ARDUINO_SAMD_MKR1000) || defined(ESP8266) || defined(ARDUINO_STM32_FEATHER)

 int state = 0;
void handleMessage(AdafruitIO_Data *data) {
	pinMode(0, OUTPUT);
   Serial.print("received <- ");

  if(data->toPinLevel() == HIGH)
    Serial.println("HIGH");
  else
    Serial.println("LOW");

  // write the current state to the led
  digitalWrite(0, (data->toPinLevel()));
	/*pinMode(5, OUTPUT);
     // convert the data to integer
  int reading = data->toInt();

  Serial.print("received <- ");
  Serial.println(reading);
  analogWrite(5, reading);*/

}

MP_AdafruitIO::MP_AdafruitIO() {

}

void MP_AdafruitIO::setup(char* IO_USERNAME,char* IO_KEY,char* WIFI_SSID, char* WIFI_PASS,char* fedName) 
// :io( AdafruitIO_WiFi (IO_USERNAME, IO_KEY, WIFI_SSID, WIFI_PASS))
{

if(state ==0){
	state = 1 ;
  io = new AdafruitIO_WiFi (IO_USERNAME, IO_KEY, WIFI_SSID, WIFI_PASS);
  name = io->feed(fedName);
  // connect to io->adafruit.com
  IOUSERNAME = IO_USERNAME;
	 IOKEY = IO_KEY;
	  WIFISSID =WIFI_SSID;
	  WIFIPASS=WIFI_PASS;
  Serial.print("Connecting to Adafruit IO");
  io->connect();
  name->onMessage(handleMessage);
  // wait for a connection
  Serial.println(state);
  while(io->status() < AIO_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  // we are connected
  Serial.println();
  Serial.println(io->statusText());

}
}
/*
void MP_AdafruitIO::setFeed(char* feed) 
{
//io = new AdafruitIO_WiFi (IOUSERNAME, IOKEY, WIFISSID, WIFIPASS);
  name = io->feed(feed);
  // connect to io->adafruit.com

  Serial.print("Conzzzzznecting to Adafruit IO");
  io->connect();
  name->onMessage(handleMessag);
  // wait for a connection
  while(io->status() < AIO_CONNECTED) {
    Serial.print(".");
    delay(500);
  }

  // we are connected
  Serial.println();
  Serial.println(io->statusText());


}
*/
void MP_AdafruitIO::intToCloud(int val)

{
  io->run();
  name->save(val);
}

void MP_AdafruitIO::floatToCloud(float val)

{
  io->run();
  name->save(val);
}


void MP_AdafruitIO::analogPercentageToCloud(int val)
{
  io->run();
  name->save(val/1023.0);
}


void MP_AdafruitIO::adaControlLED()
{
	io->run();
}

int MP_AdafruitIO::downtohost()
{
	io->run();
}

#endif