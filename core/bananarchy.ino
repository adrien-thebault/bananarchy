#include "Bouton.h"
#include "Led.h"
#include "Luminosite.h"
#include "MakeyMakey.h"
#include "MotionSensor.h"
#include "Potentiometer.h"
#include "Relai.h"
#include "Son.h"
#include "Screen.h"
#include "Temperature.h"

/* ----- CONSTANTS ----- */

// Notes
#define SILENCE 0
// Octave 2
#define DO2 131
#define DOb2 138
#define RE2 147
#define REb2 156
#define MI2 165
#define FA2 175
#define FAb2 185
#define SOL2 196
#define SOLb2 208
#define LA2 220
#define LAb2 233
#define SI2 247
// Octave 3
#define DO3 262
#define DOb3 277
#define RE3 294
#define REb3 311
#define MI3 330
#define FA3 349
#define FAb3 370
#define SOL3 392
#define SOLb3 415
#define LA3 440
#define LAb3 466
#define SI3 494

// Durations
#define DOUBLE_WHOLE 8
#define WHOLE 4
#define HALF 2
#define QUARTER 1
#define EIGTH .5
#define SIXTEENTH .25
#define THIRTY_SECOND .125
#define SIXTY_FOURTY .0625

// OTHERS
#define TEMPO 190
#define LUMINOSITY_THRESHOLD 10


/* ----- VARIABLES ----- */

Bouton button1;
Bouton button2;
Led red;
Led white;
Led blue;
Luminosite luminosity;
MakeyMakey makey;
MotionSensor motionSensor;
Potentiometer potentiometer;
Relai relai;
Son buzzer;
Screen screen;
Temperature temperature;

unsigned int initTimestamp;
unsigned int initMillis;


/* ----- WIFI DRIVER ----- */


/* ----- SETUP ----- */

void setup()
{
	Serial.begin(9600);
	screen.setContrast(70, true);

	// TODO
	//initTimestamp;
	initMillis = millis();
}


/* ----- FUNCTIONS ----- */

void displayTime() {
	if (motionSensor.detectsMotion())
		screen.switchOn();
	else
		screen.switchOff();
}

void note(float note, float duration)
{
	if (note == SILENCE) {
		buzzer.arreteDeSonner();
		attendre(duration * 60000 / TEMPO);
	} else
		buzzer.tone(note, duration * 60000 / TEMPO);
}

void alarm()
{
	while (true) {
		// Music : He's a pirate
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(MI3, EIGTH);

		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(FA3, EIGTH);
		if (makey.touched()) break;
		note(SOL3, EIGTH);

		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(DO3, EIGTH);

		if (makey.touched()) break;
		note(DO3, EIGTH);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(SILENCE, EIGTH);
		if (makey.touched()) break;
		note(LA2, EIGTH);
		if (makey.touched()) break;
		note(SI2, EIGTH);

		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(MI3, EIGTH);

		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(FA3, EIGTH);
		if (makey.touched()) break;
		note(SOL3, EIGTH);

		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(DO3, EIGTH);

		if (makey.touched()) break;
		note(RE3,QUARTER);
		if (makey.touched()) break;
		note(SILENCE, QUARTER);
		if (makey.touched()) break;
		note(LA2, EIGTH);
		if (makey.touched()) break;
		note(DO3, EIGTH);

		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(FA3, EIGTH);

		if (makey.touched()) break;
		note(SOL3, QUARTER);
		if (makey.touched()) break;
		note(SOL3, QUARTER);
		if (makey.touched()) break;
		note(SOL3, EIGTH);
		if (makey.touched()) break;
		note(LA3, EIGTH);

		if (makey.touched()) break;
		note(SI3, QUARTER);
		if (makey.touched()) break;
		note(SI3, QUARTER);
		if (makey.touched()) break;
		note(LA3, EIGTH);
		if (makey.touched()) break;
		note(SOL3, EIGTH);

		if (makey.touched()) break;
		note(LA3, EIGTH);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(SILENCE, EIGTH);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(MI3, EIGTH);

		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(FA3, QUARTER);
		if (makey.touched()) break;
		note(SOL3, QUARTER);

		if (makey.touched()) break;
		note(LA3, EIGTH);
		if (makey.touched()) break;
		note(RE3, QUARTER);
		if (makey.touched()) break;
		note(SILENCE, EIGTH);
		if (makey.touched()) break;
		note(RE3, EIGTH);
		if (makey.touched()) break;
		note(FA3, EIGTH);

		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(FA3, EIGTH);
		if (makey.touched()) break;
		note(RE3, EIGTH);

		if (makey.touched()) break;
		note(MI3, QUARTER);
		if (makey.touched()) break;
		note(SILENCE, QUARTER);
	}
}

void makeACoffee()
{
	relai.allumer(1, 1);
}


/* ----- MAIN ----- */

void loop()
{
	if (luminosity.etat() <= LUMINOSITY_THRESHOLD) {
		displayTime();

		if (false) {
			alarm();
		}
	}
}
