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

// DATA TYPES
#define DATA_AGENDA 1;
#define DATA_WEATHER 2;
#define DATA_TRAVEL_TIME 3;
#define DATA_TIMESTAMP 4;

/* ----- TYPES ----- */

typedef struct {
  byte type;
  String data;
} Data;

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
unsigned int agendaBeginAt;
String agendaName;
String weatherType;
int weatherTemp;
int travelTime;


/* ----- WIFI DRIVER ----- */


/* ----- SETUP ----- */

void setup()
{
	Serial.begin(9600);
	screen.setContrast(70, true);

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
	goto end;
}

void alarm()
{
	// Music : He's a pirate
	int notes[] = [
		RE3, RE3, RE3, MI,
		FA3, FA3, FA3, SOL3,
		MI3, MI3, RE3, DO3,
		DO3, RE3, SILENCE, LA2, SI2,
		RE3, RE3, RE3, MI3,
		FA3, FA3, FA3, SOL3,
		MI3, MI3, RE3, DO3,
		RE3, SILENCE, LA2, DO3,
		RE3, RE3, RE3, FA3,
		SOL3, SOL3, SOL3, LA3,
		SI3, SI3, LA3, SOL3,
		LA3, RE3, SILENCE, RE3, MI3,
		FA3, FA3, SOL3,
		LA3, RE3, SILENCE, RE3, FA3,
		MI3, MI3, FA3, RE3,
		MI3, SILENCE
	];

	int durations[] = [
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		EIGTH, QUARTER, EIGTH, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		EIGTH, QUARTER, EIGTH, EIGTH, EIGTH,
		QUARTER, QUARTER, QUARTER,
		EIGTH, QUARTER, EIGTH, EIGTH, EIGTH,
		QUARTER, QUARTER, EIGTH, EIGTH,
		QUARTER, QUARTER
	];

	bool stop = false;
	while (!stop)
		for (byte i = 0; i < notes.length; ++i) {
			note(notes[i], durations[i]);
			if (makey.touched()) {
				stop = true;
				break;
			}
		}
}

void makeACoffee()
{
	relai.allumer(1, 1);
}

void updateDisplay() {

	// TODO : ICI ON AFFICHE LES VARIABLES SUR l'ECRAN

}

/* ----- CALLBACKS ----- */

void onAgenda(Data d) {

	String name;
	unsigned int begin;

	// TODO : SPLIT LES DATA RECUES

	agendaBeginAt = begin;
	agendaName = name;

}

void onWeather(Data d) {

	String type;
	int temp;

	// TODO : SPLIT LES DATA RECUES

	weatherType = type;
	weatherTemp = temp;

}

void onTravelTime(Data d) {
	travelTime = d.data.toInt();
}

void onTimestamp(Data d) {

	initMillis = millis()/1000;
	initTimestamp = d.data.toInt()*1000;

}

unsigned int getCurrentTimestamp() {
	return initTimestamp + (millis()/1000-initMillis);
}

/* ----- COMMUNICATION ----- */

Data readFromBluetooth() {

	Data res;

	// TODO : COUCOU L'EQUIPE COMMUNICATION
	// Y'A DES CONSTANTES POUR LE CHAMP TYPE (i.e. DATA_AGENDA, ...)

	return res;

}

/* ----- MAIN ----- */

void loop()
{

	/** callback sur les données reçues */
	data = readFromBluetooth();

	if(data.type == DATA_AGENDA) onAgenda(data);
	if(data.type == DATA_WEATHER) onWeather(data);
	if(data.type == DATA_TRAVEL_TIME) onTravelTime(data);
	if(data.type == DATA_TIMESTAMP) onTimestamp(data);

	/** do whatever we need to do */

	updateDisplay();

	if (luminosity.etat() <= LUMINOSITY_THRESHOLD) {
		displayTime();

		if (false) {
			alarm();
		}
	}

}
