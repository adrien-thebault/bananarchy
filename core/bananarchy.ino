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
#define BT_NAME "Thingz-INSA"
#define BT_PASSWORD "1234"

// Data header
#define HEADER_TIMESTAMP "TIMESTAMP"
#define HEADER_PREPARATION_TIME "PREPARATION_TIME"
#define HEADER_AGENDA "AGENDA"
#define HEADER_WEATHER "WEATHER"
#define HEADER_TRAVEL_TIME "TRAVEL_TIME"
#define HEADER_EMAIL "P"

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

// Snooze parameters in seconds
#define REPEAT 30//300
#define MARGIN 60//300
#define WARNING 300//600
#define EMERGENCY 150//300

// Others
#define SCREEN_LINE_LENGTH 13
#define TEMPO 190
#define LUMINOSITY_THRESHOLD 10


/* ----- VARIABLES ----- */

Bouton buttonCourse;
Bouton buttonConnect;
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

unsigned int repeat;
bool snooze;
bool coffee;

unsigned long initTimestamp;
unsigned long initMillis;

unsigned long courseTimestamp;
String courseName;
String courseLocation;

String weatherType;
int weatherTemperature;

unsigned int travelTime;
unsigned int preparationTime;

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

	String get(unsigned int index) const {
		return this->index == index ? value : (next != nullptr ? next->get(index) : "");
	}

	void add(unsigned int index, String value) {
		if (next == nullptr) {
			this->index = index;
			this->value = value;
			next = new Packet();
		}
		else
			next->add(index, value);
	}
};

Packet* split(String line, char delim) {
	Packet* packet = new Packet();
	unsigned int size = line.length();
	for (unsigned int base = 0, i = 0, cpt = 0, anti = 0, j = 0; i < size; ++i) {
		if (line[i] == '\"' && ((anti & 1) == 0))
			++cpt;
		else if (line[i] == '\\')
			++anti;
		else if(line[i] == '\"' && ((anti & 1) == 1))
			anti = 0;
		else if ((line[i] == delim && ((cpt & 1) == 0)) || i == size - 1) {
			anti = 0;
			unsigned int tmp = !!cpt;
			if (i == size - 1)
				++i;
			packet->add(j++, line.substring(tmp + base, i - tmp));
			cpt = 0;
			base = i + 1;
		}
	}

	return packet;
}

class Date {
private:
	static const String MONTHS[12];
	static const byte MONTH_DAYS[12];

	short year;
	byte month;
	byte day;
	byte hour;
	byte minute;

	unsigned long modulo(unsigned long x, unsigned long y) const {
		return x - (x / y) * y;
	}

public:
	Date(unsigned long timestamp) {
		timestamp /= 60; // Now it is minutes
		minute = modulo(timestamp, 60);
		timestamp /= 60; // now it is hours
		hour = modulo(timestamp, 24);

		timestamp /= 24; // now it is days

		// Compute the year and the day
		year = 1970; // Timestamp begin at 1970
		unsigned int days = 0;
		while ((unsigned)(days += (isLeapYear(year) ? 366 : 365)) <= timestamp)
			year++;
		days -= isLeapYear(year) ? 366 : 365;
		timestamp -= days; // now it is days in this year, starting at 0

		// Compute the month and the day
		month = 0;
		for (month = 0; month < 12; ++month) {
			byte monthLength = 0;

			if (month == 1) // February
				if (isLeapYear(year))
					monthLength = 29;
				else
					monthLength = 28;
			else
				monthLength = MONTH_DAYS[month];
			
			if (timestamp >= monthLength)
				timestamp -= monthLength;
			else
				break;
		}
		++month;
		day = timestamp + 1;	
	}

	bool isLeapYear(short year) const {
		return (year > 0) && !(modulo(year, 4)) && ((modulo(year, 100)) || !(modulo(year, 400)));
	}

	short getYear() const {
		return year;
	}

	byte getMonth() const {
		return month;
	}

	String getMonthName() const {
		return MONTHS[month - 1];
	}

	byte getDay() const {
		return day;
	}

	byte getHour() const {
		return hour;
	}

	byte getMinute() const {
		return minute;
	}
};

const String Date::MONTHS[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
const byte Date::MONTH_DAYS[] = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};


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

void displayTime(const Date& date) {
	screen.clear();

	String fullDate = "";
	if (date.getDay() < 10)
		fullDate += " ";
	fullDate += date.getDay();

	if (date.getDay() == 1)
		fullDate += "st";
	else if (date.getDay() == 2)
		fullDate += "nd";
	else if (date.getDay() == 3)
		fullDate += "rd";
	else
		fullDate += "th";
	fullDate += " " + date.getMonthName() + " " + date.getYear();
	//screen.printMsg(fullDate, 0);
	Serial.println(fullDate);

	//String time = "";
	// Add a zero before the hours
	Serial.print("    ");
	if (date.getHour() < 10)
		Serial.print("0");
	//	time += "0";
	Serial.print(date.getHour());
	Serial.print(":");
	//time += date.getHour() + "\:";
	// Add a zero before the minutes
	if (date.getMinute() < 10)
		Serial.print("0");
	//	time += "0";
	Serial.println(date.getMinute());
	//time = "    " + time + date.getMinute();
	//screen.printMsg(time, 2);
}

unsigned long getCurrentTimestamp() {
	return initTimestamp + (millis() - initMillis) / 1000;
}

void displayData() {
	screen.clear();
	displayTime(Date(getCurrentTimestamp()));

	String humidity = meteo.humidite() + "h";
	Serial.print(meteo.humidite());
	Serial.print("h");
	String spaces = "";
	for (byte i = humidity.length() + weatherType.length(); i < SCREEN_LINE_LENGTH; ++i)
		//spaces += " ";
		Serial.print(" ");
	
	Serial.println("clouds");
	//screen.printMsg(humidity + spaces + weatherType, 4);

	String temperature1 = meteo.temperature() + "C";
	Serial.print(meteo.temperature());
	Serial.print("C");
	String temperature2 = weatherTemperature + "C";
	spaces = "";
	for (byte i = temperature1.length() + temperature2.length(); i < SCREEN_LINE_LENGTH; ++i)
		//spaces += " ";
		Serial.print(" ");
	Serial.print(weatherTemperature);
	Serial.println("C");
	//screen.printMsg(temperature1 + spaces + temperature2, 5);
}

void displayCourse() {
	screen.clear();
	displayTime(Date(courseTimestamp));

	String spaces = "";
	String course = courseName.substring(SCREEN_LINE_LENGTH);
	byte size = (SCREEN_LINE_LENGTH - course.length()) / 2;
	for (byte i = 0; i < size; ++i)
		Serial.print(" ");
		//spaces += " ";
	//screen.printMsg(spaces + course, 4);
	Serial.println(course);

	spaces = "";
	size = (SCREEN_LINE_LENGTH - courseLocation.length()) / 2;
	for (byte i = 0; i < size; ++i)
		Serial.print(" ");
		//spaces += " ";
	//screen.printMsg(spaces + courseLocation, 4);
	Serial.println(courseLocation);
}

void displayWakeUp() {
	screen.clear();
	//screen.printMsg("REVEILLE TOI", 2);
	Serial.println("REVEILLE TOI");
	//screen.printMsg("  PIERRE !", 3);
	Serial.println("  PIERRE !");
}

void displayConnect() {
	screen.clear();
	//screen.printMsg(" CONNECT TO", 2);
	Serial.println(" CONNECT TO");
	//screen.printMsg("  TABLET...", 3);
	Serial.println("  TABLET...");
}

void note(unsigned int note, float duration) {
	if (note == SILENCE) {
		buzzer.arreteDeSonner();
		attendre(duration * 60000 / TEMPO);
	} else
		buzzer.tone(note, duration * 60000 / TEMPO);
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
	displayWakeUp();
	screen.switchOn();
	while (!stop)
		for (byte i = 0; i < sizeof(notes) / sizeof(notes[0]); ++i) {
			note(notes[i], durations[i]);
			
			if (makey.touched()) {
				stop = true;
				break;
			}
		}
	screen.switchOff();
	displayData();
}

void makeCoffee() {
	coffee = true;
	relai.allumer(1, 1);
}

void connect() {
	displayConnect();
	bluetooth.acceptConnection(BT_NAME);
	displayData();
}


/* ----- CALLBACKS ----- */

void onTimestamp(String rawData) {
	initMillis = millis();
	initTimestamp = rawData.toInt();
}

void onPreparationTime(String rawData) {
	preparationTime = rawData.toInt() * 60;
}

void onAgenda(String rawData) {
	repeat = 0;
	snooze = false;
	coffee = false;
	Packet* data = split(rawData, ';');

	courseTimestamp = atol(data->get(0).c_str());
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
	travelTime = rawData.toInt() * 60;
}


/* ----- COMMUNICATION ----- */

void receivedFromBluetooth() {
	String buffer;

	if (bluetooth.dataAvailable())
		buffer = bluetooth.receive();

	if (buffer.startsWith(HEADER_TIMESTAMP))
		onTimestamp(buffer.substring(10));
	else if (buffer.startsWith(HEADER_PREPARATION_TIME))
		onPreparationTime(buffer.substring(17));
	else if (buffer.startsWith(HEADER_AGENDA))
		onAgenda(buffer.substring(7));
	else if (buffer.startsWith(HEADER_WEATHER))
		onWeather(buffer.substring(8));
	else if (buffer.startsWith(HEADER_TRAVEL_TIME))
		onTravelTime(buffer.substring(12));
}


/* ----- SETUP ----- */

void setup() {
	Serial.begin(9600);
	while (!Serial);
	screen.setContrast(70, true);
	bluetooth.setPassword(BT_PASSWORD);
	connect();
	repeat = 0;
	snooze = false;
	coffee = false;
}


/* ----- MAIN ----- */

void loop() {
	// Button actions
	if (buttonCourse.estTenuAppuye())
		displayCourse();
	else
		displayData();
	if (buttonConnect.estTenuAppuye())
		connect();
	if (buttonMail.estTenuAppuye())
		sendEmail();

	receivedFromBluetooth();

	// Bananarchy core
	
	if (luminosity.etat() <= LUMINOSITY_THRESHOLD) {
		screenBacklight();

		if (!snooze && getCurrentTimestamp() >= (courseTimestamp - travelTime - preparationTime - MARGIN + repeat)) {
			alarm();
			blue.switchOn();
			repeat += REPEAT;
	        relai.allumer(1, 1);
		}
	} else {
		// You are supposed to be awake
		snooze = true;
		//if (snooze && !coffee) {
	    relai.allumer(1, 1);
		//}

		if (getCurrentTimestamp() >= (courseTimestamp - travelTime - WARNING) && getCurrentTimestamp() < (courseTimestamp - travelTime - EMERGENCY)) {
			blue.switchOff();
			white.switchOn();
		} else if (getCurrentTimestamp() >= (courseTimestamp - travelTime - EMERGENCY) && getCurrentTimestamp() < courseTimestamp) {
			white.switchOff();
			red.switchOn();
		} else {
			red.switchOff();
		}
	}
}
