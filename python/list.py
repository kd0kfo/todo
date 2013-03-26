#!/usr/bin/env python
import json
from sys import argv
from getopt import getopt

class TKParent():
    def __init__(self,filename):
        self.dialog = None
        self.todolist = None
        self.filename = filename
        
    def refresh_list(self):
        from Tkinter import END
        if not self.todolist:
            return
        todo = json.loads(open(self.filename,"r").read())
        self.todolist.delete(0,END)
        for item in todo:
            self.todolist.insert(END,item["message"])

    def get_box(self):
        if not self.dialog:
            from Tkinter import Tk,Listbox,Button,Scrollbar,X,BOTTOM,HORIZONTAL
            self.dialog = Tk()
            self.dialog.title("TODO")
            scrollbar = Scrollbar(self.dialog,orient=HORIZONTAL)
            self.todolist = Listbox(self.dialog,width=50,xscrollcommand=scrollbar.set)
            scrollbar.config(command=self.todolist.xview)
            scrollbar.pack(side=BOTTOM,fill=X)
            self.todolist.pack(side="left",fill="both",expand=True)
            
            btn = Button(self.dialog,text="Refresh",command=self.refresh_list)
            btn.pack()
            
            self.refresh_list()
        
        return (self.dialog,self.todolist)

    def add(self,msg):
        from Tkinter import END
        self.todolist.insert(END,msg)

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

if use_cli:
    todo = json.loads(open(fn,"r").read())
    counter = 1
    for item in todo:
        print("{0}: {1}".format(counter,item["message"]))
        counter += 1
else:
    from Tkinter import END
    parent = TKParent(fn)
    (dialog,todolist) = parent.get_box()

    dialog.mainloop()
