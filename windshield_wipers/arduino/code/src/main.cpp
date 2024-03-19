#include "Arduino.h"

// front windshield
const uint8_t BTN1_PIN = 2;      // wiper switch in slow wipes position
const uint8_t BTN2_PIN = 4;      // wiper switch in fast wipes position
const uint8_t BTN3_PIN = 7;      // wiper switch in spray and wipes position (activate washer jets)
const uint8_t MOTORA_PIN1 = 3;   // wiper motor
const uint8_t MOTORA_PIN2 = 5;   // wiper motor
const uint8_t SWA1_PIN = 8;      // wiper limit switch (start position)
const uint8_t SWA2_PIN = 12;     // wiper limit switch (end position)
const uint8_t PUMPA_PIN = 6;     // water pump 

// rear windshield
const uint8_t BTN4_PIN = A0;      // wiper switch in slow wipes position
const uint8_t BTN5_PIN = A1;      // wiper switch in fast wipes position
const uint8_t BTN6_PIN = A2;      // wiper switch in spray and wipes position (activate washer jets)
const uint8_t MOTORB_PIN1 = 9;    // wiper motor
const uint8_t MOTORB_PIN2 = 10;   // wiper motor
const uint8_t SWB1_PIN = A3;      // wiper limit switch (start position)
const uint8_t SWB2_PIN = A4;      // wiper limit switch (end position)
const uint8_t PUMPB_PIN = 11;     // water pump 

const uint8_t STATE_IDLE = 0;
const uint8_t STATE_WIPER_START = 1;
const uint8_t STATE_WIPER_WAIT1 = 2;
const uint8_t STATE_WIPER_REVERSE = 3;
const uint8_t STATE_WIPER_WAIT2 = 4;
const uint8_t STATE_WIPER_STOP = 5;
const uint8_t STATE_WIPER_WAIT1S = 6;
const uint8_t STATE_WIPER_WAIT2S = 7;
const uint8_t STATE_PUMP_ON = 8;
const uint8_t STATE_PUMP_WAIT1 = 9;
const uint8_t STATE_PUMP_OFF = 10;
const uint8_t STATE_PUMP_EXIT_LOOP = 11;

uint8_t fsm1_current_state, fsm2_current_state;
uint8_t fsm1_return_state, fsm2_return_state;
unsigned long fsm1_timer_start, fsm2_timer_start;

void setup() {
    Serial.begin(115200);

    pinMode(BTN1_PIN, INPUT_PULLUP);
    pinMode(BTN2_PIN, INPUT_PULLUP);
    pinMode(BTN3_PIN, INPUT_PULLUP);
    pinMode(MOTORA_PIN1, OUTPUT);
    analogWrite(MOTORA_PIN1, 0);
    pinMode(MOTORA_PIN2, OUTPUT);
    analogWrite(MOTORA_PIN2, 0);
    pinMode(SWA1_PIN, INPUT_PULLUP);
    pinMode(SWA2_PIN, INPUT_PULLUP);
    pinMode(PUMPA_PIN, OUTPUT);
    analogWrite(PUMPA_PIN, 0);

    pinMode(BTN4_PIN, INPUT_PULLUP);
    pinMode(BTN5_PIN, INPUT_PULLUP);
    pinMode(BTN6_PIN, INPUT_PULLUP);
    pinMode(MOTORB_PIN1, OUTPUT);
    analogWrite(MOTORB_PIN1, 0);
    pinMode(MOTORB_PIN2, OUTPUT);
    analogWrite(MOTORB_PIN2, 0);
    pinMode(SWB1_PIN, INPUT_PULLUP);
    pinMode(SWB2_PIN, INPUT_PULLUP);
    pinMode(PUMPB_PIN, OUTPUT);
    analogWrite(PUMPB_PIN, 0);

    fsm1_current_state = STATE_IDLE;
    fsm2_current_state = STATE_IDLE;
}

void update_fsm_state(uint8_t* current_state, uint8_t* return_state, unsigned long* timer_start,
                      uint8_t btn_mode1, uint8_t btn_mode2, uint8_t btn_mode3,
                      uint8_t motor_pin1, uint8_t motor_pin2, uint8_t pump_pin,
                      uint8_t limit_sw1, uint8_t limit_sw2) {
    switch (*current_state) {
        case STATE_IDLE:
            if (digitalRead(btn_mode1) == LOW) {
                *return_state = STATE_WIPER_WAIT1S; 
                *current_state = STATE_WIPER_START;
            } else if (digitalRead(btn_mode2) == LOW) {
                *return_state = STATE_WIPER_WAIT2S;
                *current_state = STATE_WIPER_START;
            } else if (digitalRead(btn_mode3) == LOW) {
                *current_state = STATE_PUMP_ON;
            }
            break;
        case STATE_WIPER_START:
            analogWrite(motor_pin1, 100);
            analogWrite(motor_pin2, 0);
            *current_state = STATE_WIPER_WAIT1;
            break;
        case STATE_WIPER_WAIT1:
            if (digitalRead(limit_sw2) == LOW) {
                *current_state = STATE_WIPER_REVERSE;
            }
            break;
        case STATE_WIPER_REVERSE:
            analogWrite(motor_pin1, 0);
            analogWrite(motor_pin2, 100);
            *current_state = STATE_WIPER_WAIT2;
            break;
        case STATE_WIPER_WAIT2:
            if (digitalRead(limit_sw1) == LOW) {
                *current_state = STATE_WIPER_REVERSE;
            }
            break;
        case STATE_WIPER_STOP:
            analogWrite(motor_pin1, 0);
            analogWrite(motor_pin2, 0);
            *timer_start = millis();
            *current_state = *return_state;
            break;
        case STATE_WIPER_WAIT1S:
            if (millis() - *timer_start > 1000) {
                *current_state = STATE_IDLE;
            }
            break;
        case STATE_WIPER_WAIT2S:
            if (millis() - *timer_start > 2000) {
                *current_state = STATE_IDLE;
            }
            break;
        case STATE_PUMP_ON:
            analogWrite(pump_pin, 100);
            *timer_start = millis();
            *current_state = STATE_PUMP_WAIT1;
            break;
        case STATE_PUMP_WAIT1:
            if (millis() - *timer_start > 2000) {
                *current_state = STATE_PUMP_OFF;
            }
            break;
        case STATE_PUMP_OFF:
            analogWrite(pump_pin, 0);
            *return_state = STATE_PUMP_EXIT_LOOP;
            *current_state = STATE_WIPER_START;
            break;
        case STATE_PUMP_EXIT_LOOP:
            *return_state = STATE_IDLE;
            *current_state = STATE_WIPER_START;
            break;
    }
}

void loop() {
    update_fsm_state(&fsm1_current_state, &fsm1_return_state, &fsm1_timer_start,
                     BTN1_PIN, BTN2_PIN, BTN3_PIN,
                     MOTORA_PIN1, MOTORA_PIN2, PUMPA_PIN,
                     SWA1_PIN, SWA2_PIN);
    update_fsm_state(&fsm2_current_state, &fsm2_return_state, &fsm2_timer_start,
                     BTN4_PIN, BTN5_PIN, BTN6_PIN,
                     MOTORB_PIN1, MOTORB_PIN2, PUMPB_PIN,
                     SWB1_PIN, SWB2_PIN);
}
