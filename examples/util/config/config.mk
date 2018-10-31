#
# The following is the configuration file for the make files
# It is included in all Makefiles in all the directories.
#

####################
# paths
####################

# doxygen version 1.8.12 or later is required
ifndef DOXYGEN
DOXYGEN := doxygen
endif

# assumes PATTERN (designPatterns) and ICDDB (ICD-Model-Files) are siblings of NFIRAOS checkout
# and assumes NFIRAOS mode files (NFIRAOS-Model-Files) are checked out under the ICD-Model-Files directory 

ifndef CONFIG_PATH
CONFIG_PATH := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))
endif

ifndef ROOT_PATH
ROOT_PATH := ${CONFIG_PATH}/../../../../
endif

ifndef NIC_PATH
NIC_PATH := ${ROOT_PATH}/NIC
endif

ifndef TEMPLATE_PATH
TEMPLATE_PATH := ${NIC_PATH}/template
endif

ifndef SUBSYSTEM
SUBSYSTEM := NFIRAOS
endif

ifndef ICDDB
ICDDB := ${ROOT_PATH}/ICD-Model-Files/${SUBSYSTEM}-Model-Files
endif

####################
# env vars
####################

DATE := $(shell date +%F)

####################
# functions
####################

# private function for building generalized doxygen webpages
define make_doxygen_gen
	$(if ${DOXYGEN},,$(error DOXYGEN not defined))
	$(if ${TEMPLATE_PATH},,$(error TEMPLATE_PATH not defined))
	$(if ${SUBSYSTEM},,$(errorSUBSYSTEM  not defined))
	$(if ${COMP_NAME},,$(error COMP_NAME not defined))
	$(if ${COMP},,$(error COMP not defined))
	$(if ${INPUT},,$(error INPUT not defined))
	$(if ${TITLE},,$(error TITLE not defined))
	$(if ${DATE},,$(error DATE not defined))
	$(if ${INPUT},,$(error INPUT not defined))
	# create directories
	mkdir -p pic
	mkdir -p latex
	mkdir -p tmp
	# copy required latex file to local dir
	cp ${TEMPLATE_PATH}/latex/* latex 
	# copy common section files (.sec)
	cp ${TEMPLATE_PATH}/sec/common/* tmp
	cp ${TEMPLATE_PATH}/sec/${SUBSYSTEM}/* tmp
	# update asmPurpose section file
	sed -i -e 's/<<COMP_NAME>>/${COMP_NAME}/g' tmp/*.sec
	sed -i -e 's/<<SUBSYSTEM>>/${SUBSYSTEM}/g' tmp/*.sec
	# parse model file
	$(if $1,${TEMPLATE_PATH}/icddb/parseModelFile.py $1 ${COMP} tmp,)
	# get template doxygen config file
	cp ${TEMPLATE_PATH}/template.doxconf tmp/${COMP}.doxconf
	# update doxygen config template 
	sed -i -e '/^PROJECT_NAME *=/c\PROJECT_NAME = ${TITLE}' tmp/${COMP}.doxconf
	sed -i -e '/^PROJECT_BRIEF *=/c\PROJECT_BRIEF = ${DATE}' tmp/${COMP}.doxconf
	$(if ${DOC_NUM},sed -i -e '/^PROJECT_NUMBER *=/c\PROJECT_NUMBER = ${DOC_NUM}' tmp/${COMP}.doxconf,)
	sed -i -e '/^INPUT *=/c\INPUT = ${INPUT}' tmp/${COMP}.doxconf
	# Generate HTML from doxygen pages
	${DOXYGEN} tmp/${COMP}.doxconf .
endef

# public function for building doxygen webpages from the doc directory
define make_doxygen_doc
	$(call make_doxygen_gen,)
	# Prints the first 20 line of the error file
	head -n 20 doxygen.errLog
	#firefox html/index.html &
endef

# private function for building generalized doxygen tech note PDF
define make_doxygen_techNote_gen
	$(if ${COMP},,$(error COMP not defined))
	$(if ${DATE},,$(error DATE not defined))
	$(call make_doxygen_gen,$1)
	# fix latex output to remove reference to *.dox and index section
	cd latex; sed -i -e '/_8dox/d' refman.tex
	cd latex; sed -i -e '/printindex/d' refman.tex
	# Generate PDF output
	cd latex;	make pdf; cp refman.pdf ../${COMP}_techNote_${DATE}.pdf
	# create symbolic link to the current tech note
	ln -sf ${COMP}_techNote_${DATE}.pdf ./${COMP}_techNote_current.pdf
	# Prints the first 20 line of the error file
	head -n 20 doxygen.errLog
	#firefox html/index.html &
	#evince ${COMP}_techNote_${DATE}.pdf &
endef

# public function for building a doxygen Assembly tech note PDF
define make_doxygen_techNote_asm
	$(if ${ICDDB},,$(error ICDDB not defined))
	$(call make_doxygen_techNote_gen,${ICDDB})
endef

# public function for building a doxygen HCD tech note PDF
define make_doxygen_techNote_hcd
	$(call make_doxygen_techNote_gen,../../hcd-model-files)
endef

# public function for building a doxygen Component tech note PDF
# i.e. a general tech note that doesn't have model file data included
define make_doxygen_techNote_comp
	$(call make_doxygen_techNote_gen,)
endef

# public function for cleaning up a doc or techNote directory 
define make_doxygen_clean
	/bin/rm -f html/*
	/bin/rm -f latex/*
	/bin/rm -f tmp/*
	/bin/rm -f *_techNote_*.pdf
	/bin/rm -f doxygen.errLog   
endef



