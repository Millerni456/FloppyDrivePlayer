#include <MusicNotes.h>

#include <SoftwareSerial.h>

#define STEP_PIN 8
#define DIRECTION_PIN 9

#define FORWARD 1
#define BACKWARD 0

#define DEBUG_PIN 13

SoftwareSerial btSerial(2, 3);

//Function Declarations
void playNote(double note, int millis);
void reset();
void wait();
void updateHead();

//Global Declarations
double stepClockPeriod = 0.00002d; //20us
int forward = 1; 
int headPosition = 0;
int stepsRemaining = 0;
int stepDelayCounter = 0;
int stepDelayCycles = 0;
int indefinite = 0;

//Use this to reset the head to position 0.
int forceZero = 0;

void setup() {
  // put your setup code here, to run once:
  pinMode(DEBUG_PIN, OUTPUT);           // set pin to input
  pinMode(STEP_PIN, OUTPUT);
  pinMode(DIRECTION_PIN, OUTPUT);
  btSerial.begin(9600);

  //Use the Serial Monitor for debugging.
  Serial.begin(9600);

  digitalWrite(DIRECTION_PIN, HIGH);
  int i = 0;
  for (; i < 80; i++)
  {
    
    digitalWrite(STEP_PIN, HIGH);
    digitalWrite(DEBUG_PIN, HIGH);
    delay(1);
    digitalWrite(STEP_PIN, LOW);
    digitalWrite(DEBUG_PIN, LOW);
    delay(1);
  }
  digitalWrite(DIRECTION_PIN, LOW);
}

//This will store the current command from the Android app.
char command[2];
int commandIndex = 0;
bool commandReady = false;
long timeout = 50000; //2 seconds.
long timeoutCounter;

void loop() {
  delayMicroseconds(20);
  
  if (btSerial.available() > 0) {
    //Reset the timeout.
    timeoutCounter = 0;
    
    //Read the incoming byte:
    char incomingByte = btSerial.read();
    command[commandIndex++] = incomingByte;

    //Check if the command buffer is filled.
    if (commandIndex == 2)
    {
      commandReady = true;
    }
  }
  else
  {
    timeoutCounter += 1;
    if (timeoutCounter > timeout)
    {
      commandIndex = 0;
      commandReady = false;
      Serial.println("FLUSHED");
      timeoutCounter = 0;
    }
  }

  if (commandReady)
  {
    //Process the command.

    //Serial.print("Executing command: ");
    //Serial.print(command[0], BIN);
    //Serial.println(command[1], BIN);

    //React to the Bluetooth input.
    int input = (command[0] << 8) | command[1];
    int note = (0x0007 & input);
    int sharp = (0x0008 & input) >> 3;
    int octave = (0x0070 & input) >> 4;
    int channel = (0x0380 & input) >> 7; //ignored
    int length = (0x7C00 & input) >> 10; //ignored
    int format = (0x8000 & input) >> 15; //ignored

    playNote(getNote(note, sharp, octave), 1000);
    
    Serial.print("Now playing: ");
    char result[9];
    getNoteName(note, sharp, octave, result);
    Serial.println(result);
    
    commandIndex = 0;
    commandReady = false;
  } 
  
  updateHead();
}

void playNote(double noteFrequency, int millis)
{
    double phononDuration = 1.0d / noteFrequency;

    //Step delay cycles is the number of step clock cycles that must
    //occur for the high-portion of the step pulse.
    stepDelayCycles = (int)(phononDuration / stepClockPeriod / 2.0d);

    //Get the time, in seconds the note should play.
    double noteDurationSeconds = (millis/(1000.0d));
    
    if (noteFrequency != NO_NOTE)
    {
        indefinite = 1;
        stepsRemaining = (int)(noteDurationSeconds * noteFrequency);
        //mute = 0;
    }
    else
    {
        //Perform no steps if muting.
        indefinite = 0;
        stepsRemaining = 0;
        stepDelayCounter = 0;
        //mute = 1;
    }
    
    //wait(); //No need to block on this function (keep for testing purposes).
} 

//This function is called approximately once every 20us.
void updateHead()
{
  if (stepsRemaining > 0)
  {
    if (stepDelayCounter == 0)
    {
      //Note has begun.

      //If head is facing foward, we should increment the position.
      if (forward == 1)
      {
        headPosition++;
      }
      else
      {
        headPosition--;
      }
      
      //Step the head.
      digitalWrite(STEP_PIN, HIGH);
      digitalWrite(DEBUG_PIN, HIGH);
      
      //We should do a bounds check to make sure the direction is updated.
      //This will prepare the next step properly.
      if (headPosition >= 80 || headPosition <= 0)
      {
        forward = !forward;

        //Update the signal for the direction pin.
        if (forward == 1)
        {
          digitalWrite(DIRECTION_PIN, LOW);
        }
        else
        {
          digitalWrite(DIRECTION_PIN, HIGH);
        }
      }
    }
    else if (stepDelayCounter == (int)(stepDelayCycles / 2))
    {
      digitalWrite(DEBUG_PIN, LOW);
    }
    else if (stepDelayCounter == stepDelayCycles)
    {
      //Half-way through note, need to deassert the pulse.
      digitalWrite(STEP_PIN, LOW);
    }
    else if (stepDelayCounter == stepDelayCycles * 2)
    {
      //Note is finished.
      if (indefinite == 0)
      {
        stepsRemaining--;
      }

      //Make this -1 since we will increment it back to 0.
      stepDelayCounter = -1;
    }
    stepDelayCounter++;
  }
}

