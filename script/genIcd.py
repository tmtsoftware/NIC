#!/usr/bin/python
#
# ICD model file helper: given a target directory tree, generate
# command-model.conf that sends all commands to assemblies in that
# tree, and subscribe-model.conf that subscribes to all events
# published by assemblies in that tree.

import os
import sys
import re
import fnmatch
from pyhocon import ConfigFactory

## main
#

if len(sys.argv) != 4:
    print """genIcd: Generate command-model.conf that sends all commands, and subscribe-model.conf that subscribes to all events, for assemblies under the input model path.

usage:
    genIcd <input model path> <output command-model file> <output subscribe-model file>
"""
    sys.exit(1)

in_dir = sys.argv[1]
cmd_name = sys.argv[2]
sub_name = sys.argv[3]

# use a dictionary to keep track of differences between parsing
# commands and events
types = {}
types['cmd'] = {
    'outname':cmd_name,            # name of the output file
    'startstr': 'send = [\n',        # start file with this
    'endstr'  : ']\n',               # end file with this
    'space'   : '  ',              # space at start of each output line
    'inname':'command-model.conf', # name of model file to parse
    'key':'receive'                # key pointing to array to iterate over
    }
types['event'] = {
    'outname':sub_name,            # name of the output file
    'startstr': 'subscribe {\n  events = [\n', # start file with this
    'endstr'  : '  ]\n}\n',        # end file with this
    'space'   : '    ',            # space at start of each output line
    'inname':'publish-model.conf', # name of model file to parse
    'root':'publish',              # optional root containing key
    'key':'events'                 # key pointing to array to iterate over
    }


# open output files and store in dictionary
for type in types:
    outname = types[type]['outname']
    try:
        f = open(outname,"w")
        types[type]['outfile'] = f
        f.write(types[type]['startstr'])
    except Exception as e:
        print "Error creating output file " + outname + \
            "({0}): {1}".format(e.errno, e.strerror)
        sys.exit(1)

for root, dirs, files in os.walk(in_dir):
    # skip hidden
    files = [f for f in files if not f[0] == '.']
    dirs[:] = [d for d in sorted(dirs) if not d[0] == '.']

    # Parse different types of input files
    for type in types:
        d = types[type]
        outfile = d['outfile']
        spc = d['space']
        if d['inname'] in files:
            f = root+'/'+d['inname']
            try:
                conf = ConfigFactory.parse_file(f)
                subsystem = conf['subsystem']
                component = conf['component']

                if 'root' in d:
                    a = conf[d['root']][d['key']]
                else:
                    a = conf[d['key']]

                outfile.write('\n%s// %s %s\n' % (spc,subsystem,component))
                for r in a:
                    name = r['name']
                    print type, subsystem, component, name
                    s = """%s{
%s  subsystem  = %s
%s  component  = %s
%s  name       = %s
%s}
""" % (spc,spc,subsystem,spc,component,spc,name,spc)
                    outfile.write(s)
            except Exception as e:
                pass


for type in types:
    try:
        f = types[type]['outfile']
        f.write(types[type]['endstr'])
        f.close()
    except Exception as e:
        print "Error finalizing output file ({0}): {1}".format(e.errno, e.strerror)
        sys.exit(1)
