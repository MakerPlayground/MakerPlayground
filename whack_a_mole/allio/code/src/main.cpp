#include "MakerPlayground.h"
#include "MP_BUTTON_AL.h"
#include "MP_SERVO_PCA9685.h"
#include "MP_TM1637_DISPLAY.h"

unsigned long Begin1_recentBlockFinishTime_1 = 0;
unsigned long Begin1_recentBlockFinishTime_2 = 0;
unsigned long Begin2_recentBlockFinishTime = 0;
void (*current_Begin1)(void);
void (*current_Begin2)(void);

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
MP_TM1637_DISPLAY _7Segment1(A0, A1);
MP_TM1637_DISPLAY _7Segment2(A2, A3);
MP_SERVO_PCA9685 _ServoController1;
uint8_t P1Score = 0;
uint8_t P2Score = 0;

void command_hide_all_mole();
void command_hide_all_mole_options();
void command_reset_score();
void command_game_play();
void command_player1_pop_up_mole();
void command_player1_pop_up_mole_begin1_options();
void command_player1_pop_up_mole_1();
void command_player1_pop_up_mole_1_options();
void command_player1_pop_up_mole_2();
void command_player1_pop_up_mole_2_options();
void command_player1_pop_up_mole_3();
void command_player1_pop_up_mole_3_options();
void command_player1_pop_up_mole_4();
void command_player1_pop_up_mole_4_options();
void command_player1_pop_up_mole_5();
void command_player1_pop_up_mole_5_options();
void command_player1_update_score();
void command_player1_pop_up_mole_options();
void command_player1_hide_mole();
void command_game_play_option();
void command_game_play_option2();
void command_player2_pop_up_mole();
void command_player2_pop_up_mole_begin1_options();
void command_player2_pop_up_mole_1();
void command_player2_pop_up_mole_1_options();
void command_player2_pop_up_mole_2();
void command_player2_pop_up_mole_2_options();
void command_player2_pop_up_mole_3();
void command_player2_pop_up_mole_3_options();
void command_player2_pop_up_mole_4();
void command_player2_pop_up_mole_4_options();
void command_player2_pop_up_mole_5();
void command_player2_pop_up_mole_5_options();
void command_player2_update_score();
void command_player2_pop_up_mole_options();
void command_player2_hide_mole();
void command_display_p1_win();
void command_display_p2_win();
void command_display_draw();

void setup() {
    MPSerial.begin(115200);

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
    _7Segment1.init();
    _7Segment2.init();
    _ServoController1.init();

    current_Begin1 = command_hide_all_mole;
    current_Begin2 = nop;
}

void loop() {
    update();
    current_Begin1();
    current_Begin2();
}

void update() {
    currentTime = millis();

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
    _7Segment1.update(currentTime);
    _7Segment2.update(currentTime);
    _ServoController1.update(currentTime);
}

void command_hide_all_mole() {
    _ServoController1.moveServo(0, 0);
    _ServoController1.moveServo(1, 0);
    _ServoController1.moveServo(2, 0);
    _ServoController1.moveServo(3, 0);
    _ServoController1.moveServo(4, 0);
    _ServoController1.moveServo(5, 0);
    _ServoController1.moveServo(6, 0);
    _ServoController1.moveServo(7, 0);
    _ServoController1.moveServo(8, 0);
    _ServoController1.moveServo(9, 0);
    current_Begin1 = command_hide_all_mole_options;
}

void command_hide_all_mole_options() {
    if (_StartBtn.isPress()) {
        current_Begin1 = command_reset_score;
    }
}

void command_reset_score() {
    P1Score = 0;
    P2Score = 0;
    _7Segment1.showData(0);
    _7Segment2.showData(0);
    current_Begin1 = command_game_play;
}

void command_game_play() {
    Begin1_recentBlockFinishTime_1 = millis();
    current_Begin1 = command_player1_pop_up_mole;
    current_Begin2 = command_player2_pop_up_mole;
}

void command_player1_pop_up_mole() {
    Begin1_recentBlockFinishTime_2 = millis();
    current_Begin1 = command_player1_pop_up_mole_begin1_options;
}

void command_player1_pop_up_mole_begin1_options() {
    byte randomValue = random(1, 6);
    if (randomValue == 1) {
        current_Begin1 = command_player1_pop_up_mole_1;
    } else if (randomValue == 2) {
        current_Begin1 = command_player1_pop_up_mole_2;
    } else if (randomValue == 3) {
        current_Begin1 = command_player1_pop_up_mole_3;
    } else if (randomValue == 4) {
        current_Begin1 = command_player1_pop_up_mole_4;
    } else if (randomValue == 5) {
        current_Begin1 = command_player1_pop_up_mole_5;
    }
    command_player1_pop_up_mole_options(); 
}

void command_player1_pop_up_mole_1() {
    _ServoController1.moveServo(0, 90);
    current_Begin1 = command_player1_pop_up_mole_1_options;
}

void command_player1_pop_up_mole_1_options() {
    if (_Button1.isPress()) {
        current_Begin1 = command_player1_update_score;
    }
    command_player1_pop_up_mole_options();
}

void command_player1_pop_up_mole_2() {
    _ServoController1.moveServo(1, 90);
    current_Begin1 = command_player1_pop_up_mole_2_options;
}

void command_player1_pop_up_mole_2_options() {
    if (_Button2.isPress()) {
        current_Begin1 = command_player1_update_score;
    }
    command_player1_pop_up_mole_options();
}

void command_player1_pop_up_mole_3() {
    _ServoController1.moveServo(2, 90);
    current_Begin1 = command_player1_pop_up_mole_3_options;
}

void command_player1_pop_up_mole_3_options() {
    if (_Button3.isPress()) {
        current_Begin1 = command_player1_update_score;
    }
    command_player1_pop_up_mole_options();
}

void command_player1_pop_up_mole_4() {
    _ServoController1.moveServo(3, 90);
    current_Begin1 = command_player1_pop_up_mole_4_options;
}

void command_player1_pop_up_mole_4_options() {
    if (_Button4.isPress()) {
        current_Begin1 = command_player1_update_score;
    }
    command_player1_pop_up_mole_options();
}

void command_player1_pop_up_mole_5() {
    _ServoController1.moveServo(4, 90);
    current_Begin1 = command_player1_pop_up_mole_5_options;
}

void command_player1_pop_up_mole_5_options() {
    if (_Button5.isPress()) {
        current_Begin1 = command_player1_update_score;
    }
    command_player1_pop_up_mole_options();
}

void command_player1_update_score() {
    P1Score += 1;
    _7Segment1.showData(P1Score);
    current_Begin1 = command_player1_pop_up_mole_options;
}

void command_player1_pop_up_mole_options() {
    if (millis() - Begin1_recentBlockFinishTime_2 > 1000) {
        current_Begin1 = command_player1_hide_mole;
    }
    command_game_play_option(); 
}

void command_player1_hide_mole() {
    _ServoController1.moveServo(0, 0);
    _ServoController1.moveServo(1, 0);
    _ServoController1.moveServo(2, 0);
    _ServoController1.moveServo(3, 0);
    _ServoController1.moveServo(4, 0);
    current_Begin1 = command_player1_pop_up_mole;
}

void command_game_play_option() {
    if (millis() - Begin1_recentBlockFinishTime_1 > 60000) {
        current_Begin1 = command_game_play_option2;
        current_Begin2 = nop;
    }
}

void command_game_play_option2() {
    if (P1Score > P2Score) {
        current_Begin1 = command_display_p1_win;
    } else if (P2Score > P1Score) {
        current_Begin1 = command_display_p2_win;
    } else {
        current_Begin1 = command_display_draw;
    }
}

void command_player2_pop_up_mole() {
    Begin2_recentBlockFinishTime = millis();
    current_Begin2 = command_player2_pop_up_mole_begin1_options;
}

void command_player2_pop_up_mole_begin1_options() {
    byte randomValue = random(1, 6);
    if (randomValue == 1) {
        current_Begin2 = command_player2_pop_up_mole_1;
    } else if (randomValue == 2) {
        current_Begin2 = command_player2_pop_up_mole_2;
    } else if (randomValue == 3) {
        current_Begin2 = command_player2_pop_up_mole_3;
    } else if (randomValue == 4) {
        current_Begin2 = command_player2_pop_up_mole_4;
    } else if (randomValue == 5) {
        current_Begin2 = command_player2_pop_up_mole_5;
    } 
    command_player2_pop_up_mole_options();
}

void command_player2_pop_up_mole_1() {
    _ServoController1.moveServo(5, 90);
    current_Begin2 = command_player2_pop_up_mole_1_options;
}

void command_player2_pop_up_mole_1_options() {
    if (_Button6.isPress()) {
        current_Begin2 = command_player2_update_score;
    }
    command_player2_pop_up_mole_options();
}

void command_player2_pop_up_mole_2() {
    _ServoController1.moveServo(6, 90);
    current_Begin2 = command_player2_pop_up_mole_2_options;
}

void command_player2_pop_up_mole_2_options() {
    if (_Button7.isPress()) {
        current_Begin2 = command_player2_update_score;
    }
    command_player2_pop_up_mole_options();
}

void command_player2_pop_up_mole_3() {
    _ServoController1.moveServo(7, 90);
    current_Begin2 = command_player2_pop_up_mole_3_options;
}

void command_player2_pop_up_mole_3_options() {
    if (_Button8.isPress()) {
        current_Begin2 = command_player2_update_score;
    }
    command_player2_pop_up_mole_options();
}

void command_player2_pop_up_mole_4() {
    _ServoController1.moveServo(8, 90);
    current_Begin2 = command_player2_pop_up_mole_4_options;
}

void command_player2_pop_up_mole_4_options() {
    if (_Button9.isPress()) {
        current_Begin2 = command_player2_update_score;
    }
    command_player2_pop_up_mole_options();
}

void command_player2_pop_up_mole_5() {
    _ServoController1.moveServo(9, 90);
    current_Begin2 = command_player2_pop_up_mole_5_options;
}

void command_player2_pop_up_mole_5_options() {
    if (_Button10.isPress()) {
        current_Begin2 = command_player2_update_score;
    }
    command_player2_pop_up_mole_options();
}

void command_player2_update_score() {
    P2Score += 1;
    _7Segment1.showData(P2Score);
    current_Begin2 = command_player2_pop_up_mole_options;
}

void command_player2_pop_up_mole_options() {
    if (millis() - Begin2_recentBlockFinishTime > 1000) {
        current_Begin2 = command_player2_hide_mole;
    } 
}

void command_player2_hide_mole() {
    _ServoController1.moveServo(5, 0);
    _ServoController1.moveServo(6, 0);
    _ServoController1.moveServo(7, 0);
    _ServoController1.moveServo(8, 0);
    _ServoController1.moveServo(9, 0);
    current_Begin2 = command_player2_pop_up_mole;
}

void command_display_p1_win() {
    _7Segment1.showText("WIN");
    _7Segment2.off();
    current_Begin1 = command_hide_all_mole;
}

void command_display_p2_win() {
    _7Segment1.off();
    _7Segment2.showText("WIN");
    current_Begin1 = command_hide_all_mole;
}

void command_display_draw() {
    _7Segment1.showText("DRAW");
    _7Segment2.showText("DRAW");
    current_Begin1 = command_hide_all_mole;
}