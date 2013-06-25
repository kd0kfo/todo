#!/usr/bin/env python
import json
from sys import argv
from getopt import getopt

class TKParent():
    def __init__(self,filename):
        self.dialog = None
        self.todolist = None
        self.filename = filename
        self.categories = []
        self.cat_list = None
        
    def do_refresh(self,category=None):
        from Tkinter import END
        if not self.todolist:
            return
        self.categories = []
        todo = json.loads(open(self.filename,"r").read())
        self.todolist.delete(0,END)
        for item in todo:
            curr_category = None
            text = item["message"]
            if "category" in item and item["category"]:
                curr_category = item["category"]
                text  = "{0}: {1}".format(curr_category, text)
                if not curr_category in self.categories:
                    self.categories.append(curr_category)
            if category:
                if category == "NONE":
                    if curr_category and curr_category != "NONE":
                        continue
                elif category != curr_category:
                    continue
            self.todolist.insert(END,text)
        self.dialog.title("TODO ({0})".format(self.todolist.size()))

    def refresh_list(self):
        self.do_refresh()

    def filter_list(self,arg):
        if arg == "ALL":
            self.refresh_list()
        else:
            self.do_refresh(arg)
        
    def get_box(self):
        if not self.dialog:
            from Tkinter import Tk,Listbox,Button,Scrollbar,X,Y,BOTTOM,RIGHT,HORIZONTAL,VERTICAL,OptionMenu,StringVar
            self.dialog = Tk()
            self.dialog.title("TODO")
            scrollbar = Scrollbar(self.dialog,orient=HORIZONTAL)
            yscrollbar = Scrollbar(self.dialog,orient=VERTICAL)
            self.todolist = Listbox(self.dialog,width=50,xscrollcommand=scrollbar.set,yscrollcommand=yscrollbar.set)
            scrollbar.config(command=self.todolist.xview)
            scrollbar.pack(side=BOTTOM,fill=X)
            yscrollbar.config(command=self.todolist.yview)
            yscrollbar.pack(side=RIGHT,fill=Y)
            self.todolist.pack(side="left",fill="both",expand=True)
            
            cat_list_name = StringVar()
            cat_list_name.set("Category")
            

            btn = Button(self.dialog,text="Refresh",command=self.refresh_list)
            btn.pack()
            
            self.refresh_list()
            if self.categories:
                self.cat_list = OptionMenu(self.dialog,cat_list_name,*(self.categories+["ALL","NONE"]),command=self.filter_list)
                self.cat_list.pack()
        
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
