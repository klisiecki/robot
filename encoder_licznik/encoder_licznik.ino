// http://majsterkowo.pl/forum/przerwania-dlaczego-jest-to-proste-t984.html

int requestPin = 4;
int left1Pin = 12;
int left2Pin = 13;
int right1Pin = 10;
int right2Pin = 11;

volatile unsigned long counterL = 0;
volatile unsigned long counterR = 0;
unsigned long counterLbuff = 0;
unsigned long counterRbuff = 0;


double positionX = 0;
double positionY = 0;
double positionAngle = 0;


double wheelsDistance = 205.0;
double wheelDiameter = 60.0;
double gearRatio = 27.0;
double encoderResolution = 128.0;



void setup()
{
  pinMode(requestPin, INPUT);                   
  
  pinMode(left1Pin, INPUT);
  pinMode(left2Pin, INPUT);
  pinMode(right1Pin, INPUT);
  pinMode(right2Pin, INPUT);
  
  attachInterrupt(0, blinkR, RISING);      // przerwanie int.0  - pin 2
  attachInterrupt(1, blinkL, RISING);      // przerwanie int.1  - pin 3
                                          
  Serial.begin(115200);
}

void loop()
{

  //if(digitalRead(requestPin) == HIGH) {
    
    while(digitalRead(requestPin) == LOW);
    while(digitalRead(requestPin) == HIGH);
    
    noInterrupts();
    counterLbuff = counterL;
    counterL = 0;
    counterRbuff = counterR;
    counterR = 0;
    interrupts();

    //Serial.println(counterBuff);
    calculate(counterLbuff, counterRbuff);
    
    char str[21];
    String buffer = ">";
    
    dtostrf(positionX, 20, 5, str);
    buffer = buffer + str;
    buffer = buffer + " ";
    
    dtostrf(positionY, 20, 5, str);
    buffer = buffer + str;
    buffer = buffer + " ";
    
    dtostrf(positionAngle, 20, 5, str);
    buffer = buffer + str;
    buffer = buffer + "<";
    
    Serial.println(buffer);
  //}

  //while(digitalRead(requestPin) == HIGH);


//  Serial.println(counter);
//  //Serial.println("321");
//  
 // delay(500);
}


void serialEvent() {
  Serial.read();
  positionX = 0;
  positionY = 0;
  positionAngle = 0;
}

void blinkL()                                            
{
  if(digitalRead(left1Pin) == HIGH && digitalRead(left2Pin) == LOW) {
    counterL--;
  } else { // if(digitalRead(left1Pin) == HIGH && digitalRead(left2Pin) == LOW) {
    counterL++;
  }
}

void blinkR()                                            
{
  if(digitalRead(right1Pin) == HIGH && digitalRead(right2Pin) == LOW) {
    counterR--;
  } else { //if(digitalRead(right1Pin) == HIGH && digitalRead(right2Pin) == LOW) {
    counterR++;
  }
}


void calculate(long left, long right) {
  double leftMM = (left / (encoderResolution * gearRatio)) * PI * wheelDiameter;
  double rightMM = (right / (encoderResolution * gearRatio)) * PI * wheelDiameter;
  
  double angle = (leftMM - rightMM) / wheelsDistance;
  double distance = (rightMM + leftMM) / 2;
  double x = distance * sin(positionAngle + angle);
  double y = distance * cos(positionAngle + angle);
  
  positionX += x;
  positionY += y;
  positionAngle += angle;
  if (positionAngle > PI) {
    positionAngle -= 2 * PI;
  }
  if (positionAngle < (-PI)) {
    positionAngle += 2 * PI;
  }
}
