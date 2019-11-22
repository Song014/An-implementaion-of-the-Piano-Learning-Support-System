//
// Bit 7~1: Led number
// Bit 0: On/Off
//
//

#include <SoftwareSerial.h>
#include <Adafruit_NeoPixel.h>

SoftwareSerial bluetoothSerial(3, 2); // RX, TX

#define PIN        6
#define NUMPIXELS 80    // Led 개수.
Adafruit_NeoPixel pixels(NUMPIXELS, PIN, NEO_GRB + NEO_KHZ800);

//
// 초기화 함수.
//
void setup()
{
  // Open serial communications and wait for port to open:
  Serial.begin(9600);

  // set the data rate for the SoftwareSerial port
  bluetoothSerial.begin(9600);

  // Led strip
  pixels.begin();
  pixels.clear();
  pixels.show();
  delay(10);
}

uint8_t bluetoothInput = 0;
uint8_t ledNumber = 0;
uint8_t onOff = 0;

//
// 메인 함수.
//
void loop()
{
  // Bluetooth input check
  if (bluetoothSerial.available())
  {
    bluetoothInput = bluetoothSerial.read();
    Serial.println(bluetoothInput);

    // 수신한 블루투스 데이터에서 Led 번호와 on/off 신호 파싱.
    ledNumber = (bluetoothInput >> 1);
    onOff = (bluetoothInput & 0x01);

    // Led control
    LedOnOff(ledNumber, onOff);
  }
}

//
// Led on/off 제어 함수.
//
void LedOnOff(uint8_t id, uint8_t onOff)
{
  if(id == 100)
  {
    pixels.clear();
  }
  else if(id == 101)
  {
    pixels.show();
    delay(50);
  }
  else
  {
    if(onOff == 1)
    {
      if(id > 46)
      {
        pixels.setPixelColor(id, pixels.Color(151, 117, 0));
      }
      else
      {
        pixels.setPixelColor(id, pixels.Color(0, 198, 109));
      }
    }
    else if(onOff == 0)
    {
      pixels.setPixelColor(id, pixels.Color(0, 0, 0));   // R, G, B
    }
  }
}


//
// Led strip 테스트 함수.
//
void LedStripTest()
{
  while(1)
  {
    for(int i = 0; i < NUMPIXELS; i++)
    {
      pixels.clear();
      pixels.setPixelColor(i, pixels.Color(151, 117, 0));
      pixels.show();
      delay(500);
    }
  }
}

//
// 블루투스 테스트 함수.
//
void BluetoothTest()
{
  if (bluetoothSerial.available())
  {
    Serial.write(bluetoothSerial.read());
  }
    
  if (Serial.available())
  {
    bluetoothSerial.write(Serial.read());
  }
}
