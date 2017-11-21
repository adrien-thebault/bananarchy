#include "Led.h";
#include "Luminosite.h";
#include "MakeyMakey.h";
#include "MotionSensor.h";
#include "Potentiometer.h";
#include "Relai.h";
#include "Son.h";
#include "Screen.h";
#include "Temperature.h";

/* ----- CONSTANTS ----- */

#define NB_SONGS 1;
#define LUMINOSITY_THRESHOLD 1;


/* ----- VARIABLES ----- */

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


/* ----- WIFI DRIVER ----- */


/* ----- SETUP ----- */

void setup()
{

}


/* ----- FUNCTIONS ----- */

void alarm() const
{
	switch (potentiometer.etat / NB_SONGS)
	{
		case 1:
		// SONG 1
		break;
	}
}

void makeACoffee() const
{
	relai.allume(1);
}


/* ----- MAIN ----- */

void loop()
{
	if (luminosity.etat() < LUMINOSITY_THRESHOLD)
	{
		if (makey.touched()) {}

		// Starts the alarm
		if (false)
		{
			alarm();
		}
	}
}