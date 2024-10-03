#include <Arduino.h>
#include "MP_BUTTON_AL.h"
#include "MP_SERVO_PCA9685.h"
#include "MP_TM1637_DISPLAY.h"

MP_BUTTON_AL _Button1(2);
MP_BUTTON_AL _Button2(3);
MP_BUTTON_AL _Button3(4);
MP_BUTTON_AL _Button4(5);
MP_BUTTON_AL _Button5(6);
MP_BUTTON_AL _Button6(7);
MP_BUTTON_AL _Button7(8);
MP_BUTTON_AL _Button8(9);
MP_BUTTON_AL _Button9(10);
MP_BUTTON_AL _Button10(11);
MP_BUTTON_AL _StartBtn(12);

MP_TM1637_DISPLAY _display1(A0, A1);
MP_TM1637_DISPLAY _display2(A2, A3);

MP_SERVO_PCA9685 _ServoController1;

unsigned long start_time;

byte player1_score = 0, player1_mole_id;
unsigned long player1_popup_time;

byte player2_score = 0, player2_mole_id;
unsigned long player2_popup_time;

void player1_hide_mole()
{
    for (int i = 0; i < 5; i++)
    {
        _ServoController1.moveServo(i, 0);
    }
}

void player1_popup_mole()
{
    player1_hide_mole();
    player1_mole_id = random(0, 5);
    _ServoController1.moveServo(player1_mole_id, 90);
    player1_popup_time = millis();
}

void player2_hide_mole()
{
    for (int i = 5; i < 10; i++)
    {
        _ServoController1.moveServo(i, 0);
    }
}

void player2_popup_mole()
{
    player2_hide_mole();
    player2_mole_id = random(5, 10);
    _ServoController1.moveServo(player2_mole_id, 90);
    player2_popup_time = millis();
}

void displayScore()
{
    _display1.showData(player1_score);
    _display2.showData(player2_score);
}

void setup()
{
    _Button1.init();
    _Button2.init();
    _Button3.init();
    _Button4.init();
    _Button5.init();
    _Button6.init();
    _Button7.init();
    _Button8.init();
    _Button9.init();
    _Button10.init();
    _StartBtn.init();
    _display1.init();
    _display2.init();
    _ServoController1.init();

    displayScore();
    for (int i = 0; i < 10; i++)
    {
        _ServoController1.moveServo(i, 0);
    }
}

void update() {
    unsigned long currentTime = millis();

    _Button1.update(currentTime);
    _Button2.update(currentTime);
    _Button3.update(currentTime);
    _Button4.update(currentTime);
    _Button5.update(currentTime);
    _Button6.update(currentTime);
    _Button7.update(currentTime);
    _Button8.update(currentTime);
    _Button9.update(currentTime);
    _Button10.update(currentTime);
    _StartBtn.update(currentTime);
    _display1.update(currentTime);
    _display2.update(currentTime);
    _ServoController1.update(currentTime);
}

void loop()
{
    player1_score = 0;
    player2_score = 0;
    while (_StartBtn.isNotPress()) {
        update();
    }
    displayScore();
    start_time = millis();

    player1_popup_mole();
    player2_popup_mole();
    while (millis() - start_time < 60000)
    {
        update();
        if (millis() - player1_popup_time < 1000)
        {
            if (player1_mole_id == 0 && _Button1.isPress()) {
                player1_score += 1;
                player1_popup_mole();
            } else if (player1_mole_id == 1 && _Button2.isPress()) {
                player1_score += 1;
                player1_popup_mole();
            } else if (player1_mole_id == 2 && _Button3.isPress()){
                player1_score += 1;
                player1_popup_mole();
            } else if (player1_mole_id == 3 && _Button4.isPress()){
                player1_score += 1;
                player1_popup_mole();
            } else if (player1_mole_id == 4 && _Button5.isPress()){
                player1_score += 1;
                player1_popup_mole();
            }
        }
        else
        {
            player1_popup_mole();
        }
        
        if (millis() - player2_popup_time < 1000)
        {
            if (player2_mole_id == 0 && _Button6.isPress()) {
                player2_score += 1;
                player2_popup_mole();
            } else if (player2_mole_id == 1 && _Button7.isPress()) {
                player2_score += 1;
                player2_popup_mole();
            } else if (player2_mole_id == 2 && _Button8.isPress()){
                player2_score += 1;
                player2_popup_mole();
            } else if (player2_mole_id == 3 && _Button9.isPress()){
                player2_score += 1;
                player2_popup_mole();
            } else if (player2_mole_id == 4 && _Button10.isPress()){
                player2_score += 1;
                player2_popup_mole();
            }
        }
        else
        {
            player2_popup_mole();
        }

        displayScore();
    }
    
    player1_hide_mole();
    player2_hide_mole();
    if (player1_score > player2_score) {
        _display1.showText("WIN");
        _display2.off();
    } else if (player2_score > player1_score) {
        _display1.off();
        _display2.showText("WIN");
    } else {
        _display1.showText("DRAW");
        _display2.showText("DRAW");
    }
}