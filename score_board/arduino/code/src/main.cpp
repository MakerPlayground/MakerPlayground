#include <Arduino.h>
#include "MP_BUTTON_AL.h"
#include "MP_TM1637_DISPLAY.h"

MP_BUTTON_AL _Btn_R(2);
MP_BUTTON_AL _Btn_B(3);
MP_BUTTON_AL _Btn_Reset(4);

MP_TM1637_DISPLAY _7Seg_R(A0, A1);
MP_TM1637_DISPLAY _7Seg_B(A2, A3);

int Score_R, Score_B;

void setup() {
    _Btn_R.init();
    _Btn_B.init();
    _Btn_Reset.init();
    _7Seg_R.init();
    _7Seg_B.init();
}

void loop() {
    Score_R = 0;
    Score_B = 0;
    _7Seg_R.showData(Score_R);
    _7Seg_B.showData(Score_B);

    while (!_Btn_Reset.isPressAndRelease()) {
        _Btn_R.update(millis());
        _Btn_B.update(millis());
        _Btn_Reset.update(millis());
        
        if (_Btn_R.isPressAndRelease()) {
            Score_R++;
            _7Seg_R.showData(Score_R);
        }
        if (_Btn_B.isPressAndRelease()) {
            Score_B++;
            _7Seg_B.showData(Score_B);
        }
    }
}