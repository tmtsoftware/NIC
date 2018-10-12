
.SUFFIXES:
.SUFFIXES: .c .cpp .d .h .o .a

INC = ../h
OBJ = ../obj
BIN = ../bin
LIB = ../lib

LINKOPTS = -L${LIB} -lm -lrt -lpthread

TESTOPTS = -DCOMP_TEST_MAIN

####################

CC = gcc 
CFLAGS = -std=gnu99 -g -Wall -Wextra -O3 -I${INC} -I${DAOLIB}/h  

CXX = g++ 
CXXFLAGS = -std=gnu++98 -Wall -Wextra -O3 -I${INC} ${PROFOPTS}

####################
SRCS = \
	templateA.c \
	templateB.cpp

OBJS := $(addprefix ${OBJ}/, $(addsuffix .o, $(basename ${SRCS})))

####################
all: \
	libTemplate.a \
	comptests

comptests: \
	templateATester

clean: 
	/bin/rm -f ${BIN}/*
	/bin/rm -f ${OBJ}/*.o
	/bin/rm -f ${LIB}/lib*.a

####################
# Build library - make this first target so it is default

libTemplate.a: $(OBJS)
	@echo -e "\nCompiling and creating library $@"
	ar r ${LIB}/$@ $(addprefix ${OBJ}/, $?)

####################

# Rule to compile C files into object files
${OBJ}/%.o : %.c ${INC}/%.h
	@echo -e "\nCompiling $< to $@"
	${CC} ${CFLAGS} -c -o $@ $< 

####################

# Rule to compile C++ files into object files
${OBJ}/%.o : %.cpp ${INC}/%.hpp
	@echo -e "\nCompiling $< to $@"
	${CXX} ${CXXFLAGS} -c -o $@ $< 

####################

templateATester: libTemplate.a \
	templateA.c ${INC}/templateA.h
	@echo -e "\nBuilding $@"
	${CC} ${CFLAGS} ${TESTOPTS} templateA.c -o ${BIN}/$@ ${LINKOPTS}

