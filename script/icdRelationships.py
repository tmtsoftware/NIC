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
                            sub_dict[p][subtype] = set()
                        pubprefix = prefix(item['subsystem'],item['component'])
                        if not pubprefix:
                            continue
                        sub_dict[p][subtype].add(pubprefix+'.'+item['name'])
                        #print(p,"subscribes to",pubprefix+'.'+item['name'])

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

                    if cmdtype == 'send':
                        cmdprefix = prefix(item['subsystem'],item['component'])
                    else:
                        cmdprefix = p

                    itemName = cmdprefix+'.'+item['name']

                    if cmdtype == 'receive':
                        cmd_dict[itemName] = p

                    cmd_comp_dict[p][cmdtype].add(itemName)
                    


# Make a plot
dot = Digraph()
dot.graph_attr['layout']='fdp'#'twopi'#'neato'#'circo'
dot.graph_attr['sep']='+20'
dot.graph_attr['ratio']='1'
dot.edge_attr['fontsize']='10'


subsystem = 'NFIRAOS'
component = 'rtcRole'

#subsystem = 'IRIS'
#component = 'oiwfs-poa-assembly'

p = prefix(subsystem,component)
dot.node(p)

# edges for all commands sent to component
for comp in cmd_comp_dict:
    if 'send' in cmd_comp_dict[comp]:
        for cmd in cmd_comp_dict[comp]['send']:
            if (cmd in cmd_dict) and (cmd_dict[cmd] == p):
                c_name = cmd.split('.')[-1]
                dot.edge(comp,p,label=c_name,color='black')

# attributes for events
dot.attr('edge',fontcolor='blue')
dot.attr('edge',color='blue')

# edges for events that other components subscribe to from this component
for comp in sub_dict:
    if 'events' in sub_dict[comp]:
        for ev in sub_dict[comp]['events']:
            if (ev in pub_dict['events']) and (pub_dict['events'][ev] == p):
                e_name = ev.split('.')[-1]
                dot.edge(p,comp,label=e_name)

# edges for events that this component subscrbes to
if p in sub_dict and 'events' in sub_dict[p]:
    for ev in sub_dict[p]['events']:
        if ev in pub_dict['events']:
            publisher = pub_dict['events'][ev]
            e_name = ev.split('.')[-1]
            dot.edge(publisher,p,label=e_name)

print(dot.source)
dot.render(view=True)
