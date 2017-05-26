#!/usr/bin/env python

import sys
from pushbullet import Pushbullet

pb = Pushbullet("YOUR API KEY HERE")
devi = pb.get_device("YOUR DEVICE NAME HERE")
push = pb.push_sms(devi, "YOUR EMERGENCY CONTACT", sys.argv[1])