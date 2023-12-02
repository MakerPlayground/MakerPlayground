#include "MP_INTERACTIVE.h"

bool MP_INTERACTIVE::isFreezeSensor() {
    return freezeSensor;
}

void MP_INTERACTIVE::setFreezeSensor(bool isFreeze) {
    freezeSensor = isFreeze;
}

int MP_INTERACTIVE::getSensorRate() {
    return sensorRate;
}

void MP_INTERACTIVE::setSensorRate(int rate) {
    sensorRate = rate;
}

MP_INTERACTIVE MPInteractive;