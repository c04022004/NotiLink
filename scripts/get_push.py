#!/usr/bin/env python

from pushbullet import Pushbullet

pb = Pushbullet("YOUR API KEY HERE")
pushes = pb.get_pushes()
print pushes[0]["body"]
