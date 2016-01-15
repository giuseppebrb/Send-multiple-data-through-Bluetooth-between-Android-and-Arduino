#include <SoftwareSerial.h>
 
SoftwareSerial mySerial(6, 5);
int dataFromBT;

const int RED_PIN = 11;
const int GREEN_PIN = 9;
const int BLUE_PIN = 10;

String string;
char command;

void setup() {
  Serial.begin(57600);
  Serial.println("Arduino started...");
 
  // The data rate for the SoftwareSerial port needs to 
  // match the data rate for your bluetooth board.
  mySerial.begin(9600);
  pinMode(RED_PIN, OUTPUT);
  pinMode(GREEN_PIN, OUTPUT);
  pinMode(BLUE_PIN, OUTPUT);
}


void loop() {
  // if there's a new command reset the string
  if(mySerial.available()){
    string = "";
    }

    // Construct the command string fetching the bytes, sent by Android, one by one.
    while(mySerial.available()){
      command = ((byte)mySerial.read());
      
      if(command == ':') {
        break;
      } else { 
        string += command;
      }
      delay(1);
    }

    // Print on the Monitor latest command recieved
    Serial.println(string);
    
    if (string == "RED OFF") {
    // Turn off LED red
    digitalWrite(RED_PIN, LOW);
  } else if ( string == "RED ON") {
    // Turn on LED red
    digitalWrite(RED_PIN, HIGH);
  } else if ( string == "GREEN ON" ) {
    // Turn on LEF green
    digitalWrite(GREEN_PIN, HIGH);
  } else if ( string == "GREEN OFF" ) {
    // Turn off LED green
    digitalWrite(GREEN_PIN, LOW);
  } else if ( string == "BLUE ON" ) {
    // Turn on LED blue
    digitalWrite(BLUE_PIN, HIGH);
  } else if ( string == "BLUE OFF" ) {
    // Turn off LED blue
    digitalWrite(BLUE_PIN, LOW);
  }


  if(string.startsWith("#")){
    String value = string.substring(1);
    if(value.startsWith("RED")){
      value = value.substring(3);
      analogWrite(RED_PIN, value.toInt());
    } else if (value.startsWith("GREEN")) {
      value = value.substring(5);
      analogWrite(GREEN_PIN, value.toInt());
    } else if (value.startsWith("BLUE")) {
      value = value.substring(4);
      analogWrite(BLUE_PIN, value.toInt());
    }
      
  }
  
}
