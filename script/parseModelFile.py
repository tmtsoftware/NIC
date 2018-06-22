#!/usr/bin/python
#

import os
import sys
import re
import fnmatch
from pyhocon import ConfigFactory
import markdown2

# subsystem pub/sub prefixes
sysPrefix = {'NFIRAOS': 'ao.nfiraos', 'TCS': 'tcs', 'AOESW': 'ao.aoesw'}

# The encoding to generate .sec files.
DOXYGEN_DOC_ENCODING='utf-8'

## Sanitize a string so that it displays correctly with latex
def latexStr(str):
  # Underscores need to be escaped.
  str_sanitized = str.replace('_','\_')
  return str_sanitized

## Sanitize a string so that it can be used as a target label
def targetStr(str):
  # Underscores need to be removed.
  str_sanitized = str.replace('_','')
  return str_sanitized

## get a value from the directory, if not present then an empty string is returned
def getVal(dir,name):
  val = ''
  if name in dir: 
    val = dir[name]
    
  if isinstance(val, unicode):
    return val.encode(DOXYGEN_DOC_ENCODING)
  else:
    return val

## get the description from the directory, parse it as markdown and
## convert it to HTML
def getDescription(dir):
  return markdown2.markdown(getVal(dir, 'description')).encode(DOXYGEN_DOC_ENCODING)

## parse an enum and build enum list string separated by '|'
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

  # check if type
  if 'type' in elem:
    type = elem['type']
    
    # check if array
    if type == 'array':
      dim = getVal(elem,'dimensions')
      subType = ''
      enum = ''
      
      # check for iteam
      if 'items' in elem:
        item = elem['items']
        if 'type' in item:
          subType = item['type']
        elif 'enum' in item:
          enum = item['enum']
       
      # write array type and array dimension 
      dt = "{0}{1}".format(subType,dim)
          
      if enum != '':
        enumRange = getEnumStr(enum)
   
    # if not array then write the type
    else:
      dt = "{0}".format(type)

  # if not type then check if enum 
  elif 'enum' in elem:
    dt = "enum"
    enumRange = getEnumStr(elem['enum'])
  
  # get min/max range
  minMaxRange = getRangeStr(elem)

  ## write min/max and enum range
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

## get subsystem string and rate 
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


## write component section
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
  file.write("{0}<br>\n".format(getDescription(conf)))
  
  file.close()

  return prefix, title

## write published telemetry section
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
      file.write("\latexonly\n\subsection{"+latexStr(name)+" Telemetry}\n\endlatexonly\n")
      file.write("<b>Telemetry item:</b> {0}.{1}<br>\n".format(prefix,name))  

      if 'archive' in tel[i]:
        file.write("<b>Archived:</b> {0}<br>\n".format(tel[i]['archive']))

      str = getRateStr(tel[i])
      if str != '':
        file.write(str)

      file.write("{0}<br>\n\n".format(getDescription(tel[i])))

      if 'attributes' in tel[i]:
        file.write("<table>\n<tr><th> Attribute <th> Data Type <th> Units <th> Range <th> Description\n")
        attr = tel[i]['attributes']
        for j in range(0, len(attr)):
          file.write("<tr><td> "+getVal(attr[j],'name'))
          dateType, rangeStr = getDtStr(attr[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(attr[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getDescription(attr[j]))
        file.write("\n</table>\n\n")
  else:
    file.write("N/A<br>\n")

  file.close()

## write published event section 
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
      file.write("\latexonly\n\subsection{"+latexStr(name)+" Event}\n\endlatexonly\n")
      file.write("<b>Event item:</b> {0}.{1}<br>\n".format(prefix,name))  

      if 'archive' in tel[i]:
        file.write("<b>Archived:</b> {0}<br>\n".format(tel[i]['archive']))

      str = getRateStr(tel[i])
      if str != '':
        file.write(str)

      file.write("{0}<br>\n\n".format(getDescription(tel[i])))

      if 'attributes' in tel[i]:
        file.write("<table>\n<tr><th> Attribute <th> Data Type <th> Units <th> Range <th> Description\n")
        attr = tel[i]['attributes']
        for j in range(0, len(attr)):
          file.write("<tr><td> "+getVal(attr[j],'name'))
          dateType, rangeStr = getDtStr(attr[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(attr[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getDescription(attr[j]))
        file.write("\n</table>\n\n")
  else:
    file.write("N/A<br>\n")

  file.close()


## write published alarms section
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
      file.write("<td> "+getDescription(alarm[i]))
    file.write("\n</table>\n\n")

  file.close()


## write subscribed telemetry section
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

## write subscribed event section
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


## write commands section
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
    
  file.write("\latexonly \\begin{itemize}\n")

  for i in range(0, len(rec)):
      name = getVal(rec[i],'name')
      file.write("\\item ")
      file.write("\hyperref[" + targetStr(name) + "]{" + latexStr(name) + "}\n")

  file.write("\\end{itemize} \endlatexonly\n")

  for i in range(0, len(rec)):
      name = getVal(rec[i],'name')

      file.write("<hr>\n")

##  Added functionality to allow linking to command subsections.
##  In the doxygen file, add the following to lines to link to a command foo.
##  \latexonly \hyperref[foo]{foo}\endlatexonly
##  \htmlonly<a href="#foo">foo</a>\endhtmlonly
##
##  Linking only works within the document that included the command.sec file.
##
##      file.write("\latexonly\n\subsection{"+latexStr(name)+" Command}\n\endlatexonly\n")
      file.write("\latexonly\n\subsection{"+latexStr(name)+" Command}\n\endlatexonly\n")
      file.write("\latexonly\n\label{"+targetStr(name)+"}\n\endlatexonly\n")
      file.write("\htmlonly<a id="+targetStr(name)+"></a>\endhtmlonly\n")

      file.write("<b>Command:</b> {0}.{1}<br>\n".format(prefix,name))  
      file.write("{0}<br>\n\n".format(getDescription(rec[i])))

      if 'args' in rec[i]:
        file.write("<table>\n<tr><th> Argument <th> Data Type <th> Units <th> Range <th> Description\n")
        arg = rec[i]['args']
        for j in range(0, len(arg)):
          file.write("<tr><td> "+getVal(arg[j],'name'))
          dateType, rangeStr = getDtStr(arg[j])
          file.write("<td> "+dateType)
          file.write("<td> "+getVal(arg[j],'units'))
          file.write("<td> "+rangeStr)
          file.write("<td> "+getDescription(arg[j]))
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



