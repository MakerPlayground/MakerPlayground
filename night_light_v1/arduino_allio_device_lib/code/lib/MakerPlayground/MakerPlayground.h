#ifndef MAKERPLAYGROUND_H
#define MAKERPLAYGROUND_H

#include <Arduino.h>
#include "MP_ERROR.h"

/*SAMD core*/
#ifdef ARDUINO_SAMD_VARIANT_COMPLIANCE
  #define MPSerial SerialUSB
#else
  #define MPSerial Serial
#endif

#define MP_LOG_INTERVAL 1000

#if defined(MP_DEBUG_ENABLE)
#define PR_DEVICE(device) MPSerial.print("\""); MPSerial.print(device); MPSerial.print("\""); MPSerial.write('\0');
#define PR_INFO(device) MPSerial.print(F("[[I]]")); MPSerial.write('\0'); PR_DEVICE(device);
#define PR_ERR(device) MPSerial.print(F("[[E]]")); MPSerial.write('\0'); PR_DEVICE(device);
#define PR_VAL(device) MPSerial.print(F("[[V]]")); MPSerial.write('\0'); PR_DEVICE(device);
#define PR_END() MPSerial.println('\0')

#define MP_LOG(device, name) PR_INFO(F(name)); device.printStatus(); PR_END(); 
#define MP_LOG_P(device, name) PR_INFO(F(name)); device->printStatus(); PR_END();
#ifdef __AVR__
    #define MP_ERR(name, status_code) PR_ERR(F(name)); MPSerial.println(reinterpret_cast<const __FlashStringHelper *>(pgm_read_word(&(ERRORS[status_code])))); PR_END();
#else
    #define MP_ERR(name, status_code) PR_ERR(F(name)); MPSerial.println(reinterpret_cast<const __FlashStringHelper *>(ERRORS[status_code])); PR_END();
#endif
#else
#define PR_DEVICE(device)
#define PR_INFO(device)
#define PR_ERR(device)
#define PR_VAL(device)
#define PR_END()

#define MP_LOG(device, name)
#define MP_LOG_P(device, name)
#define MP_ERR(name, status_code)
#endif

uint8_t status_code = 0;
unsigned long currentTime = 0;
unsigned long latestLogTime = 0;

void update();

double mapDouble(double x, double in_min, double in_max, double out_min, double out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
}

#endif