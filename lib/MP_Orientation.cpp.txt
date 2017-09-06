#include "I2Cdev.h"
#include "MPU6050.h"
#include "Wire.h"

MP_Orientation::MP_Orientation() {
}

void MP_Orientation::init() {
    Wire.begin();
    mpu.initialize();
    Serial.println(mpu.testConnection() ? "Connected" : "Connection failed");
}

double MP_Orientation::getRoll() {
    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
    valx = map(ax, -17000, 17000, 0, 179);
    valy = map(ay, -17000, 17000, 0, 179);
    valz = map(az, -17000, 17000, 0, 179);
    Serial.print("axis x = ") ; 
    Serial.print(valx) ; 
    Serial.print(" axis y = ") ; 
    Serial.print(valy) ; 
    Serial.print(" axis z = ") ; 
    Serial.println(valz) ; 
    return valx;
}

double MP_Orientation::getPitch(){
    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
    valx = map(ax, -17000, 17000, 0, 179);
    valy = map(ay, -17000, 17000, 0, 179);
    valz = map(az, -17000, 17000, 0, 179);
    return valy;
}

double MP_Orientation::getYaw() {
    mpu.getMotion6(&ax, &ay, &az, &gx, &gy, &gz);
    valx = map(ax, -17000, 17000, 0, 179);
    valy = map(ay, -17000, 17000, 0, 179);
    valz = map(az, -17000, 17000, 0, 179);
    return valz;
}