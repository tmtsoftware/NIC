#
# The following is the configuration file for the make files
# It is included in all Makefiles in all the directories.
#

####################
# vars
####################

# get datestamp
DATE = $(shell date +%F)

####################

# current git project root, not necessarily NIC
PROJ_ROOT := $(shell git rev-parse --show-toplevel)

ifndef NIC_ROOT
NIC_ROOT := ${PROJ_ROOT}/../NIC
endif

####################
# functions
####################

# public function for preparing .sec files for doxygen
#	SUBSYSTEM - subsystem name, e.g. NFIRAOS, IRIS
#	COMP_NAME - name of component, e.g. DM Assembly
define nic_doxygen_sec
	$(if ${SUBSYSTEM},,$(error SUBSYSTEM not defined))
	$(if ${COMP_NAME},,$(error COMP_NAME not defined))
	# create directories
	mkdir -p tmp
	# copy common section files (.sec)
	cp ${NIC_ROOT}/template/sec/common/* tmp
	cp ${NIC_ROOT}/template/sec/${SUBSYSTEM}/* tmp
	# update asmPurpose section file
	sed -i -e 's/<<COMP_NAME>>/${COMP_NAME}/g' tmp/*.sec
	sed -i -e 's/<<SUBSYSTEM>>/${SUBSYSTEM}/g' tmp/*.sec
endef

# public function for parsing models files for doxygen tech notes
#	NIC_ROOT - path to head of NIC project
#	ICDDB - path to head of subsystem model file project, e.g. ICD-Model-Files/NFIRAOS-Model-Files/
#	COMP - component short name / directory name in model file project, e.g. dm-assembly
define nic_doxygen_parse_model
	$(if ${NIC_ROOT},,$(error NIC_ROOT not defined))
	$(if ${ICDDB},,$(error ICDDB not defined))
	$(if ${COMP},,$(error COMP not defined))
	# parse model file
	${NIC_ROOT}/template/icddb/parseModelFile.py ${ICDDB} ${COMP} tmp
endef

# public function for building PDF from doxygen output
# 	DOC_TYPE - document type identifier using in file name, e.g. techNote, SBD
#	COMP - component short name used in file name, e.g. dm-assembly
define nic_doxygen_pdf
	$(if ${DOC_TYPE},,$(error DOC_TYPE not defined))
	$(if ${COMP},,$(error COMP not defined))
	# fix latex output to remove reference to *.dox and index section
	cd latex; sed -i -e '/_8dox/d' refman.tex
	cd latex; sed -i -e '/printindex/d' refman.tex
	# Generate PDF output
	cd latex; make pdf; cp refman.pdf ../${COMP}_${DOC_TYPE}_${DATE}.pdf
	# create symbolic link to the current tech note
	ln -sf ${COMP}_${DOC_TYPE}_${DATE}.pdf ./${COMP}_${DOC_TYPE}_current.pdf
	# Prints the first 20 line of the error file
	head -n 20 doxygen.errLog
	#firefox html/index.html &
	#evince ${COMP}_${DOC_TYPE}_${DATE}.pdf &
endef

