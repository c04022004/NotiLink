#!/usr/bin/env python

import sys
from pushbullet import Pushbullet

pb = Pushbullet("YOUR API KEY HERE")
push = pb.push_note("NotiLink System", sys.argv[1])