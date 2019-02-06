#!/usr/bin/env python3
#
# Python script that illustrates ICD relationships using dot diagrams

from graphviz import Digraph
from pymongo import MongoClient
import re
import sys

client = MongoClient()
db = client['icds']
collection_names = set(db.list_collection_names())

# Dictionary maps subsystem -> component -> prefix
prefix_dict = {}
for name in collection_names:
    match = re.match('(\w+)\.(.*)\.component',name)
    if match:
        subsystem=match.group(1)
        component=match.group(2)

        collection = db[name]
        cursor = collection.find()
        component_prefix = cursor.next()['prefix']

        if subsystem not in prefix_dict:
            prefix_dict[subsystem] = {}

        prefix_dict[subsystem][component] = component_prefix
        #print("Found: ", subsystem, component, component_prefix)

def prefix(subsystem,component):
    # return prefix given subsystem and component
    retval = None
    if subsystem not in prefix_dict:
        print("Error: don't know subsystem",subsystem)
    elif component not in prefix_dict[subsystem]:
        print("Error: ",component,"not in subsystem",subsystem)
    else:
         retval = prefix_dict[subsystem][component]
    return retval
    

# Dictionary of all events, telemetry, alarms pointing to components that publish them
pub_dict = {
    'events':{},
    'telemetry':{},
    'alarms':{}
    }

for name in collection_names:
    match = re.match('(\w+)\.(.*)\.publish',name)
    if match:
        subsystem=match.group(1)
        component=match.group(2)
        p = prefix(subsystem,component)
        if not p:
            continue
        
        collection = db[name]
        cursor = collection.find()
        d = cursor.next()
        if 'publish' in d:
            publish = d['publish']

            for pubtype in ['events','telemetry','alarms']:
                if pubtype in publish:
                    for item in publish[pubtype]:
                        pub_dict[pubtype][p+'.'+item['name']] = p
                    
# Dictionary of all events, telemetry subscribed to by each component
sub_dict = {}
for name in collection_names:
    match = re.match('(\w+)\.(.*)\.subscribe',name)
    if match:
        subsystem=match.group(1)
        component=match.group(2)
        p = prefix(subsystem,component)
        if not p:
            continue
        
        collection = db[name]
        cursor = collection.find()
        d = cursor.next()
        if 'subscribe' in d:
            subscribe = d['subscribe']

            for subtype in ['events','telemetry']:
                if subtype in subscribe:
                    for item in subscribe[subtype]:
                        if p not in sub_dict:
                            sub_dict[p] = {}
                        if subtype not in sub_dict[p]:
                            sub_dict[p][subtype] = {}

                        sub_dict[p][subtype] = p+'.'+item['name']

# Dictionaries containing mapping of components->commands received and sent, and commands->components that receive
cmd_comp_dict = {}  # commands received/sent by component
cmd_dict = {}       # mapping of all commands to the components that receive them
for name in collection_names:
    match = re.match('(\w+)\.(.*)\.command',name)
    if match:
        subsystem=match.group(1)
        component=match.group(2)
        p = prefix(subsystem,component)
        if not p:
            continue
        
        collection = db[name]
        cursor = collection.find()
        command = cursor.next()
    
        for cmdtype in ['send','receive']:
            if cmdtype in command:
                for item in command[cmdtype]:
                    if p not in cmd_comp_dict:
                        cmd_comp_dict[p] = {}
                    if cmdtype not in cmd_comp_dict[p]:
                        cmd_comp_dict[p][cmdtype] = set()

                    itemName = p+'.'+item['name']
                    cmd_comp_dict[p][cmdtype].add(itemName)
                    cmd_dict[itemName] = p
