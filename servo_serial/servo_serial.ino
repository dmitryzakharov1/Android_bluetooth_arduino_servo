#include <Servo.h>
String readString;
Servo myservo;

int servo1pos;

void setup() {
  // put your setup code here, to run once:
Serial.begin(9600); 
myservo.attach(9);
myservo.write(7);
}

void loop() {
  // put your main code here, to run repeatedly:

while (Serial.available()) {
		delay(3);  //delay to allow buffer to fill 
		if (Serial.available() >0) {
			char c = Serial.read();  //gets one byte from serial buffer
			readString += c; //makes the string readString
		} 
	}

	if (readString.length() >0)
	{
		Serial.println(readString); //see what was received
		
		int commaIndex = readString.indexOf(' ');
		
		String firstValue = readString.substring(0, commaIndex);
		servo1pos = firstValue.toInt();
		
                if ((servo1pos > 0) & (servo1pos < 180)) {		
		myservo.write(servo1pos);
                delay(15); 
                }
		
	}
	//очистим строку
	readString = "";

}
