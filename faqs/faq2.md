## How to implement efficient non-blocking device shim?

**Minimum Example**

All device shims are a class that include at least three methods: init, update, and printStatus. Followed by method for all Actions, Conditions and Values supported by the device. In all cases, sensor values should be read in the update method and cached in an instance variable. The get...() method can be called many times during executing the Command or Transition block. Therefore, it should return a value cache in an instance variable to avoid blocking the execution thread.

```C++
int MP_SOMEDEVICE::init() {
    // any necessary initialize code
    return MP_ERR_OK;
}

void MP_SOMEDEVICE::update(unsigned long current_time) {
    this->someValue = device.read();
}

void MP_SOMEDEVICE::printStatus() { /* ... */ }

float MP_SOMEDEVICE::getSomeValue() { return this->someValue; }
```

**Control acquisition rate**

The update method receives the current time as a parameter, which can be used to limit the sensor reading rate. The following code shows how to read a sensor approximately every 50 ms.

```C++
void MP_SOMEDEVICE::update(unsigned long current_time) {
    if (current_time - last_update >= 50) {
        this->someValue = device.read();
        last_update = current_time;
    }
}
```

**Handle device with long wait time**

Long-running tasks and polling wait loops, as shown in the following code, should be avoided to prevent blocking the MCU from performing other tasks.

```C++
// DO NOT DO THIS!!!
void MP_SOMEDEVICE::update(unsigned long current_time) {
    device.begin_read();
    delay(50);
    this->someValue = device.getValue();
}
```

```C++
// DO NOT DO THIS!!!
void MP_SOMEDEVICE::update(unsigned long current_time) {
    while (!device.is_ready());
    this->someValue = device.getValue();
}
```

Instead, split the task to be executed across multiple calls to the update method and use state machine or flag variables as necessary, as shown in the following examples.

```C++
void MP_SOMEDEVICE::update(unsigned long current_time) {
    if (!hasBeginRead) { 
        device.begin_read();
        start_wait_time = current_time;
        hasBeginRead = true;
    } else if (hasBeginRead && (start_wait_time - current_time > 50)) {
        this->someValue = device.getValue();
        hasBeginRead = false;
    }
}
```

```C++
void MP_SOMEDEVICE::update(unsigned long current_time) {
    if (device.is_ready()) {
        this->someValue = device.getValue();
    }
}
```

For more example, please see our shim for a [push button](https://anonymous.4open.science/r/ALLIO_Libary/lib/arduino/MP_BUTTON_AL/MP_BUTTON_AL.cpp) which implements 30 ms debounce logic in software with a state machine to avoid blocking the MCU.
