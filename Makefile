#
# Example top-level Makefile
#

SUBDIRS := examples
SUBDIRSCLEAN = $(addsuffix .clean,$(SUBDIRS))

.PHONY: all $(SUBDIRS)

all: $(SUBDIRS) 

$(SUBDIRS):
	$(MAKE) -C $@

.PHONY: clean $(SUBDIRSCLEAN)

clean: $(SUBDIRSCLEAN)

$(SUBDIRSCLEAN): %.clean:
	$(MAKE) -C $* clean
