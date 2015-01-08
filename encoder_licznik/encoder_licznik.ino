// http://majsterkowo.pl/forum/przerwania-dlaczego-jest-to-proste-t984.html

int requestPin = 6; // pojawienie się stanu niskiego oznacza żądanie przesłania pozycji robota

int rightPin = 4; // drugi pin prawego enkodera, pierwszy podłączony do przerwania int.0 - pin 2
int leftPin = 5; // drugi pin lewego enkodera, pierwszy podłączony do przerwania int.1 - pin 3

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
  
  pinMode(leftPin, INPUT);
  pinMode(rightPin, INPUT);

  
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
  if(digitalRead(leftPin) == HIGH) {
    counterL--;
  } else {
    counterL++;
  }
}

void blinkR()                                            
{
  if(digitalRead(rightPin) == HIGH) {
    counterR--;
  } else {
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
