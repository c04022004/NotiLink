#!/usr/bin/env python

from sense_hat import SenseHat
import time
import numled
import sys

sense = SenseHat()

w = (150, 150, 150)
image = [
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w,
w,w,w,w,w,w,w,w
]


for i in range(0, 10):
	sense.set_pixels(image)
	time.sleep(0.5)
	sense.clear()
	time.sleep(0.5)

