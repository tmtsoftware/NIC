#!/usr/bin/python
#

import os
import sys
import re
import fnmatch
from pyhocon import ConfigFactory

# subsystem pub/sub prefixes
sysPrefix = {'NFIRAOS': 'ao.nfiraos', 'TCS': 'tcs', 'AOESW': 'ao.aoesw'}

# The encoding to generate .sec files.
DOXYGEN_DOC_ENCODING='utf-8'

# Configurations for subsections that render information from model files:
#
#   modelfile: refers to the input model file from which the data are parsed
#   secfile  : section that will be included in a doxygen file
#   tagprefix: prefix that will be added to item names to create tags
#
# Note that a number of ALIASES have been added to template.doxconf that
# make use of the tags to simplify cross-referencing.

command_c = {
  'modelfile' : 'command-model.conf',
  'secfile'   : 'command.sec',
  'tagprefix' : 'cmd'   # should match "icdcmd" ALIAS in template.doxconf
}

pubevent_c = {
  'modelfile' : 'publish-model.conf',
  'secfile'   : 'publishEvent.sec',
  'tagprefix' : 'pubev' # should match "icdpubev" ALIAS in template.doxconf
}

subevent_c = {
  'modelfile' : 'subscribe-model.conf',
  'secfile'   : 'subscribeEvent.sec',
  'tagprefix' : 'subev' # should match "icdsubbev" ALIAS in template.doxconf
}

component_c = {
  'modelfile' : 'component-model.conf',
  'secfile'   : 'component.sec'
}

alarm_c = {
  'modelfile' : 'publish-model.conf',
  'secfile'   : 'alarm.sec',
  'tagprefix' : 'alarm' # should match "icdalarm" ALIAS in template.doxconf
}

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

## Generate tag string for both LaTex and HTML output using prefix
## specified in the various subsection *_c configuration dictionaries
def getTag(model_c,name):
  latex_tag = model_c['tagprefix']+latexStr(name)
  html_tag = model_c['tagprefix']+targetStr(name)
  tagstr = "\latexonly\n\label{"+latex_tag+"}\n\endlatexonly\n" + \
    "\htmlonly<a id="+html_tag+"></a>\endhtmlonly\n"
  return tagstr

## get a value from the directory, if not present then an empty string is returned
def getVal(dir,name):
  val = ''
  if name in dir: 
    val = dir[name]
    
  if isinstance(val, unicode):
    return val.encode(DOXYGEN_DOC_ENCODING)
  else:
    return val

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
      str = "{0} to {1}".format(minRate,maxRate)
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
  file = open(outDir+"/"+component_c['secfile'], 'w')

  filename = compDir+"/"+component_c['modelfile']
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


## write published event section 
def writePubEvent(compDir, outDir, prefix, title):
  file = open(outDir+"/"+pubevent_c['secfile'], 'w')

  filename = compDir+"/"+pubevent_c['modelfile']
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

      ##  Enable linking to published event subsections.
      ##  In the doxygen file, add the following to lines to link to an event foo.
      ##  \latexonly \hyperref[foo]{foo}\endlatexonly
      ##  \htmlonly<a href="#foo">foo</a>\endhtmlonly
      ##
      ##  Linking only works within the document that included the publishEvent.sec file.
      file.write("\latexonly\n\subsection{"+latexStr(name)+" Event}\n\endlatexonly\n")
      file.write(getTag(pubevent_c,name))

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


## write published alarms section
def writeAlarm(compDir, outDir, prefix, title):
  file = open(outDir+"/"+alarm_c['secfile'], 'w')

  filename = compDir+"/"+alarm_c['modelfile']
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
      name = getVal(alarm[i],'name')
      
      ##  Enable linking to published alarms.
      ##  See ALIASES in template.doxconf to see how to reference alarm tags.
      ##
      ##  Linking only works within the document that included the alarm.sec file.
      file.write(getTag(alarm_c,name.split('.')[-1]))

      file.write("<tr><td> {0}.{1} ".format(prefix,name))
      file.write("<td> "+getVal(alarm[i],'severity'))
      file.write("<td> {0}".format(getVal(alarm[i],'archive')))
      file.write("<td> "+getVal(alarm[i],'description'))
    file.write("\n</table>\n\n")

  file.close()

## write subscribed event section
def writeSubEvent(compDir, outDir, title):
  file = open(outDir+"/"+subevent_c['secfile'], 'w')

  filename = compDir+"/"+subevent_c['modelfile']
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
      
      ##  Enable linking to subscribed events.
      ##  See ALIASES in template.doxconf to see how to reference subscribed events.
      ##
      ##  Linking only works within the document that included the subscribeEvent.sec file.
      file.write(getTag(subevent_c,item))

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
  file = open(outDir+"/"+command_c['secfile'], 'w')

  filename = compDir+"/"+command_c['modelfile']
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
      file.write("\hyperref[" + command_c['tagprefix']+targetStr(name) + "]{" + latexStr(name) + "}\n")

  file.write("\\end{itemize} \endlatexonly\n")

  for i in range(0, len(rec)):
      name = getVal(rec[i],'name')

      file.write("<hr>\n")

      ##  Enable linking to command subsections.
      ##  See ALIASES in template.doxconf to see how to reference command tags.
      ##
      ##  Linking only works within the document that included the command.sec file.
      file.write("\latexonly\n\subsection{"+latexStr(name)+" Command}\n\endlatexonly\n")
      file.write(getTag(command_c,name))

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
  writeSubEvent(compDir,outDir,title)
  writePubEvent(compDir,outDir,prefix,title)
  writeAlarm(compDir,outDir,prefix,title)
  writeCmd(compDir,outDir,prefix,title)

except IOError as e:
  print "I/O error({0}): {1}".format(e.errno, e.strerror)
  file.close()

# end of file



