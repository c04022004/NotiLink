# NotiLink
An integrated alert system using Raspberry Pi(s).

## Origin
This is the individual project in my university course [ELEC3542](https://elec3542.github.io). Basically it's a IoT application using the existing framework (PushBullet API) to monitor home safety. Raspberry Pi and NodeMCU are the main hardware in use.

Due to time constrains, the prototype works but the code quality is really bad. Lots of shell command is present in the java to offlaod the work to seperate python scripts. The configuration variables scatter across the whole workspace. Hoping to refactor stuffs when I have some spare time.

Some part of the code may come from the lecture materials. Hope it will not generate any copyright issues.

## Hardware/software configuration
This repo needs a Raspberry Pi with a working linux OS with GUI, and configured to be a WiFi Access Point (to work stanalone). Setup the network of Raspberry Pi(s) with consistant IP assigned to each of them. Compile the source code wih the json library achieved (`cd src/ &javac -cp ./json-20170516.jar:. *.java`). For the server node, start `src/NotifyGUI` on the Raspberry Pi with display and `src/DisplayNode` on ones with SenseHat.

For the NodeMCU, load `script/OxiNode/OxiNode.ino` into the NodeMCU via the arduino IDE. It takes a digital signal from `pin 6` as doorbell trigger. Of course you can adopt it to any digital/analog sensors and customise the alert message.

## Demo
<img src="https://github.com/c04022004/NotiLink/blob/master/docs/demo.jpg?raw=true" width="90%">

<img src="https://github.com/c04022004/NotiLink/blob/master/docs/oxinode.jpg?raw=true" width="90%">


## Code summary
- `src/Device.java`: Store device types
- `src/DisplayNode.java`: Trigger scripts to set SenseHat LED on messaged recieved
- `src/MessageStatus.java`: Store message types
- `src/NotifyGUI.java`: Main screen that the Raspberry Pi interact with user
- `src/PushbulletLinstener.java`: Dirty hacks to run WebSocket without using libraries in java
- `scripts/blink.py`: Flashes the onboard LED at max brightness
- `scripts/font3x5.py`: Bitmap font for numbers (from ELEC3542 material)
- `scripts/get_push.py`: Get new PushBullet messages using python library
- `scripts/get_tmp.py`: Get new sensor data from SenseHat using python library
- `scripts/numled.py`: Set LED form bitmap (from ELEC3542 material)
- `scripts/send_sms.py`: Send SMS via PushBullet api using python library
- `scripts/send_numled.py`: Wrap numled() for java shell script call
- `scripts/steam_push.py`: Send PushBullet messages using python library
- `scripts/websocket_listener.py`: Listen to Pushbullet stream using WebSocket 
- `scripts/OxiNode/OxiNode.ino`: Arduino program to drive a NodeMCU with WiFi connection

