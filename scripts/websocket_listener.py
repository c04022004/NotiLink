#!/usr/bin/env python
import ConfigParser
import time
import json
import os
import requests
import subprocess
import websocket

def on_error(ws, error):
  print error

def on_close(ws):
  print "connection closed"

def on_message(ws, message):
  print message
  message = json.loads(message)
    
if __name__ == "__main__":
  # websocket.enableTrace(True)   
  ws = websocket.WebSocketApp("wss://stream.pushbullet.com/websocket/YOUR_API_KEY_HERE",
                              on_message=on_message,
                              on_error=on_error,
                              on_close=on_close)
    
  ws.run_forever()
