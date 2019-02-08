#!/usr/bin/env python3
# ******************************************************************************
# ****         D A O   I N S T R U M E N T A T I O N   G R O U P           *****
# *
# * (c) 2019                               (c) 2019
# * National Research Council              Conseil national de recherches
# * Ottawa, Canada, K1A 0R6                Ottawa, Canada, K1A 0R6
# * All rights reserved                    Tous droits reserves
# *
# * NRC disclaims any warranties,          Le CNRC denie toute garantie
# * expressed, implied, or statutory, of   enoncee, implicite ou legale, de
# * any kind with respect to the soft-     quelque nature que se soit, concer-
# * ware, including without limitation     nant le logiciel, y compris sans
# * any warranty of merchantability or     restriction toute garantie de valeur
# * fitness for a particular purpose.      marchande u de pertinence pour un
# * NRC shall not be liable in any event   usage particulier. Le CNRC ne pourra
# * for any damages, whether direct or     en aucun cas etre tenu responsable
# * indirect, special or general, conse-   de tout dommage, direct ou indirect,
# * quential or incidental, arising from   particulier ou general, accessoire
# * the use of the software.               ou fortuit, resultant de l'utili-
# *                                        sation du logiciel.
# *
# *****************************************************************************

"""Generate diagram of relationships stored in TMT software ICD database"""

import argparse
from graphviz import Digraph
from pymongo import MongoClient
import re
import sys

# plotting defaults
subsystem_colours = {
    'aoesw'  : 'green4',
    'nfiraos': 'springgreen',
    'tcs'    : 'purple',
    'iris'   : 'blue'
}

cmdcol = 'chocolate'
nocmdcol = 'red'
evcol = 'dimgrey'
nocmdcol = 'red'

layout = 'dot'#'fdp'#'twopi'#'neato'#'circo'#'dot'

# globals
prefix_dict = {}
all_prefixes = set()
pub_dict = {
    'events':{},
    'telemetry':{},
    'alarms':{}
    }
sub_dict = {}
cmd_comp_dict = {}  # commands received/sent by component
cmd_dict = {}       # mapping of all commands to the components that receive them

cmd_pairs = {}        # each entry points to list of events between each unique publisher&receiver
ev_pairs = {}         # each entry points to list of events between each unique publisher&receiver
all_nodes = set()     # a set of all nodes that will be plotted
primary_nodes = set() # nodes for which full information is provided
cmd_no_sender = {}    # dictionary using prefix as key for commands that nobody sends
ev_no_publisher = {}  # dictionary using prefix as key for events component subscribes to, nobody publishes

group_subsystem = True
show_missing_events = True
show_missing_commands = False

show_command_labels = False
show_event_labels = False

# Define components explicitly
#components = ['iris.oiwfs.poa','iris.oiwfs.adc','iris.oiwfs.detector','iris.rotator','iris.imager.odgw']
components = ['nfiraos.rtc']
subsystems = None

# Or select all components in subsystems
#subsystems = set(['tcs'])


def prefix(subsystem,component):
    # return prefix given subsystem and component
    pref = None
    failed = None
    if subsystem not in prefix_dict:
        failed = "Error: don't know subsystem "+subsystem
    elif component not in prefix_dict[subsystem]:
        failed = "Error: "+component+" not in subsystem "+subsystem
    else:
         pref = prefix_dict[subsystem][component]
    return pref,failed

# read database
def read_database():
    client = MongoClient()
    db = client['icds']
    collection_names = set(db.list_collection_names())

    # Dictionary maps subsystem -> component -> prefix
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
            all_prefixes.add(component_prefix)
            #print("Found: ", subsystem, component, component_prefix)

    

    # Dictionary of all events, telemetry, alarms pointing to components that publish them
    for name in collection_names:
        match = re.match('(\w+)\.(.*)\.publish',name)
        if match:
            subsystem=match.group(1)
            component=match.group(2)
            p,failed = prefix(subsystem,component)
            if failed:
                print('pub events',name,failed)
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
    for name in collection_names:
        match = re.match('(\w+)\.(.*)\.subscribe',name)
        if match:
            subsystem=match.group(1)
            component=match.group(2)
            p,failed = prefix(subsystem,component)
            if failed:
                print('sub events',name,failed)
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
                            pubprefix,failed = prefix(item['subsystem'],item['component'])
                            if failed:
                                # Can't identify the publisher
                                print("event not published for",p,failed)
                                pubprefix = item['subsystem']+'.'+item['component']
                            sub_dict[p][subtype].add(pubprefix+'.'+item['name'])
                            #print(p,"subscribes to",pubprefix+'.'+item['name'])

    # Dictionaries containing mapping of components->commands received and sent, and commands->components that receive
    for name in collection_names:
        match = re.match('(\w+)\.(.*)\.command',name)
        if match:
            subsystem=match.group(1)
            component=match.group(2)
            p,failed = prefix(subsystem,component)
            if failed:
                print('commands',name,failed)
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
                            cmdprefix,failed = prefix(item['subsystem'],item['component'])
                        else:
                            cmdprefix = p

                        if not cmdprefix:
                            continue
                        itemName = cmdprefix+'.'+item['name']

                        if cmdtype == 'receive':
                            cmd_dict[itemName] = p

                        cmd_comp_dict[p][cmdtype].add(itemName)
                        

# Define individual nodes within subsystems
def define_nodes(g, nodes, col):
    for node in nodes:
            # Create nodes for components 
            parts = node.split('.')  
            component = '.'.join(parts[1:])
            if shortlabel:
                label = component
            else:
                label = node
            if node in primary_nodes:
                style='bold'
            else:
                style='dashed'
            g.node(node,label,fontcolor=col,color=col,style=style)

            # Create dummy node for commands nobody sends
            if show_missing_commands and node in cmd_no_sender:
                g.node(node+'.cmd_no_sender','?',fontcolor='red',color='red')

            # Create dummy node for required events nobody sends
            if show_missing_events and node in ev_no_publisher:
                g.node(node+'.ev_no_publisher','?',fontcolor='red',color='red')


# Entrypoint -----------------------------------------------------------------

if __name__ == '__main__':

    read_database()

    dot = Digraph()
    dot.graph_attr['layout']=layout
    #dot.graph_attr['sep']='+20'
    dot.graph_attr['ratio']='0.5'
    dot.node_attr['fontsize']='20'
    dot.edge_attr['fontsize']='10'

    if subsystems:
        components = set()
        for p in all_prefixes:
            subsystem = p.split('.')[0]
            if subsystem in subsystems:
                components.add(p)

    for p in components:
        if p not in all_prefixes:
            print("Error: don't know",p)
            continue
        all_nodes.add(p)
        primary_nodes.add(p)

        #-----------------------------------------------------------------------------

        cmds_sent = set()

        # edges for all commands sent to component
        for comp in cmd_comp_dict:
            if 'send' in cmd_comp_dict[comp]:
                for cmd in cmd_comp_dict[comp]['send']:
                    if (cmd in cmd_dict) and (cmd_dict[cmd] == p):
                        c_name = cmd.split('.')[-1]
                        pair = comp+','+p
                        if pair not in cmd_pairs:
                            cmd_pairs[pair] = []
                        cmd_pairs[pair].append(c_name)
                        all_nodes.add(comp)
                        cmds_sent.add(cmd)
                        #dot.edge(comp,p,label=c_name)

        # identify commands that no one sends to this component
        if p in cmd_comp_dict and 'receive' in cmd_comp_dict[p]:
            recv = set(cmd_comp_dict[p]['receive'])
            diff = recv.difference(cmds_sent)
            if diff:
                cmd_no_sender[p] = set()
                for cmd in diff:
                    name = cmd.split('.')[-1]
                    cmd_no_sender[p].add(name)

        # edges for all commands sent from component
        if (p in cmd_comp_dict) and ('send' in cmd_comp_dict[p]):
            for cmd in cmd_comp_dict[p]['send']:
                if cmd in cmd_dict:
                    c_name = cmd.split('.')[-1]
                    pair = p+','+cmd_dict[cmd]
                    if pair not in cmd_pairs:
                        cmd_pairs[pair] = []
                    cmd_pairs[pair].append(c_name)
                    all_nodes.add(cmd_dict[cmd])
                    #dot.edge(p,cmd_dict[cmd],label=c_name)
                else:
                    print('Error: command',cmd,'not in cmd_dict')

        #-----------------------------------------------------------------------------

        # events that other components subscribe to from this component
        for comp in sub_dict:
            if 'events' in sub_dict[comp]:
                for ev in sub_dict[comp]['events']:
                    if (ev in pub_dict['events']) and (pub_dict['events'][ev] == p):
                        e_name = ev.split('.')[-1]
                        pair = p+','+comp
                        if pair not in ev_pairs:
                            ev_pairs[pair] = []
                        ev_pairs[pair].append(e_name)
                        all_nodes.add(comp)
                        #dot.edge(p,comp,label=e_name)

        # events that this component subscrbes to
        if p in sub_dict and 'events' in sub_dict[p]:
            for ev in sub_dict[p]['events']:
                if ev in pub_dict['events']:
                    publisher = pub_dict['events'][ev]
                    if publisher in all_prefixes:
                        e_name = ev.split('.')[-1]
                        pair = publisher+','+p
                        if pair not in ev_pairs:
                            ev_pairs[pair] = []
                        ev_pairs[pair].append(e_name)
                        all_nodes.add(publisher)
                        #dot.edge(publisher,p,label=e_name)
                else:
                    # identify required events that no one publishes
                    if p not in ev_no_publisher:
                        ev_no_publisher[p] = set()
                    name = ev.split('.')[-1]
                    ev_no_publisher[p].add(name)

    # ----------------------------------------------------------------------------


    # from all_nodes create a dictionary of nodes in each subsystem
    all_subsystems = {}
    for node in all_nodes:
        subsystem=node.split('.')[0]
        if subsystem not in all_subsystems:
            all_subsystems[subsystem] = set()
        all_subsystems[subsystem].add(node)

    for subsystem,nodes in all_subsystems.items():
        if subsystem in subsystem_colours:
            col = subsystem_colours[subsystem]
            shortlabel = True
        else:
            col = 'grey'
            shortlabel = False

        if group_subsystem:
            # Group nodes by subsystem
            with dot.subgraph(name='cluster_'+subsystem) as c:
                c.attr(label=subsystem)
                c.attr(color=col)
                c.attr(fontcolor=col)
                c.attr(fontsize='30')
                c.attr(style='rounded')
                c.attr(penwidth='3')
                c.attr(labelloc='b')

                define_nodes(c,nodes,col)
        else:
            # No grouping
            define_nodes(dot, nodes, col)
            

    # One edge for each unique command sender,receiver listing all commands in label
    dot.attr('edge',fontcolor=cmdcol)
    dot.attr('edge',color=cmdcol)
    for pair,cmds in cmd_pairs.items():
        sender,receiver = pair.split(',')
        if show_command_labels:
            cmd_str = '\n'.join(sorted(cmds))
        else:
            cmd_str = None
        dot.edge(sender,receiver,label=cmd_str)

    # One edge showing all commands nobody sends to each component
    if show_missing_commands:
        dot.attr('edge',fontcolor=nocmdcol)
        dot.attr('edge',color=nocmdcol)
        for p,cmds in cmd_no_sender.items():
            if show_command_labels:
                cmd_str = '\n'.join(cmds)
            else:
                cmd_str = None
            dot.edge(p+'.cmd_no_sender',p,label=cmd_str)

    # One edge for each unique event publisher,receiver listing all items as the label
    dot.attr('edge',fontcolor=evcol)
    dot.attr('edge',color=evcol)
    #dot.attr('edge',style='dotted')
    for pair,events in ev_pairs.items():
        publisher,receiver = pair.split(',')
        if show_event_labels:
            ev_str = '\n'.join(sorted(events))
        else:
            ev_str = None
        dot.edge(publisher,receiver,label=ev_str)

    # One edge showing all events components need but nobody publishes
    if show_missing_events:
        dot.attr('edge',fontcolor=nocmdcol)
        dot.attr('edge',color=nocmdcol)
        for p,evs in ev_no_publisher.items():
            if show_event_labels:
                ev_str = '\n'.join(evs)
            else:
                ev_str = None
            dot.edge(p+'.ev_no_publisher',p,label=ev_str,style='dashed')

    # Render the diagram
    #print(dot.source)
    dot.render(view=True)