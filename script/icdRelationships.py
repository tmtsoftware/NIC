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
    'nfiraos': 'green4',
    'aoesw'  : 'springgreen',
    'tcs'    : 'purple',
    'iris'   : 'blue',
    'others' : 'grey'    # colour used for remaining subsystems
}
cmdcol = 'chocolate'     # command colour
nocmdcol = 'red'         # missing command colour
evcol = 'dimgrey'        # event colours
noevcol = 'red'          # missing event colour

possible_layouts = ['dot','fdp','sfdp','twopi','neato','circo','patchwork']
layout = 'dot'
ratio = '0.5'
node_fontsize = '20'
edge_fontsize = '10'
subsystem_fontsize = '30'
groupsubsystems = True
showplot = True
imagefile = None
dotfile = None
missingevents = True    # plot missing events?
missingcommands = False # plot missing commands?
commandlabels = False   # show command labels?
eventlabels = True      # show event labels?
splines = True          # use splines for edges?
overlap = 'scale'
possible_types = ['HCD', 'Assembly', 'Sequencer', 'Application']
omittypes = set(['HCD'])

# suffixes for dummy nodes
suffix_nocmd = '.cmd_no_sender'
suffix_noev = '.ev_no_publisher'

# information parsed from database in read_database()
prefix_dict = {}      # map subsystem->component->prefix
all_prefixes = set()  # all component prefixes from the database
pub_dict = {          # mapping from events,telemetry,alarms->producers
    'events':{},
    'telemetry':{},
    'alarms':{}
    }
sub_dict = {}         # mapping from component prefixes->all subscribed items
cmd_comp_dict = {}    # commands received/sent by component
cmd_dict = {}         # mapping of all commands to the components that receive them

# global information built when preparing for plot
cmd_pairs = {}        # each entry points to list of events between each unique publisher&receiver
ev_pairs = {}         # each entry points to list of events between each unique publisher&receiver
all_nodes = set()     # a set of all nodes that will be plotted
primary_nodes = set() # nodes for which full information is provided
cmd_no_sender = {}    # dictionary using prefix as key for commands that nobody sends
ev_no_publisher = {}  # dictionary using prefix as key for events component subscribes to, nobody publishes
component_types = {}  # dictionary using prefix as key for component types

# *****************************************************************************
def prefix(subsystem,component):
    """ Establish prefix given subsystem and component

    Model files specify subsystem and component names. However,
    these do not necessarily need to match the prefix
    of a component. That mapping is provided by the component-model.conf
    which specifies the component prefix. This routine makes
    use of the global prefix_dict to get the prefix

    Args:
        subsystem (str): subsystem name from model files
        component (str): component name from model files

    Returns:
        pref (str): The prefix if successful, error message if it failed
        failed (bool): Flag is True if it failed
    """
    pref = None
    failed = None
    if subsystem not in prefix_dict:
        failed = "Error: don't know subsystem "+subsystem
    elif component not in prefix_dict[subsystem]:
        failed = "Error: "+component+" not in subsystem "+subsystem
    else:
         pref = prefix_dict[subsystem][component]
    return pref,failed

# *****************************************************************************
# read database
def read_database():
    """ Read information from the database into globals

    Connect to the database and populate the following globals:
    prefix_dict, all_prefixes, pub_dict, sub_dict, cmd_comp_dict, cmd_dict
    """

    client = MongoClient()
    db = client['icds']

    # Each collection corresponds to an individual model file. The
    # name of each collection is of the form:
    #  (subsystem).(component).(modelfiletype)
    collection_names = set(db.list_collection_names())

    # Dictionary maps subsystem -> component -> prefix
    for name in collection_names:
        # We get the prefixes out of the component-model files
        match = re.match('(\w+)\.(.*)\.component',name)
        if match:
            subsystem=match.group(1)
            component=match.group(2)

            collection = db[name]
            cursor = collection.find()
            datadict = cursor.next()
            component_prefix = datadict['prefix']
            component_type = datadict['componentType'] #HCD, Assembly, Sequencer, Application

            if subsystem not in prefix_dict:
                prefix_dict[subsystem] = {}

            prefix_dict[subsystem][component] = component_prefix
            component_types[component_prefix] = component_type
            all_prefixes.add(component_prefix)
            #print("Found: ", subsystem, component, component_prefix)

    # Dictionary of all events, telemetry, alarms pointing to
    # components that publish them
    for name in collection_names:
        # Extract this information from the publish-model files
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
        # Extract this information from the subscribe-model files
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
        # Extract this information from the command-model files
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
                        
# *****************************************************************************
def define_nodes(g, nodes, col, shortlabel):
    """ Define nodes in a graph

    Add supplied nodes to the supplied graph.
    Primary nodes and secondary nodes have a different style.
    The subroutine will also create dummy nodes corresponding to
    any node that has missing commands/events.

    This routine may be called on a subgraph.

    Args:
        g (Digraph): graph object for which nodes are being defined
        nodes (iterable): list of node prefixes
        col (str): colour for the nodes
        shortlabel (bool): if true just component label instead of full prefix
    """
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
            if missingcommands and node in cmd_no_sender:
                g.node(node+suffix_nocmd,'?',fontcolor=nocmdcol,color=nocmdcol)

            # Create dummy node for required events nobody sends
            if missingevents and node in ev_no_publisher:
                g.node(node+suffix_noev,'?',fontcolor=noevcol,color=noevcol)

# *****************************************************************************
def str2bool(s):
    """Convert a string into a Bool

    Converts "true" , "y", "1" to True
             false" , "n", "0" to False

    Case is ignored.

    Args:
        s (str): Input string

    Returns:
        Bool: converted value
    """

    retval = None

    truestr = set(['true','y','1'])
    falsestr = set(['false','n','0'])
    
    if s.lower() in truestr:
        retval = True
    elif s.lower() in falsestr:
        retval = False
    else:
        raise Exception('Unable to convert "'+s+'" to a Boolean (try "True" or "False")')

    return retval

# *****************************************************************************
# Entrypoint

if __name__ == '__main__':

    # parse the command line
    parser = argparse.ArgumentParser(
        formatter_class=argparse.RawTextHelpFormatter,
        description="Use graphviz dot to graph relationships stored in TMT ICDDB",
        epilog="""All components specified through the --components or --subsystems arguments
are considered "primary" nodes and indicated with solid ovals. Any other
component that they communicate with is a "secondary" node, and will be shown
with a dashed oval.
        
The following colours are currently hard-coded:

  commands - %s
  events - %s
  missing commands - %s
  missing events - %s
%s
        
Examples:

# Plot all interfaces for a particular component to the screen,
# label events and commands, and show missing events and commands

icdRelationships.py --components iris.oiwfs.poa --missingcommands true \\
    --missingevents true --commandlabels true --eventlabels true

# Plot all interfaces for two components only to a file called graph.pdf

icdRelationships.py --components iris.oiwfs.poa,iris.rotator \\
    --imagefile graph --showplot false

# Plot all interfaces for multiple subsystems and one component from
# another subsystem to screen, with no missing events shown

icdRelationships.py --components iris.rotator --subsystems nfiraos,tcs \\
    --missingevents false

# Use "neato" layout to get a more readable graph with a lot of nodes
icdRelationships.py --subsystems iris --layout neato --eventlabels false \\
    --overlap false

""" % (cmdcol,evcol,nocmdcol,noevcol, \
    'subsystems:\n'+'\n'.join(['    '+k+' - '+v for k,v in subsystem_colours.items()])
    ))
    parser.add_argument("--components", type=str, nargs="?",
        help="Comma-separated list of primary component prefixes")
    parser.add_argument("--subsystems", type=str, nargs="?",
        help="Comma-separated list of subsystem prefixes")
    parser.add_argument("--showplot", default=str(showplot), nargs="?",
        help="Display plot in a window (default=%s)"%str(showplot) )
    parser.add_argument("--imagefile", default=imagefile, nargs="?",
        help="Write image to file (default=%s)"%str(imagefile) )
    parser.add_argument("--dotfile", default=dotfile, nargs="?",
        help="Write dot source to file (default=%s)"%str(dotfile) )
    parser.add_argument("--ratio", default=ratio, nargs="?",
        help="Image aspect ratio (y/x) (default=%s)"%ratio )
    parser.add_argument("--missingevents", default=str(missingevents), nargs="?",
        help="Plot missing events (default=%s)"%str(missingevents) )
    parser.add_argument("--missingcommands", default=str(missingcommands), nargs="?",
        help="Plot missing commands (default=%s)"%str(missingcommands) )
    parser.add_argument("--commandlabels", default=str(commandlabels), nargs="?",
        help="Plot command labels (default=%s)"%str(commandlabels) )
    parser.add_argument("--eventlabels", default=str(eventlabels), nargs="?",
        help="Plot event labels (default=%s)"%str(eventlabels) )
    parser.add_argument("--groupsubsystems", default=str(groupsubsystems), nargs="?",
        help="Group components from same subsystem together (default=%s)"%str(groupsubsystems) )
    parser.add_argument('--layout', default=layout, choices=possible_layouts, nargs="?",
        help="Dot layout engine (default=%s)"%layout)
    parser.add_argument('--overlap', default=overlap, choices=['true','false','scale'], nargs="?",
        help="Node overlap handling (default=%s)"%overlap)
    parser.add_argument("--splines", default=str(splines), nargs="?",
        help="Use splines for edges? (default=%s)"%str(splines) )
    parser.add_argument("--omittypes", default=','.join(omittypes), type=str, nargs="?",
        help="Comma-separated list of component types (%s) to omit as primaries (default=%s)" % \
            (','.join(possible_types),omittypes) ) 

    args = parser.parse_args()

    components = set()
    subsystems = set()
    
    if args.components:
        components = components.union(args.components.split(','))
    if args.subsystems:
        subsystems = subsystems.union(args.subsystems.split(','))
    showplot = str2bool(args.showplot)
    imagefile = args.imagefile
    dotfile = args.dotfile
    ratio=args.ratio
    missingevents = str2bool(args.missingevents)
    missingcommands = str2bool(args.missingcommands)
    commandlabels = str2bool(args.commandlabels)
    eventlabels = str2bool(args.eventlabels)
    groupsubsystems = str2bool(args.groupsubsystems)
    layout = args.layout
    overlap = args.overlap
    splines = args.splines
    if args.omittypes:
        omittypes = set(args.omittypes.split(','))

    if not components and not subsystems:
        print("Need to specify at least --components or --subsystems. For help:\n"+\
            "  icdRelationships.py -h")
        sys.exit(0)

    # Populate globals with information from the database
    read_database()

    # Add components from user-specified subsystems
    if subsystems:
        for p in all_prefixes:
            subsystem = p.split('.')[0]
            if subsystem in subsystems:
                components.add(p)

    # Remove primary components if they are in omittypes
    if omittypes:
        components = {c for c in components if component_types[c] not in omittypes}

    # ***************************************************************
    # Iterate over primary components and establish all of the 
    # relationships required to make the plots
    for p in components:
        if p not in all_prefixes:
            print("Error: don't know",p)
            continue
        all_nodes.add(p)
        primary_nodes.add(p)

        # *************************************************
        # edges for all commands sent to component
        cmds_sent = set()
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

        # *************************************************
        # identify commands that no one sends to this component
        if p in cmd_comp_dict and 'receive' in cmd_comp_dict[p]:
            recv = set(cmd_comp_dict[p]['receive'])
            diff = recv.difference(cmds_sent)
            if diff:
                cmd_no_sender[p] = set()
                for cmd in diff:
                    name = cmd.split('.')[-1]
                    cmd_no_sender[p].add(name)

        # *************************************************
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

        # *************************************************
        # events that other components subscribe to from
        # this component
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

        # *************************************************
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

    # ***************************************************************
    # from all_nodes create a dictionary of nodes in each subsystem
    all_subsystems = {}
    for node in all_nodes:
        subsystem=node.split('.')[0]
        if subsystem not in all_subsystems:
            all_subsystems[subsystem] = set()
        all_subsystems[subsystem].add(node)

    # ***************************************************************
    # Create the graph

    # Graph initialization
    dot = Digraph()
    dot.graph_attr['layout']=layout
    dot.graph_attr['splines']=splines
    dot.graph_attr['overlap']=overlap
    #dot.graph_attr['sep']='+20'
    dot.graph_attr['ratio']=ratio
    dot.node_attr['fontsize']=node_fontsize
    dot.edge_attr['fontsize']=edge_fontsize


    # Define all of the nodes
    for subsystem,nodes in all_subsystems.items():
        if subsystem in subsystem_colours:
            col = subsystem_colours[subsystem]
            shortlabel = True
        else:
            # use a single colour for subsystems
            # that don't have a specific colour,
            # and also revert to specifying
            # full prefix for nodes in those
            # subsystems instead of the short name
            col = subsystem_colours['others']
            shortlabel = False

        if groupsubsystems:
            # Group nodes by subsystem
            with dot.subgraph(name='cluster_'+subsystem) as c:
                c.attr(label=subsystem)
                c.attr(color=col)
                c.attr(fontcolor=col)
                c.attr(fontsize=subsystem_fontsize)
                c.attr(style='rounded')
                c.attr(penwidth='3')
                c.attr(labelloc='b')

                define_nodes(c,nodes,col,shortlabel)
        else:
            # No grouping
            define_nodes(dot, nodes, col,shortlabel)
            

    # One edge for each unique command sender,receiver listing all commands in label
    dot.attr('edge',fontcolor=cmdcol)
    dot.attr('edge',color=cmdcol)
    for pair,cmds in cmd_pairs.items():
        sender,receiver = pair.split(',')
        if commandlabels:
            cmd_str = '\n'.join(sorted(cmds))
        else:
            cmd_str = None
        dot.edge(sender,receiver,label=cmd_str)

    # One edge showing all commands nobody sends to each component,
    # using dummy nodes as the source
    if missingcommands:
        dot.attr('edge',fontcolor=nocmdcol)
        dot.attr('edge',color=nocmdcol)
        for p,cmds in cmd_no_sender.items():
            if commandlabels:
                cmd_str = '\n'.join(cmds)
            else:
                cmd_str = None
            dot.edge(p+suffix_nocmd,p,label=cmd_str)

    # One edge for each unique event publisher,receiver listing all items as the label
    dot.attr('edge',fontcolor=evcol)
    dot.attr('edge',color=evcol)
    #dot.attr('edge',style='dotted')
    for pair,events in ev_pairs.items():
        publisher,receiver = pair.split(',')
        if eventlabels:
            ev_str = '\n'.join(sorted(events))
        else:
            ev_str = None
        dot.edge(publisher,receiver,label=ev_str)

    # One edge showing all events components need but nobody publishes,
    # using dummy nodes as the source
    if missingevents:
        dot.attr('edge',fontcolor=nocmdcol)
        dot.attr('edge',color=nocmdcol)
        for p,evs in ev_no_publisher.items():
            if eventlabels:
                ev_str = '\n'.join(evs)
            else:
                ev_str = None
            dot.edge(p+suffix_noev,p,label=ev_str,style='dashed')

    # Render the diagram
    if showplot or imagefile:
        dot.render(cleanup=True,view=showplot,filename=imagefile)
    else:
        print("Neither --showplot nor --imagefile specified, no plot generated")

    if dotfile:
        with open(dotfile,'w') as f:
            f.write(dot.source)