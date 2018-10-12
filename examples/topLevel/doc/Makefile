include ../../util/config/config.mk

COMP := topLevel
COMP_NAME := Top Level
TITLE := "System Top Level"
INPUT := \.

all:
	mkdir -p ./tmp
	cp  ../../*/doc/*Intro.sec ./tmp
	$(call make_doxygen_doc)

clean:
	$(call make_doxygen_clean)

