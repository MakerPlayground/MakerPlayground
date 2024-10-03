#ifndef MP_INTERACTIVE_H
#define MP_INTERACTIVE_H

class MP_INTERACTIVE {
public:
    bool isFreezeSensor();
    void setFreezeSensor(bool isFreeze);
    int getSensorRate();
    void setSensorRate(int rate);
private:
    bool freezeSensor = false;
    int sensorRate = 100;
};

extern MP_INTERACTIVE MPInteractive;

#endif //MP_INTERACTIVE_H