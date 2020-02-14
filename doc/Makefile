
DOXYGEN := doxygen

TEMPLATE_PATH := ../template/

DOXCONF := ./nic.doxconf

PROJECT_NAME   := "NFIRAOS-IRIS Commom Repository (NIC)"
PROJECT_NUMBER := 
PROJECT_BRIEF := $(shell date +%F)

INPUT := .

all:
	# copy required latex file to local dir
	cp ${TEMPLATE_PATH}/latex/* latex 
	# get template doxygen config file
	cp ${TEMPLATE_PATH}/template.doxconf ${DOXCONF}
	# update doxygen config template 
	sed -i -e '/^PROJECT_NAME *=/c\PROJECT_NAME = ${PROJECT_NAME}' ${DOXCONF}
	sed -i -e '/^PROJECT_BRIEF *=/c\PROJECT_BRIEF = ${PROJECT_BRIEF}' ${DOXCONF}
	sed -i -e '/^PROJECT_NUMBER *=/c\PROJECT_NUMBER = ${PROJECT_NUMBER}' ${DOXCONF}
	sed -i -e '/^INPUT *=/c\INPUT = ${INPUT}' ${DOXCONF}
	# Generate HTML from doxygen pages
	${DOXYGEN} ${DOXCONF}

clean:
	/bin/rm -f ${DOXCONF}
	/bin/rm -f html/*
	/bin/rm -f latex/*
	/bin/rm -f doxygen.errLog
