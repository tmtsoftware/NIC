#!/usr/bin/python
#

import os
import sys
import re
import fnmatch
from pyhocon import ConfigFactory

# subsystem pub/sub prefixes
sysPrefix = {'NFIRAOS': 'ao.nfiraos', 'TCS': 'tcs', 'AOESW': 'ao.aoesw'}

## get a value from the directory, if not present then an empty string is returned
def getVal(dir,name):
  val = ''
  if name in dir: 
    val = dir[name]
  return val

## parse an enum and build enum list string
def getEnumStr(enum):
  str = ''
  for k in range(0, len(enum)):
    if k == len(enum)-1:
      str += "{0}".format(enum[k])
    else:
      str += "{0} <br> | ".format(enum[k])
  return str

## parse data type and build output string
def getDtStr(elem):
  dt = ''
  rangeStr = ''
  enumRange = ''
  minMaxRange = ''
  if 'type' in elem:
    type = elem['type']
    if type == 'array':
      dim = getVal(elem,'dimensions')
      subType = ''
      enum = ''
      if 'items' in elem:
        item = elem['items']
        if 'type' in item:
          subType = item['type']
        elif 'enum' in item:
          enum = item['enum']
       
      dt = "{0}{1}".format(subType,dim)
          
      if enum != '':
        enumRange = getEnumStr(enum)
    else:
      dt = "{0}".format(type)
  elif 'enum' in elem:
    dt = "enum"
    enumRange = getEnumStr(elem['enum'])
  
  minMaxRange = getRangeStr(elem)

  if minMaxRange != '' and enumRange != '':
    rangeStr = "{0} ({1})".format(enumRange,minMaxRange)
  elif minMaxRange != '':
    rangeStr = minMaxRange
  elif enumRange != '':
    rangeStr = enumRange
  return dt, rangeStr


## parse range and build output string
def getRangeStr(elem):
  str = ''

  if 'maximum' in elem and 'minimum' in elem:
    str = "{0} to {1}".format(elem['minimum'],elem['maximum'])
  elif 'maximum' in elem:
    str = "&le; {0}".format(elem['maximum']) 
  elif 'minimum' in elem:
    str = "&ge; {0}".format(elem['minimum'])

  return str


## parse rate and build output string
def getRateStr(elem):
  str = ''

  if 'maxRate' in elem and 'minRate' in elem:
    maxRate = elem['maxRate']
    minRate = elem['minRate']
    if maxRate == minRate:
      str = "{0}".format(maxRate)
    else:
      str = "{0} to {1}".format(maxRate,minRate)
  elif 'maxRate' in elem:
    str = "&le; {0}".format(elem['maxRate']) 
  elif 'minRate' in elem:
    str = "&ge; {0}".format(elem['minRate'])

  if str != '':
    str = "<b>Rate:</b> "+str+" Hz<br>\n"

  return str

## 
def getSubTableStr(tel):
  subsystem = getVal(tel,'subsystem')
  component = getVal(tel,'component')
  name = getVal(tel,'name')
  rate = getVal(tel,'requiredRate')

  if subsystem in sysPrefix:
    prefix = sysPrefix[subsystem]
  else:
    prefix = subsystem

  item = "{0}.{1}.{2}".format(prefix,component,name)

  return item, rate


##
def writeComp(compDir, outDir):
  file = open(outDir+"/component.sec", 'w')

  filename = compDir+"/component-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)

  comp = getVal(conf,'component')
  title = getVal(conf,'title')
  prefix = getVal(conf,'prefix')

  file.write("The prefix for the {0} is: <em>{1}</em>\n\n".format(title,prefix))
  file.write("{0}<br>\n".format(getVal(conf,'description')))
  
  file.close()

  return prefix, title

##
def writePubTelem(compDir, outDir, prefix, title):
  file = open(outDir+"/publishTelem.sec", 'w')

  filename = compDir+"/publish-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  print(filename)
  sys.stdout.flush()
  conf = ConfigFactory.parse_file(filename)

  if 'publish' not in conf:
    return
  pub = conf['publish']

  if 'telemetry' in pub:
    tel = pub['telemetry']

    file.write("The {0} publishes the following telemetry items:\n\n".format(title))

    for i in range(0, len(tel)):
      name = getVal(tel[i],'name')

      file.write("<hr>\n")
      file.write("\latexonly\n\subsection{"+name+" Telemetry}\n\endlatexonly\n")
      file.write("<b>Telemetry item:</b> {0}.{1}<br>\n".format(prefix,name))  

      if 'archive' in tel[i]:
        file.write("<b>Archived:</b> {0}<br>\n".format(tel[i]['archive']))

      str = getRateStr(tel[i])
      if str != '':
        file.write(str)

      file.write("{0}<br>\n\n".format(getVal(tel[i],'description')))

      if 'attributes' in tel[i]:
        file.write("<table>\n<tr><th> Attribute <th> Data Type <th> Units <th> Range <th> Description\n")
        attr = tel[i]['attributes']
        for j in range(0, len(attr)):
          file.write("<tr><td> "+getVal(attr[j],'name'))
          dateType, rangeStr = getDtStr(attr[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(attr[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getVal(attr[j],'description'))
        file.write("\n</table>\n\n")
  else:
    file.write("N/A<br>\n")

  file.close()

##
def writePubEvent(compDir, outDir, prefix, title):
  file = open(outDir+"/publishEvent.sec", 'w')

  filename = compDir+"/publish-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)

  if 'publish' not in conf:
    return
  pub = conf['publish']

  if 'events' in pub:
    tel = pub['events']

    file.write("The {0} publishes the following event items:\n\n".format(title))

    for i in range(0, len(tel)):
      name = getVal(tel[i],'name')

      file.write("<hr>\n")
      file.write("\latexonly\n\subsection{"+name+" Event}\n\endlatexonly\n")
      file.write("<b>Event item:</b> {0}.{1}<br>\n".format(prefix,name))  

      if 'archive' in tel[i]:
        file.write("<b>Archived:</b> {0}<br>\n".format(tel[i]['archive']))

      str = getRateStr(tel[i])
      if str != '':
        file.write(str)

      file.write("{0}<br>\n\n".format(getVal(tel[i],'description')))

      if 'attributes' in tel[i]:
        file.write("<table>\n<tr><th> Attribute <th> Data Type <th> Units <th> Range <th> Description\n")
        attr = tel[i]['attributes']
        for j in range(0, len(attr)):
          file.write("<tr><td> "+getVal(attr[j],'name'))
          dateType, rangeStr = getDtStr(attr[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(attr[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getVal(attr[j],'description'))
        file.write("\n</table>\n\n")
  else:
    file.write("N/A<br>\n")

  file.close()


##
def writeAlarm(compDir, outDir, prefix, title):
  file = open(outDir+"/alarm.sec", 'w')

  filename = compDir+"/publish-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)

  if 'publish' not in conf:
    return
  pub = conf['publish']

  if 'alarms' in pub:
    alarm = pub['alarms']

    file.write("The {0} publishes the following alarms:\n\n".format(title))
    
    file.write("<table>\n<tr><th> Alarm <th> Severity <th> Archived <th> Description\n")
    for i in range(0, len(alarm)):
      file.write("<tr><td> {0}.{1} ".format(prefix,getVal(alarm[i],'name')))  
      file.write("<td> "+getVal(alarm[i],'severity'))
      file.write("<td> {0}".format(getVal(alarm[i],'archive')))
      file.write("<td> "+getVal(alarm[i],'description'))
    file.write("\n</table>\n\n")

  file.close()


##
def writeSubTelem(compDir, outDir, title):
  file = open(outDir+"/subscribeTelem.sec", 'w')

  filename = compDir+"/subscribe-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)
 
  if 'subscribe' not in conf:
    return
  sub = conf['subscribe']

  if 'telemetry' in sub:
    tel = sub['telemetry']

    file.write("The {0} subscribes to the following telemetry items:\n\n".format(title))

    file.write("<table>\n<tr><th> Subscription <th> Rate (Hz)\n")
    for i in range(0, len(tel)):
      item, rate = getSubTableStr(tel[i])
      file.write("<tr><td> {0} <td> {1}".format(item,rate))
      if 'usage' in tel[i]:
        file.write("<br><b>Usage:</b><br>"+tel[i]['usage'])
      file.write("\n")
    file.write("</table>\n")
  else:
    file.write("N/A<br>\n")

  file.close()

##
def writeSubEvent(compDir, outDir, title):
  file = open(outDir+"/subscribeEvent.sec", 'w')

  filename = compDir+"/subscribe-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)
 
  if 'subscribe' not in conf:
    return
  sub = conf['subscribe']

  if 'events' in sub:
    tel = sub['events']

    file.write("The {0} subscribes to the following event items:\n\n".format(title))

    file.write("<table>\n<tr><th> Subscription <th> Rate (Hz)\n")
    for i in range(0, len(tel)):
      item, rate = getSubTableStr(tel[i])
      file.write("<tr><td> {0} <td> {1}".format(item,rate))
      if 'usage' in tel[i]:
        file.write("<br><b>Usage:</b><br>"+tel[i]['usage'])
      file.write("\n")
    file.write("</table>\n")
  else:
    file.write("N/A<br>\n")

  file.close()


##
def writeCmd(compDir, outDir, prefix, title):
  file = open(outDir+"/command.sec", 'w')

  filename = compDir+"/command-model.conf"
  if not os.path.isfile(filename):
    file.write("N/A<br>\n")
    file.close()
    return
  conf = ConfigFactory.parse_file(filename)

  if 'receive' not in conf:
    return
  rec = conf['receive']

  file.write("The {0} accepts the following commands:\n\n".format(title))
    
  for i in range(0, len(rec)):
      name = getVal(rec[i],'name')

      file.write("<hr>\n")
      file.write("\latexonly\n\subsection{"+name+" Command}\n\endlatexonly\n")
      file.write("<b>Command:</b> {0}.{1}<br>\n".format(prefix,name))  
      file.write("{0}<br>\n\n".format(getVal(rec[i],'description')))

      if 'args' in rec[i]:
        file.write("<table>\n<tr><th> Argument <th> Data Type <th> Units <th> Range <th> Description\n")
        arg = rec[i]['args']
        for j in range(0, len(arg)):
          file.write("<tr><td> "+getVal(arg[j],'name'))
          dateType, rangeStr = getDtStr(arg[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(arg[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getVal(arg[j],'description'))
        file.write("\n</table>\n\n")

      if 'requiredArgs' in rec[i]:
        arg = rec[i]['requiredArgs']
        file.write("<b>Required Args:</b> ")
        for j in range(0, len(arg)):
          if j == len(arg)-1:
            file.write("{0}<br>\n".format(arg[j]))
          else:
            file.write("{0}, ".format(arg[j]))

  file.close()

## main
#

inDir = sys.argv[1]
compName = sys.argv[2]
outDir = sys.argv[3]

try:

  compDir = "{0}/{1}".format(inDir,compName)
  prefix, title = writeComp(compDir,outDir)
  writeSubTelem(compDir,outDir,title)
  writeSubEvent(compDir,outDir,title)
  writePubTelem(compDir,outDir,prefix,title)
  writePubEvent(compDir,outDir,prefix,title)
  writeAlarm(compDir,outDir,prefix,title)
  writeCmd(compDir,outDir,prefix,title)

except IOError as e:
  print "I/O error({0}): {1}".format(e.errno, e.strerror)
  file.close()

# end of file



