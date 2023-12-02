# Night Light V2 (ALLI/O)

An automatic night light system with a push button to toggle between three modes of operation: off, auto (based on ambient light), and on.

## ALLI/O Diagram

View/Download full diagram image in [png](diagram/night_light_v2.png) or [svg](diagram/night_light_v2.svg).

<img src="diagram/night_light_v2.png">

## Circuit Connection Diagram

The following table shows some possible devices combination (MCU, LED and light sensor) that can be programmed using the diagram above.

| List of Devices  | Connection Type | Screenshot |
| ---------------- | --------- |------------- |
| - Arduino UNO R3<br>- LED 5mm<br>- Rohm BH1750 Ambient Light Sensor (GY-302) | - Digital<br>- I2C | <img src="circuit/config1.png" width="30%"><br>([View full image](circuit/config1.png))  |
| - Arduino Nano<br>- LED 5mm<br>- Photoresistor (LDR) 5mm | - Digital<br>- Analog | <img src="circuit/config2.png" width="30%"><br>([View full image](circuit/config2.png))  |
| - Seeeduino Lotus v1.1<br>- Grove LED<br>- Grove Digital Light Sensor (TSL2561) | - Digital<br>- I2C | <img src="circuit/config3.png" width="30%"><br>([View full image](circuit/config3.png))  |