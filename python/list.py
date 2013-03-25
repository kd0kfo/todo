#!/usr/bin/env python
import json
from sys import argv

if len(argv) == 1:
    print("Usage: list.py <file name>")
    exit(1)

fn = argv[1]

todo = json.loads(open(fn,"r").read())
counter = 1
for item in todo:
    print("{0}: {1}".format(counter,item["message"]))
    counter += 1
