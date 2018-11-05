# Mostly basic wrappers around sbt for now

SUBDIRS := doc
SUBDIRSCLEAN = $(addsuffix .clean,$(SUBDIRS))

.PHONY: all $(SUBDIRS)

all: build $(SUBDIRS)

$(SUBDIRS):
	$(MAKE) -C $@

.PHONY: clean $(SUBDIRSCLEAN)

build:
	sbt stage

test: build
	sbt test

clean: $(SUBDIRSCLEAN)
	sbt clean

$(SUBDIRSCLEAN): %.clean:
	$(MAKE) -C $* clean
