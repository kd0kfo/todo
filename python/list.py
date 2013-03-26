#!/usr/bin/env python
import json
from sys import argv
from getopt import getopt

(opts, args) = getopt(argv[1:], "",["cli"])

if len(args) != 1:
    print("Usage: list.py [--cli] <file name>")
    exit(1)

use_cli = False
fn = args[0]

for (opt,optarg) in opts:
    while opt[0] == '-':
        opt = opt[1:]
    if opt == "cli":
        use_cli = True

todo = json.loads(open(fn,"r").read())

if use_cli:
    counter = 1
    for item in todo:
        print("{0}: {1}".format(counter,item["message"]))
        counter += 1
else:
    from Tkinter import *
    dialog = Tk()
    dialog.title("TODO")
    todolist = Listbox(dialog)
    todolist.pack()

    for item in todo:
        todolist.insert(END,item["message"])
    dialog.mainloop()
