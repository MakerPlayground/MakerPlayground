#ifndef MP_ORIENTATION_H
#define MP_ORIENTATION_H

//#include "MP_Led.h"
#include <Arduino.h>
#include <Wire.h>
#include "MPU6050.h"

class MP_Orientation //: MP_Led
{
  public:
	MP_Orientation();
	~MP_Orientation() {};

    void init();
    double getRoll();
    double getPitch();
    double getYaw();

  private:
    MPU6050 mpu;
};

#endif