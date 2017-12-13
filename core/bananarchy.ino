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
#include "Bluetooth.h"


/* ----- CONSTANTS ----- */

// Credentials
#define BT_NAME "ThingzBT"
#define BT_PASSWORD "1234"

// Data header
#define HEADER_AGENDA "AGENDA"
#define HEADER_WEATHER "WEATHER"
#define HEADER_TRAVEL_TIME "TRAVEL_TIME"
#define HEADER_TIMESTAMP "TIMESTAMP"

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

// Others
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
Bluetooth bluetooth;

unsigned int initTimestamp;
unsigned int initMillis;
unsigned int agendaBeginAt;
String agendaName;
String weatherType;
int weatherTemp;
int travelTime;


/* ----- CLASSES ----- */

class Packet {
private:
	String value;
	unsigned int index;
	Packet* next;

public:
	Packet() : next(nullptr) {}

	~Packet() {
		if (next != nullptr)
			delete next;
	}

	String get(const unsigned int index) const {
		return this->index == index ? value : (next != nullptr ? next->get(index) : "");
	}

	void add(const unsigned int index, const String value) {
		if (next == nullptr) {
			this->index = index;
			this->value = value;
			next = new Packet();
		}
	}
};


/* ----- USEFUL FUNCTIONS ----- */

Packet* split(const String line, const char delim) {
	Packet* packet = new Packet();
	unsigned int size = line.length();
	for (unsigned int base = 0, i = 0, cpt = 0, anti = 0, j = 0; i < size; ++i) {
		if (line[i] == '\"' && ((anti & 1) == 1))
			++cpt;
		else if (line[i] == '\\')
			++anti;
		else if (line[i] == delim || ((cpt & 1) == 0)) {
			anti = 0;
			int tmp = !!cpt;
			packet->add(j++, line.substring(tmp + base, i - tmp));
			cpt = 0;
			base = i + 1;
		}
	}

	return packet;
}

void note(const unsigned int note, const float duration) {
	if (note == SILENCE) {
		buzzer.arreteDeSonner();
		attendre(duration * 60000 / TEMPO);
	} else
		buzzer.tone(note, duration * 60000 / TEMPO);
}


/* ----- SETUP ----- */

void setup() {
	Serial.begin(9600);
	screen.setContrast(70, true);
	bluetooth.setPassword(BT_PASSWORD);
}


/* ----- FUNCTIONS ----- */

void displayTime() {
	if (motionSensor.detectsMotion())
		screen.switchOn();
	else
		screen.switchOff();
}

void alarm() {
	// Music : He's a pirate
	int notes[] = {
		RE3, RE3, RE3, MI3,
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
	};

	float durations[] = {
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
	};

	bool stop = false;
	while (!stop)
		for (byte i = 0; i < sizeof(notes)/sizeof(notes[0]); ++i)
		{
			note(notes[i], durations[i]);
			if (makey.touched())
			{
				stop = true;
				break;
			}
		}
}

void makeCoffee() {
	relai.allumer(1, 1);
}

void sendEmail() {

}

void updateDisplay() {

	// TODO : ICI ON AFFICHE LES VARIABLES SUR l'ECRAN

}

/* ----- CALLBACKS ----- */

void onAgenda(String rawData) {
	Packet* data = split(rawData, ';');

	agendaName = data->get(0);
	agendaBeginAt = data->get(1).toInt();

	delete data;
}

void onWeather(String rawData) {
	Packet* data = split(rawData, ';');

	weatherType = data->get(0);
	weatherTemp = data->get(1).toInt();

	delete data;
}

void onTravelTime(String rawData) {
	travelTime = rawData.toInt();
}

void onTimestamp(String rawData) {
	initMillis = millis() / 1000;
	initTimestamp = rawData.toInt() * 1000;
}

unsigned int getCurrentTimestamp() {
	return initTimestamp + (millis() / 1000 - initMillis);
}

/* ----- COMMUNICATION ----- */

void receivedFromBluetooth() {

	String buffer = "";

	if (bluetooth.dataAvailable())
		buffer = bluetooth.receive();

	if (buffer.startsWith(HEADER_AGENDA))
		onAgenda(buffer.substring(7));
	else if (buffer.startsWith(HEADER_WEATHER))
		onWeather(buffer.substring(8));
	else if (buffer.startsWith(HEADER_TRAVEL_TIME))
		onTravelTime(buffer.substring(12));
	else if (buffer.startsWith(HEADER_TIMESTAMP))
		onTimestamp(buffer.substring(10));

}

/* ----- MAIN ----- */

void loop() {
	// Button actions
	if (button1.estTenuAppuye())
		bluetooth.acceptConnection(BT_NAME);
	if (button2.estTenuAppuye())
		sendEmail();

	receivedFromBluetooth();

	/** do whatever we need to do */

	updateDisplay();

	if (luminosity.etat() <= LUMINOSITY_THRESHOLD) {
		displayTime();

		if (false) {
			alarm();
		}
	}
}
