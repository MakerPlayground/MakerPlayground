#include "MakerPlayground.h"
#include "MP_BUTTON_AL.h"
#include "MP_TM1637_DISPLAY.h"

unsigned long Begin1_recentBlockFinishTime = 0;
unsigned long Begin2_recentBlockFinishTime = 0;
void (*current_Begin1)(void);
void (*current_Begin2)(void);

MP_BUTTON_AL _Btn_R(2);
MP_BUTTON_AL _Btn_B(3);
MP_BUTTON_AL _Btn_Reset(3);
MP_TM1637_DISPLAY _7Seg_R(A0, A1);
MP_TM1637_DISPLAY _7Seg_B(A2, A3);
uint8_t Score_R = 0;
uint8_t Score_B = 0;

void command_reset_r();
void command_reset_r_options();
void command_increment_score_r();
void command_increment_score_r_options();
void command_reset_b();
void command_reset_b_options();
void command_increment_score_b();
void command_increment_score_b_options();

void setup() {
    _Btn_R.init();
    _Btn_B.init();
    _Btn_Reset.init();
    _7Seg_R.init();
    _7Seg_B.init();

    current_Begin1 = command_reset_r;
    current_Begin2 = command_reset_b;
}

void loop() {
    update();
    current_Begin1();
    current_Begin2();
}

void update() {
    currentTime = millis();

    _Btn_R.update(currentTime);
    _Btn_B.update(currentTime);
    _Btn_Reset.update(currentTime);
    _7Seg_R.update(currentTime);
    _7Seg_B.update(currentTime);
}

void command_reset_r() {
    Score_R = 0;
    _7Seg_R.showData(Score_R);
    current_Begin1 = command_reset_r_options;
}

void command_reset_r_options() {
    if (_Btn_R.isPressAndRelease()) {
        current_Begin1 = command_increment_score_r;
    }
}

void command_increment_score_r() {
    Score_R = Score_R + 1;
    _7Seg_R.showData(Score_R);
    current_Begin1 = command_increment_score_r_options;
}

void command_increment_score_r_options() {
    if (_Btn_R.isPressAndRelease()) {
        current_Begin1 = command_increment_score_r;
    } else if (_Btn_Reset.isPressAndRelease()) {
        current_Begin1 = command_reset_r;
    }
}

void command_reset_b() {
    Score_B = 0;
    _7Seg_B.showData(Score_B);
    current_Begin2 = command_reset_b_options;
}

void command_reset_b_options() {
    if (_Btn_B.isPressAndRelease()) {
        current_Begin2 = command_increment_score_b;
    }
}

void command_increment_score_b() {
    Score_B = Score_B + 1;
    _7Seg_B.showData(Score_B);
    current_Begin2 = command_increment_score_b_options;
}

void command_increment_score_b_options() {
    if (_Btn_B.isPressAndRelease()) {
        current_Begin2 = command_increment_score_b;
    } else if (_Btn_Reset.isPressAndRelease()) {
        current_Begin2 = command_reset_b;
    }
}