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
#define HEADER_EMAIL "PAZAT"

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

// Months
static const String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
static const byte monthDays[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


/* ----- VARIABLES ----- */

Bouton buttonConnect;
Bouton buttonCourse;
Bouton buttonMail;
Led blue;
Led white;
Led red;
Luminosite luminosity;
MakeyMakey makey;
MotionSensor motionSensor;
Potentiometer potentiometer;
Relai relai;
Son buzzer;
Screen screen;
Temperature meteo;
Bluetooth bluetooth;

unsigned int initTimestamp;
unsigned int initMillis;
unsigned int startTime;
String courseName;
String courseLocation;
String weatherType;
int weatherTemperature;
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
		Serial.print("line: ");
		Serial.println(line[i]);

		if (line[i] == '\"' && ((anti & 1) == 1))
			++cpt;
		else if (line[i] == '\\')
			++anti;
		else if (line[i] == delim || ((cpt & 1) == 0)) {
			anti = 0;
			unsigned int tmp = !!cpt;
			Serial.print(tmp);
			Serial.print(" ");
			Serial.print(base);
			Serial.print(" ");
			Serial.print(i);
			Serial.print(" ");
			Serial.print(tmp);
			Serial.println(line.substring(tmp + base, i - tmp));
			packet->add(j++, line.substring(tmp + base, i - tmp));
			cpt = 0;
			base = i + 1;
		}
	}

	return packet;
}

unsigned long getCurrentTimestamp() {
	return initTimestamp + (millis() / 1000 - initMillis);
}

unsigned long modulo(unsigned long x, unsigned int y) {
	return x - (x / y) * y;
}

bool isLeapYear(short year) {
	return ((1970 + year) > 0) && !(modulo((1970 + year), 4)) && ((modulo((1970 + year), 100)) || !(modulo((1970 + year), 400)));
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

void sendEmail() {
	bluetooth.send(HEADER_EMAIL);
}

void screenBacklight() {
	if (motionSensor.detectsMotion())
		screen.switchOn();
	else
		screen.switchOff();
}

void displayData() {
	unsigned long timestamp = getCurrentTimestamp();
	timestamp /= 60; // Now it is minutes
	byte minute = modulo(timestamp, 60);
	timestamp /= 60; // now it is hours
	byte hour = modulo(timestamp, 60);
	timestamp /= 24; // now it is days

	// Compute the year and the day
	short year = 0;  
	byte day = 0;
	while ((unsigned)(day += (isLeapYear(year) ? 366 : 365)) <= timestamp)
		year++;
	day -= isLeapYear(year) ? 366 : 365;
	timestamp -= day; // now it is days in this year, starting at 0

	// Compute the month  and the day
	byte month = 0;
	byte monthLength = 0;
	for (month = 0; month < 12; ++month) {
		if (month == 1) // February
			if (isLeapYear(year))
				monthLength = 29;
			else
				monthLength = 28;
		else
			monthLength = monthDays[month];
		
		if (timestamp >= monthLength)
			timestamp -= monthLength;
		else
			break;
	}
	day = timestamp + 1;

	String abbreviation;
	if (day == 1)
		abbreviation = "st";
	else if (day == 2)
		abbreviation = "nd";
	else if (day == 3)
		abbreviation = "rd";
	else
		abbreviation = "th";
	screen.printMsg(day + abbreviation + " " + months[month] + " " + year, 0);

	String time = "";
	// Add a zero before the hours
	if (hour < 10)
		time += "0";
	time += hour + ":";
	// Add a zero before the minutes
	if (minute < 10)
		time += "0";
	time += minute;
	screen.printMsg("    " + time, 2);

	// LA PUTAIN DE SA MERE LA PUTE
	String humidity = meteo.humidite() + "%";
	String spaces = "";
	for (byte i = humidity.length() + weatherType.length(); i < 13; ++i)
		spaces += " ";
	screen.printMsg(humidity + spaces + weatherType, 4);

	String temperature1 = meteo.temperature() + "°C";
	String temperature2 = weatherTemperature + "°C";
	spaces = "";
	for (byte i = temperature1.length() + temperature2.length(); i < 13; ++i)
		spaces += " ";
	screen.printMsg(temperature1 + spaces + temperature2, 5);
}

void displayCourse() {

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
	screen.printMsg(" REVEIL TOI", 2);
	screen.printMsg("  CONNARD !", 3);
	while (!stop)
		for (byte i = 0; i < sizeof(notes) / sizeof(notes[0]); ++i) {
			note(notes[i], durations[i]);
			
			if (makey.touched()) {
				stop = true;
				break;
			}
		}
	displayData();
}

void makeCoffee() {
	relai.allumer(1, 1);
}

void connect() {
	screen.printMsg(" CONNECT TO", 2);
	screen.printMsg("  DEVICE...", 3);
	bluetooth.acceptConnection(BT_NAME);
	displayData();
}


/* ----- CALLBACKS ----- */

void onAgenda(String rawData) {
	Packet* data = split(rawData, ';');

	startTime = data->get(0).toInt();
	courseName = data->get(1);
	courseLocation = data->get(2);

	delete data;
}

void onWeather(String rawData) {
	Packet* data = split(rawData, ';');

	weatherType = data->get(0);
	weatherTemperature = data->get(1).toInt();

	delete data;
}

void onTravelTime(String rawData) {
	travelTime = rawData.toInt();
}

void onTimestamp(String rawData) {
	initMillis = millis() / 1000;
	initTimestamp = rawData.toInt() * 1000;
}


/* ----- COMMUNICATION ----- */

void receivedFromBluetooth() {
	String buffer;

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
	//Packet* tmp = split("data1;1", ';');

	//Serial.print(tmp->get(0));
	//Serial.print(" ");
	//Serial.println(tmp->get(1));
	//attendre(100000);

	//delete tmp;

	// Button actions
	/*if (buttonConnect.estTenuAppuye())
		connect();
	if (buttonCourse.estTenuAppuye())
		displayCourse();
	if (buttonMail.estTenuAppuye())
		sendEmail();

	receivedFromBluetooth();
	displayData();

	if (luminosity.etat() <= LUMINOSITY_THRESHOLD) {
		screenBacklight();

		if (false) {
			alarm();
		}
	}*/
}
