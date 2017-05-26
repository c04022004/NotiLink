#!/usr/bin/env python

from sense_hat import SenseHat
import time
import numled
import sys

sense = SenseHat()
c = (0, 100, 0)

if(int(sys.argv[1])):
	c = (255, 0, 0)
	image = [
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c,
	c,c,c,c,c,c,c,c
	]
	sense.set_pixels(image)
	time.sleep(0.5)
	sense.clear()
	time.sleep(0.1)

numled.show_number(sense, int(sys.argv[1]), c)
time.sleep(2)
