#!/usr/bin/env python

import json
from sense_hat import SenseHat

sense = SenseHat()
temperature = sense.get_temperature()
print temperature
humidity = sense.get_humidity()
print humidity
