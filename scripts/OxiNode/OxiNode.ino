
#define OXI_VERSION "v1.00"

//These are the libraries included by the nodemcu board 
#include <ESP8266WiFi.h>
#include <ESP8266WiFiMulti.h>
ESP8266WiFiMulti WiFiMulti;

//Libraries required to drive the oled display
#include <Wire.h>
#include "OLED.h"
OLED display(D2, D1);//Usage: display(SDA, SCL);

void setup() {
  pinMode(D6,INPUT_PULLUP);
  Serial.begin(115200);
  delay(10);
  display.begin();
  display.print(String("OxiNode ")+OXI_VERSION,1);
  display.print("Connecting...   ",3);
  WiFiMulti.addAP("oxipie", "pipipipi");
  while(WiFiMulti.run() != WL_CONNECTED) {
        Serial.print(".");
        delay(500);}
  Serial.println("connected: "+IpAddress2String(WiFi.localIP()));
  display.print("Connected:      ",4);
  display.print(IpAddress2String(WiFi.localIP()),5);
  delay(5*1000);

  // faux message
  serverUpdate("Smoke detected");
}

void loop() {
  // connect pin D6 to ground to trigger
  if(digitalRead(D6)==LOW){
    serverUpdate("doorbell");
  }
  delay(100);
}

String IpAddress2String(const IPAddress& ipAddress){
  return String(ipAddress[0]) + String(".") +\
  String(ipAddress[1]) + String(".") +\
  String(ipAddress[2]) + String(".") +\
  String(ipAddress[3])  ; 
}

// this will send a message to the server pi
void serverUpdate(const String& message){
    const uint16_t port = 9080;
    const char * host = "192.168.1.129"; // ip or dns
    
    Serial.print("connecting to ");
    Serial.println(host);

    // Use WiFiClient class to create TCP connections
    WiFiClient client;

    if (!client.connect(host, port)) {
        Serial.println("connection failed");
        Serial.println("wait 5 sec...");
        delay(5000);
        return;
    }

    // This will send the request to the server
    client.print(message+"\r\n");
    display.print("uploading",7);

    //read back one line from server
    String line = client.readStringUntil('\r');
    Serial.println(line);

    Serial.println("closing connection");
    client.stop();

    delay(1000);
    display.print("                ",7);
}


